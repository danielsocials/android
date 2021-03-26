package org.haobtc.onekey.onekeys;

import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import java.util.ArrayList;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.SupportActivity;
import org.haobtc.onekey.adapter.FragmentMainAdapter;
import org.haobtc.onekey.bean.TabEntity;
import org.haobtc.onekey.business.update.AutoCheckUpdate;
import org.haobtc.onekey.manager.HardwareCallbackHandler;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.homepage.OnBackPressedCallback;
import org.haobtc.onekey.onekeys.homepage.SetOnBackCallback;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.widget.NoScrollViewPager;
import org.haobtc.onekey.ui.widget.tablayout.CommonTabLayout;
import org.haobtc.onekey.ui.widget.tablayout.CustomTabEntity;
import org.haobtc.onekey.ui.widget.tablayout.OnTabSelectListener;
import org.jetbrains.annotations.NotNull;

/** @author liyan */
public class HomeOneKeyActivity extends BaseActivity implements SetOnBackCallback {

    public static final String EXT_RESTART = "ext_restart";

    @BindView(R.id.scrollView)
    NoScrollViewPager scrollViewPager;

    @BindView(R.id.common_tab)
    CommonTabLayout tabLayout;

    @BindView(R.id.customer_service_layout)
    LinearLayout customerServiceLayout;

    private long firstTime = 0;

    private String[] mTitles;
    private int[] mIconUnSelectIds = {
            R.drawable.wallet_normal,
            R.drawable.wallet_tab_swap_un,
            R.drawable.wallet_tab_find_un,
            R.mipmap.mindno
    };
    private int[] mIconSelectIds = {
            R.drawable.wallet_highlight,
            R.drawable.wallet_tab_swap,
            R.drawable.wallet_tab_find,
            R.mipmap.mindyes
    };
    private FragmentMainAdapter fragmentMainAdapter;
    private ArrayList<CustomTabEntity> mTabEntities;
    private AutoCheckUpdate mAutoCheckUpdate;

    private OnBackPressedCallback mOnBackPressedCallback;

    /**
     * * init layout
     *
     * @return
     */
    @Override
    public int getContentViewId() {
        return R.layout.activity_home_onekey;
    }

    @Override
    public boolean needEvents() {
        return false;
    }

    /** init */
    @Override
    public void init() {
        HardwareCallbackHandler callbackHandler = HardwareCallbackHandler.getInstance();
        PyEnv.setHandle(callbackHandler);
        initPage();
        mAutoCheckUpdate = AutoCheckUpdate.getInstance(this);
        getUpdateInfo();
    }

    private void initPage() {
        mTitles =
                new String[] {
                        getString(R.string.wallet),
                        getString(R.string.tab_swap),
                        getString(R.string.tab_found),
                        getString(R.string.mind)
                };
        mTabEntities = new ArrayList<>();
        fragmentMainAdapter = new FragmentMainAdapter(getSupportFragmentManager(), mTitles);
        for (int i = 0; i < mTitles.length; i++) {
            mTabEntities.add(new TabEntity(mTitles[i], mIconSelectIds[i], mIconUnSelectIds[i]));
        }
        scrollViewPager.setAdapter(fragmentMainAdapter);
        scrollViewPager.setOffscreenPageLimit(mTitles.length);
        tabLayout.setTabData(mTabEntities);
        tabLayout.setOnTabSelectListener(
                new OnTabSelectListener() {
                    @Override
                    public void onTabSelect(int position) {
                        scrollViewPager.setCurrentItem(position);
                        customerServiceLayout.setVisibility(
                                position == mTitles.length - 1 ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onTabReselect(int position) {}
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (scrollViewPager != null && scrollViewPager.getCurrentItem() == 2) {
            if (mOnBackPressedCallback != null && mOnBackPressedCallback.onBackPressed()) {
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(HomeOneKeyActivity.this, R.string.dowbke_to_exit, Toast.LENGTH_SHORT)
                        .show();
                firstTime = secondTime;
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void getUpdateInfo() {
        if (getIntent().getBooleanExtra(EXT_RESTART, false)) {
            return;
        }
        mAutoCheckUpdate.checkUpdate(getSupportFragmentManager(), false);
    }

    @OnClick({R.id.customer_service_layout})
    public void onClick(View view) {
        SupportActivity.start(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAutoCheckUpdate.onDestroy();
    }

    @Override
    public void setOnBackPressed(@NotNull OnBackPressedCallback onBackPressed) {
        mOnBackPressedCallback = onBackPressed;
    }
}
