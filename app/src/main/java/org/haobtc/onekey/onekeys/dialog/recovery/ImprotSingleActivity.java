package org.haobtc.onekey.onekeys.dialog.recovery;

import android.content.Intent;
import android.view.View;

import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.aop.SingleClick;

import butterknife.ButterKnife;
import butterknife.OnClick;

@Deprecated
public class ImprotSingleActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_improt_single;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @SingleClick(value = 1000)
    @OnClick({R.id.img_back, R.id.rel_import_btc, R.id.rel_import_eth})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.rel_import_btc:
                Intent intent = new Intent(ImprotSingleActivity.this, ChooseImportMethodActivity.class);
                intent.putExtra("importType", "BTC");
                startActivity(intent);
                break;
            case R.id.rel_import_eth:
                Intent intent1 = new Intent(ImprotSingleActivity.this, ChooseImportMethodActivity.class);
                intent1.putExtra("importType", "ETH");
                startActivity(intent1);
                break;
        }
    }
}
