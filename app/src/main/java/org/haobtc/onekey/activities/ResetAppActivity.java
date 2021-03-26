package org.haobtc.onekey.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.lxj.xpopup.XPopup;
import com.tbruyelle.rxpermissions2.RxPermissions;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.constant.FileNameConstant;
import org.haobtc.onekey.constant.PyConstant;
import org.haobtc.onekey.manager.PreferencesManager;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.ui.dialog.ReStartDialog;
import org.haobtc.onekey.ui.dialog.custom.CustomReSetBottomPopup;
import org.haobtc.onekey.utils.NavUtils;
import org.haobtc.onekey.utils.NoLeakHandler;

public class ResetAppActivity extends BaseActivity
        implements OnCheckedChangeListener, NoLeakHandler.HandlerCallback {
    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.text_title)
    TextView textTitle;

    @BindView(R.id.checkbox_ok)
    CheckBox checkboxOk;

    @BindView(R.id.btn_forward)
    Button btnForward;

    protected NoLeakHandler mHandler;
    private final String TAG = "ResetAppActivity";
    private static final int Reset_Code_OK = 100;
    private static final int Reset_Code_FAILURE = 101;
    private RxPermissions rxPermissions;
    private ReStartDialog reStartDialog;

    public static void start(Context context) {
        context.startActivity(new Intent(context, ResetAppActivity.class));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_reset_app;
    }

    @Override
    public void initView() {
        checkboxOk.setOnCheckedChangeListener(this);
    }

    @Override
    public void initData() {
        rxPermissions = new RxPermissions(this);
        mHandler = new NoLeakHandler(this);
    }

    @SingleClick
    @OnClick({R.id.btn_forward, R.id.img_back})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_forward:
                showDialog();
                break;
            case R.id.img_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void showDialog() {
        new XPopup.Builder(mContext)
                .dismissOnTouchOutside(false)
                .isDestroyOnDismiss(true)
                .moveUpToKeyboard(false)
                .asCustom(
                        new CustomReSetBottomPopup(
                                ResetAppActivity.this,
                                () ->
                                        new Thread(
                                                        () -> {
                                                            try {
                                                                PyEnv.sCommands.callAttr(
                                                                        PyConstant.RESET_APP);
                                                                PreferencesManager
                                                                        .getSharedPreferences(
                                                                                mContext,
                                                                                FileNameConstant
                                                                                        .myPreferences)
                                                                        .edit()
                                                                        .clear()
                                                                        .apply();
                                                                PreferencesManager
                                                                        .getSharedPreferences(
                                                                                mContext,
                                                                                FileNameConstant
                                                                                        .Device)
                                                                        .edit()
                                                                        .clear()
                                                                        .apply();
                                                                PreferencesManager
                                                                        .getSharedPreferences(
                                                                                mContext,
                                                                                FileNameConstant
                                                                                        .BLE_INFO)
                                                                        .edit()
                                                                        .clear()
                                                                        .apply();
                                                                PreferencesManager
                                                                        .getSharedPreferences(
                                                                                mContext,
                                                                                Constant.WALLETS)
                                                                        .edit()
                                                                        .clear()
                                                                        .apply();
                                                                PreferencesManager
                                                                        .getSharedPreferences(
                                                                                this,
                                                                                FileNameConstant
                                                                                        .myPreferences)
                                                                        .edit()
                                                                        .putBoolean(
                                                                                Constant.FIRST_RUN,
                                                                                false)
                                                                        .apply();
                                                                mHandler.sendEmptyMessageDelayed(
                                                                        Reset_Code_OK, 200);
                                                            } catch (Exception e) {
                                                                Message message = new Message();
                                                                message.what = Reset_Code_FAILURE;
                                                                message.obj = e.getMessage();
                                                                mHandler.sendMessage(message);
                                                            }
                                                        })
                                                .start(),
                                CustomReSetBottomPopup.resetApp))
                .show();
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
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

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case Reset_Code_OK:
                mToast(getString(R.string.reset_ok));
                NavUtils.reSetApp(mContext);
                break;
            case Reset_Code_FAILURE:
                mToast(getString(R.string.reset_failure));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(null);
        mHandler = null;
    }
}
