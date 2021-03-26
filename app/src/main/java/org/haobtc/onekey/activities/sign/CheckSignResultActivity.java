package org.haobtc.onekey.activities.sign;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.utils.Utils;

public class CheckSignResultActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.imgStatus)
    ImageView imgStatus;

    @BindView(R.id.testStatus)
    TextView testStatus;

    @BindView(R.id.testContent)
    TextView testContent;

    private boolean verify;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_sign_result;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        verify = intent.getBooleanExtra("verify", false);
        if (!verify) {
            imgStatus.setImageDrawable(getDrawable(R.drawable.check_sign_no));
            testStatus.setText(getString(R.string.verify_failed));
            testStatus.setTextColor(getColor(R.color.text_ten));
            testContent.setText(getString(R.string.agreement_msg_no));
        }
    }

    @Override
    public void initData() {}

    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
            case R.id.btn_finish:
                finishActivity();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    private void finishActivity() {
        finish();
        if (verify) {
            Utils.finishActivity(CheckSignActivity.class);
        }
    }
}
