package org.haobtc.onekey.activities.settings;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.chaquo.python.PyObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.adapter.QuetationChooseAdapter;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.bean.CNYBean;
import org.haobtc.onekey.event.FirstEvent;
import org.haobtc.onekey.manager.PyEnv;

public class QuotationServerActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.recl_Quetation)
    RecyclerView reclQuetation;

    private ArrayList<CNYBean> exchangeList;
    private int exChange;
    private SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.activity_quetation_choose;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        exChange = preferences.getInt("exChange", 0);
    }

    @Override
    public void initData() {
        reclQuetation.setNestedScrollingEnabled(false);
        exchangeList = new ArrayList<>();
        // get exchanges list
        getExchangelist();
    }

    private void getExchangelist() {
        PyObject get_exchanges = null;
        try {
            get_exchanges = PyEnv.sCommands.callAttr("get_exchanges");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.i("get_exchanges", "getExchangelist: " + get_exchanges);
        if (get_exchanges != null) {
            String content = get_exchanges.toString();
            String unit = content.replaceAll("\"", "");
            String[] pathArr = (unit.substring(1, unit.length() - 1)).split(",");
            List<String> pathList = Arrays.asList(pathArr);

            for (int i = 0; i < pathList.size(); i++) {
                CNYBean cnyBean = new CNYBean(pathList.get(i), false);
                exchangeList.add(cnyBean);
            }
        }
        QuetationChooseAdapter quetationChooseAdapter =
                new QuetationChooseAdapter(QuotationServerActivity.this, exchangeList, exChange);
        reclQuetation.setAdapter(quetationChooseAdapter);
        quetationChooseAdapter.setOnLisennorClick(
                new QuetationChooseAdapter.onLisennorClick() {
                    @Override
                    public void itemClick(int pos) {
                        String exchangeName = exchangeList.get(pos).getName();
                        try {
                            PyEnv.sCommands.callAttr("set_exchange", exchangeName);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        preferences.edit().putInt("exChange", pos).apply();
                        preferences.edit().putString("exchangeName", exchangeName).apply();
                        EventBus.getDefault().post(new FirstEvent("defaultServer"));
                    }
                });
    }

    @SingleClick
    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            default:
        }
    }
}
