package org.haobtc.onekey.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.utils.MyDialog;

public class TransactionsSettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.tet_addNode)
    TextView tetAddNode;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @BindView(R.id.switch_rbf)
    Switch switchRbf;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @BindView(R.id.switch_noConfirm)
    Switch switchNoConfirm;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @BindView(R.id.switch_find)
    Switch switchFind;

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @BindView(R.id.switch_usdt)
    Switch switchUsdt;

    private SharedPreferences preferences;
    private MyDialog myDialog;

    @Override
    public int getLayoutId() {
        return R.layout.transaction_setting;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(TransactionsSettingActivity.this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        init();
    }

    private void init() {
        boolean setRbf = preferences.getBoolean("set_rbf", false);
        boolean setUnconf = preferences.getBoolean("set_unconf", false);
        boolean setUseChange = preferences.getBoolean("set_use_change", false);
        boolean setPreventDust = preferences.getBoolean("set_prevent_dust", false);
        if (setRbf) {
            switchRbf.setChecked(true);
        } else {
            switchRbf.setChecked(false);
        }
        if (setUnconf) {
            switchNoConfirm.setChecked(true);
        } else {
            switchNoConfirm.setChecked(false);
        }
        if (setUseChange) {
            switchFind.setChecked(true);
        } else {
            switchFind.setChecked(false);
        }
        if (setPreventDust) {
            switchUsdt.setChecked(true);
        } else {
            switchUsdt.setChecked(false);
        }
    }

    @Override
    public void initData() {
        switchChoose();
    }

    private void switchChoose() {
        switchRbf.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        try {
                            PyEnv.sCommands.callAttr("set_rbf", true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_rbf", true).apply();
                        mToast(getString(R.string.set_success));
                    } else {
                        try {
                            PyEnv.sCommands.callAttr("set_rbf", false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_rbf", false).apply();
                    }
                });
        // pay unConfirmed income
        switchNoConfirm.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        try {
                            PyEnv.sCommands.callAttr("set_unconf", false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_unconf", true).apply();
                        mToast(getString(R.string.set_success));
                    } else {
                        try {
                            PyEnv.sCommands.callAttr("set_unconf", true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_unconf", false).apply();
                    }
                });
        // use Give change adrress
        switchFind.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        try {
                            PyEnv.sCommands.callAttr("set_use_change", true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_use_change", true).apply();
                        mToast(getString(R.string.set_success));
                    } else {
                        try {
                            PyEnv.sCommands.callAttr("set_use_change", false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_use_change", false).apply();
                    }
                });
        // Prevent dust attack
        switchUsdt.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    if (isChecked) {
                        try {
                            PyEnv.sCommands.callAttr("set_dust", true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_prevent_dust", true).apply();
                        mToast(getString(R.string.set_success));
                    } else {
                        try {
                            PyEnv.sCommands.callAttr("set_dust", false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putBoolean("set_prevent_dust", false).apply();
                    }
                });
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.tet_addNode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_addNode:
                // Restore default settings
                handler.sendEmptyMessage(1);
                break;
            default:
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler =
            new Handler() {
                @Override
                public void handleMessage(@NonNull Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == 1) {
                        restoreSet();
                    }
                }
            };

    private void restoreSet() {
        try {
            PyEnv.sCommands.callAttr("set_rbf", true);
            PyEnv.sCommands.callAttr("set_unconf", false);
            //            PyEnv.sCommands.callAttr("set_use_change", false);
            //            PyEnv.sCommands.callAttr("set_dust", false);

        } catch (Exception e) {
            e.printStackTrace();
            myDialog.dismiss();
            return;
        }
        preferences.edit().putBoolean("set_rbf", true).apply();
        preferences.edit().putBoolean("set_unconf", true).apply();
        switchRbf.setChecked(true);
        switchNoConfirm.setChecked(true);
        switchFind.setChecked(false);
        switchUsdt.setChecked(false);
        myDialog.dismiss();
        mToast(getString(R.string.recovery_succse));
    }
}
