package org.haobtc.onekey.onekeys.homepage.mindmenu;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.google.common.base.Strings;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.event.FinishEvent;
import org.haobtc.onekey.event.GotPassEvent;
import org.haobtc.onekey.exception.HardWareExceptions;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.backup.BackupGuideActivity;
import org.haobtc.onekey.ui.activity.SoftPassActivity;
import org.haobtc.onekey.ui.dialog.BackupRequireDialog;

public class WalletManageActivity extends BaseActivity {

    @BindView(R.id.text_safe)
    TextView textSafe;

    @BindView(R.id.rel_export_word)
    RelativeLayout relExportWord;

    private String deleteHdWalletName;

    @Override
    public int getLayoutId() {
        return R.layout.activity_wallet_manage;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        deleteHdWalletName = getIntent().getStringExtra("deleteHdWalletName");
    }

    @Override
    public void initData() {}

    @SingleClick
    @OnClick({R.id.img_back, R.id.rel_export_word, R.id.rel_delete_wallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.rel_export_word:
                startActivity(new Intent(this, SoftPassActivity.class));
                break;
            case R.id.rel_delete_wallet:
                hdWalletIsBackup();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGotPass(GotPassEvent event) {
        PyResponse<String> response =
                PyEnv.exportMnemonics(event.getPassword(), deleteHdWalletName);
        String errors = response.getErrors();
        if (Strings.isNullOrEmpty(errors)) {
            Intent intent = new Intent(this, BackupGuideActivity.class);
            intent.putExtra("exportWord", response.getResult());
            intent.putExtra("importHdword", "exportHdword");
            startActivity(intent);
            finish();
        } else {
            mlToast(errors);
        }
    }

    private void hdWalletIsBackup() {
        Log.i("deleteHdWalletNamejxm", "hdWalletIsBackup: " + deleteHdWalletName);
        try {
            PyObject data =
                    PyEnv.sCommands.callAttr(
                            "get_backup_info", new Kwarg("name", deleteHdWalletName));
            boolean isBackup = data.toBoolean();
            if (isBackup) {
                Intent intent = new Intent(WalletManageActivity.this, DeleteWalletActivity.class);
                intent.putExtra("deleteHdWalletName", deleteHdWalletName);
                startActivity(intent);
                finish();
            } else {
                // 没备份提示备份
                new BackupRequireDialog(this).show(getSupportFragmentManager(), "");
            }
        } catch (Exception e) {
            if (!TextUtils.isEmpty(e.getMessage())) {
                mToast(HardWareExceptions.getExceptionString(e));
            }
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onFinish(FinishEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
