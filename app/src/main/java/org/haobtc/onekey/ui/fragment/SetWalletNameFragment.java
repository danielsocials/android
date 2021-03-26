package org.haobtc.onekey.ui.fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.common.base.Strings;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.event.CreateWalletEvent;
import org.haobtc.onekey.ui.base.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * @author liyan
 */
public class SetWalletNameFragment extends BaseFragment implements TextWatcher {

    @BindView(R.id.device_name)
    protected EditText mDeviceNameEditText;
    @BindView(R.id.btn_create)
    protected Button mCreate;

    @Override
    public void init(View view) {
        mDeviceNameEditText.addTextChangedListener(this);
    }

    @OnTextChanged(value = R.id.device_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void onTextChange(Editable s) {
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_set_wallet_name;
    }

    @OnClick(R.id.btn_create)
    public void onViewClicked(View view) {
        if (Strings.isNullOrEmpty(mDeviceNameEditText.getText().toString())) {
            showToast(R.string.name_empty);
            return;
        }
        EventBus.getDefault().post(new CreateWalletEvent(mDeviceNameEditText.getText().toString()));
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
            mDeviceNameEditText.setText(sb.toString());
            mDeviceNameEditText.setSelection(start);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = s.toString().replace(" ", "");
        if (!TextUtils.isEmpty(text)) {
            mCreate.setEnabled(true);
            if (s.length() > 14) {
                Toast.makeText(getActivity(), getString(R.string.name_lenth), Toast.LENGTH_SHORT).show();
            }
        } else {
            mCreate.setEnabled(false);
        }
    }
}
