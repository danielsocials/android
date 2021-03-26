package org.haobtc.onekey.onekeys.dialog.recovery.importmethod;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.event.NameSettedEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImportWalletSetNameActivity extends BaseActivity implements TextWatcher {
    @BindView(R.id.edit_set_wallet_name)
    EditText editSetWalletName;
    @BindView(R.id.btn_import)
    Button btnImport;
    private int purpose;

    public static void gotoImportWalletSetNameActivity (Context context, int purpose) {
        Intent intent = new Intent(context, ImportWalletSetNameActivity.class);
        intent.putExtra(Constant.Purpose, purpose);
        context.startActivity(intent);
    }

    @Override
    public int getLayoutId () {
        return R.layout.activity_import_wallet_set_name;
    }

    @Override
    public void initView () {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        if (getIntent().hasExtra(Constant.Purpose)) {
            purpose = getIntent().getIntExtra(Constant.Purpose, -1);
        }
        editSetWalletName.addTextChangedListener(this);
    }

    @SingleClick(value = 1000)
    @OnClick({R.id.img_back, R.id.btn_import})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_import:
                if (TextUtils.isEmpty(editSetWalletName.getText().toString())) {
                    mToast(getString(R.string.please_input_walletname));
                    return;
                }
                NameSettedEvent nameSettedEvent = new NameSettedEvent(editSetWalletName.getText().toString());
                if (purpose > -1) {
                    nameSettedEvent.addressPurpose = purpose;
                }
                EventBus.getDefault().post(nameSettedEvent);
                finish();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // 禁止EditText输入空格
        if (s.toString().contains(" ")) {
            String[] str = s.toString().split(" ");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < str.length; i++) {
                sb.append(str[i]);
            }
            editSetWalletName.setText(sb.toString());
            editSetWalletName.setSelection(start);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString().replace(" ", "");
        if (!TextUtils.isEmpty(text)) {
            btnImport.setEnabled(true);
            if (s.length() > 14) {
                mToast(getString(R.string.name_lenth));
            }
        } else {
            btnImport.setEnabled(false);
        }
    }

}
