package org.haobtc.onekey.activities.personalwallet;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.event.FixWalletNameEvent;
import org.haobtc.onekey.exception.HardWareExceptions;
import org.haobtc.onekey.manager.PyEnv;

public class FixWalletNameActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.name_edit)
    EditText nameEdit;

    @BindView(R.id.number)
    TextView number;

    @BindView(R.id.btn_next)
    Button btnNext;

    @Override
    public int getLayoutId() {
        return R.layout.activity_fix_wallet_name;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        nameEditStyle();
    }

    private void nameEditStyle() {
        nameEdit.addTextChangedListener(
                new TextWatcher() {
                    CharSequence input;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        input = s;
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        number.setText(String.format(Locale.CHINA, "%d/16", input.length()));
                        if (input.length() > 15) {
                            mToast(getString(R.string.moreinput_text_fixbixinkey));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!TextUtils.isEmpty(s)) {
                            btnNext.setEnabled(true);
                            btnNext.setBackground(getDrawable(R.drawable.button_bk));
                        } else {
                            btnNext.setEnabled(false);
                            btnNext.setBackground(getDrawable(R.drawable.button_bk_grey));
                        }
                    }
                });
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_next:
                reNameWallet();
                break;
            default:
        }
    }

    private void reNameWallet() {
        String walletName = getIntent().getStringExtra("wallet_name");
        try {
            PyEnv.sCommands.callAttr("rename_wallet", walletName, nameEdit.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
            mToast(HardWareExceptions.getExceptionString(e));
            return;
        }
        EventBus.getDefault().post(new FixWalletNameEvent(nameEdit.getText().toString()));
        mToast(getString(R.string.fix_success));
        finish();
    }
}
