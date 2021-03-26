package org.haobtc.onekey.activities.personalwallet.hidewallet;

import static org.haobtc.onekey.activities.service.CommunicationModeSelector.isNFC;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.chaquo.python.Kwarg;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.activities.service.NfcNotifyHelper;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.event.CheckHideWalletEvent;
import org.haobtc.onekey.event.FinishEvent;
import org.haobtc.onekey.event.HideInputPassFinishEvent;
import org.haobtc.onekey.event.OperationTimeoutEvent;
import org.haobtc.onekey.event.PinEvent;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.utils.Global;

public class HideWalletSetPassActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.editNewPass)
    EditText editNewPass;

    @BindView(R.id.bn_next)
    Button bnNext;

    public static final String TAG =
            "org.haobtc.onekey.activities.personalwallet.hidewallet.HideWalletSetPassActivity";

    @Override
    public int getLayoutId() {
        return R.layout.activity_hide_wallet_set_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void timeout(OperationTimeoutEvent event) {
        Toast.makeText(this, getString(R.string.passphrase_timeout), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void initData() {}

    @SingleClick
    @OnClick({R.id.img_back, R.id.bn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Global.py
                        .getModule("trezorlib.customer_ui")
                        .get("CustomerUI")
                        .put("user_cancel", 1);
                finish();
                break;
            case R.id.bn_next:
                String strNewpass = editNewPass.getText().toString();
                if (TextUtils.isEmpty(strNewpass)) {
                    mToast(getString(R.string.please_input_pass));
                    return;
                }
                if (!isNFC) {
                    EventBus.getDefault().post(new PinEvent("", strNewpass));
                    finish();
                } else {
                    Intent intent = new Intent(this, NfcNotifyHelper.class);
                    intent.putExtra("tag", "Passphrase");
                    intent.putExtra("passphrase", strNewpass);
                    startActivity(intent);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(CheckHideWalletEvent updataHint) {
        String xpub = updataHint.getXpub();
        String deviceId = updataHint.getDeviceId();
        String strXpub = "[[\"" + xpub + "\",\"" + deviceId + "\"]]";
        try {
            PyEnv.sCommands.callAttr(
                    "import_create_hw_wallet", "隐藏钱包", 1, 1, strXpub, new Kwarg("hide_type", true));
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            if ("BaseException: file already exists at path".equals(message)) {
                Toast.makeText(this, getString(R.string.changewalletname), Toast.LENGTH_SHORT)
                        .show();
            } else {
                assert message != null;
                if (message.contains("The same xpubs have create wallet")) {
                    String haveWalletName = message.substring(message.indexOf("name=") + 5);
                    Toast.makeText(
                                    this,
                                    getString(R.string.xpub_have_wallet) + haveWalletName,
                                    Toast.LENGTH_SHORT)
                            .show();
                }
            }
            return;
        }
        Intent intent = new Intent(this, CheckHideWalletActivity.class);
        startActivity(intent);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinish(FinishEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(HideInputPassFinishEvent event) {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
