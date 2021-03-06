package org.haobtc.onekey.onekeys.homepage.mindmenu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Map;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.bean.LocalWalletInfo;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.business.wallet.AccountManager;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.event.FinishEvent;
import org.haobtc.onekey.event.GotPassEvent;
import org.haobtc.onekey.event.LoadOtherWalletEvent;
import org.haobtc.onekey.event.SecondEvent;
import org.haobtc.onekey.manager.ActivityManager;
import org.haobtc.onekey.manager.PreferencesManager;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.HomeOneKeyActivity;
import org.haobtc.onekey.ui.activity.SoftPassActivity;
import org.haobtc.onekey.ui.dialog.BackupRequireDialog;

public class DeleteWalletActivity extends BaseActivity
        implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.checkbox_ok)
    CheckBox checkboxOk;

    @BindView(R.id.btn_forward)
    Button btnForward;

    @BindView(R.id.text_title)
    TextView textTitle;

    @BindView(R.id.delete_wallet_tip1)
    TextView deleteWalletTip1;

    @BindView(R.id.delete_wallet_tip2)
    TextView deleteWalletTip2;

    @BindView(R.id.once_tv)
    TextView onceTV;

    private String deleteHdWalletName;
    private String importHdword;
    private String walletName;
    private boolean isBackup;
    private String deleteWalletType;
    private AccountManager mAccountManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_delete_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mAccountManager = new AccountManager(this);
        checkboxOk.setOnCheckedChangeListener(this);
        deleteHdWalletName = getIntent().getStringExtra("deleteHdWalletName");
        importHdword = getIntent().getStringExtra("importHdword");
        walletName = getIntent().getStringExtra("walletName");
        isBackup = getIntent().getBooleanExtra("isBackup", false);
        deleteWalletType = getIntent().getStringExtra("delete_wallet_type");
        Log.i("deleteWalletType", "initView: " + deleteWalletType);
        if ("deleteSingleWallet".equals(importHdword)) {
            onceTV.setText(R.string.delete_risk_tip_1);
            textTitle.setText(getString(R.string.delete_single_wallet));
            deleteWalletTip1.setText(getString(R.string.delele_tip1));
            deleteWalletTip2.setText(getString(R.string.delete_tip2));
            btnForward.setText(R.string.delete);
        }
    }

    @Override
    public void initData() {}

    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_forward})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_forward:
                deleteOtherWallet();
                break;
        }
    }

    private void deleteOtherWallet() {
        if ("deleteSingleWallet".equals(importHdword) && !isBackup) {
            // ?????????????????????
            new BackupRequireDialog(mContext).show(getSupportFragmentManager(), "backup_require");
            return;
        }
        startActivity(new Intent(this, SoftPassActivity.class));
    }

    @Subscribe
    public void onGotPass(GotPassEvent event) {
        if ("deleteSingleWallet".equals(importHdword)) {
            deleteSingleWallet(event.getPassword());
        } else if (!Strings.isNullOrEmpty(deleteHdWalletName)) {
            deleteAllMainWallet(event.getPassword());
        }
    }

    public void onDeleteSuccess(String walletName) {
        mToast(getString(R.string.delete_succse));
        PreferencesManager.remove(this, Constant.WALLETS, walletName);
        EventBus.getDefault().post(new LoadOtherWalletEvent());
        ActivityManager.getInstance().finishLeftActivity(HomeOneKeyActivity.class);
    }

    private void deleteSingleWallet(String password) {
        String keyName = mAccountManager.getCurrentWalletName();
        PyResponse<Void> response = PyEnv.deleteWallet(password, keyName, false);
        String errors = response.getErrors();
        if (Strings.isNullOrEmpty(errors)) {
            onDeleteSuccess(keyName);
        } else {
            mlToast(errors);
        }
    }

    /**
     * ?????????????????????
     *
     * @param password ????????????
     */
    private void deleteAllMainWallet(String password) {
        ArrayList<String> hd = new ArrayList<>();
        Map<String, ?> jsonToMap = PreferencesManager.getAll(this, Constant.WALLETS);
        jsonToMap
                .entrySet()
                .forEach(
                        stringEntry -> {
                            LocalWalletInfo info =
                                    LocalWalletInfo.objectFromData(
                                            stringEntry.getValue().toString());
                            int walletType = info.getWalletType();
                            if (walletType == Vm.WalletType.MAIN) {
                                hd.add(info.getName());
                            }
                        });
        PyResponse<Void> response = PyEnv.deleteWallet(password, deleteHdWalletName, true);
        String errors = response.getErrors();
        if (Strings.isNullOrEmpty(errors)) {
            hd.forEach(
                    (name) -> {
                        PreferencesManager.remove(this, Constant.WALLETS, name);
                    });
            onDeleteSuccess(deleteHdWalletName);
        } else {
            mlToast(errors);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            btnForward.setEnabled(true);
            btnForward.setBackground(getDrawable(R.drawable.delete_wallet_yes));
        } else {
            btnForward.setEnabled(false);
            btnForward.setBackground(getDrawable(R.drawable.delete_wallet_no));
        }
    }

    @Subscribe
    public void onFinish(FinishEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if ("finish".equals(msgVote)) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
