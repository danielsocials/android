package org.haobtc.onekey.ui.base;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.business.language.LanguageManager;
import org.haobtc.onekey.manager.ActivityManager;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.dappbrowser.ui.BaseAlertBottomDialog;
import org.haobtc.onekey.utils.MyDialog;

/** @author liyan */
public abstract class BaseActivity extends AppCompatActivity implements IBaseView {

    public Fragment mCurrentFragment;
    public Context mContext;

    @BindView(R.id.title)
    @Nullable
    protected TextView mTitle;

    public Toolbar toolbar;
    public TextView leftTitle;
    public TextView rightTitle;
    private MyDialog mProgressDialog;
    protected final CompositeDisposable mCompositeDisposable = new CompositeDisposable();
    private Unbinder mBind;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        applyOverrideConfiguration(new Configuration());
    }

    @Override
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        Configuration customLanguageConfiguration =
                LanguageManager.getInstance().updateConfigurationIfSupported(overrideConfiguration);
        super.applyOverrideConfiguration(customLanguageConfiguration);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setCustomDensity();
        super.onCreate(savedInstanceState);
        PyEnv.init();
        ActivityManager.getInstance().addActivity(this);
        mContext = this;

        if (showToolBar()) {
            setContentView(R.layout.ac_base_fit_layout);
            initDefaultView();
            toolbar.setVisibility(View.VISIBLE);
        } else {
            setContentView(R.layout.ac_base_not_fit_layout);
            initDefaultView();
            toolbar.setVisibility(View.GONE);
        }
        mBind = ButterKnife.bind(this);
        if (requireSecure()) {
            requestSecure();
        }
        diyWindow();
        init();
        if (needEvents()) {
            EventBus.getDefault().register(this);
        }
    }

    private void initDefaultView() {
        toolbar = findViewById(R.id.toolbar_base);
        leftTitle = findViewById(R.id.left_tv);
        rightTitle = findViewById(R.id.right_tv);
        FrameLayout container = findViewById(R.id.fl_activity_child_container);
        toolbar.setNavigationOnClickListener(view -> leftBackCLicked());

        View childView = null;
        if (enableViewBinding()) {
            View layoutView = getLayoutView();
            if (layoutView == null) {
                throw new RuntimeException("???????????? getLayoutView ?????????");
            }
            childView = layoutView;
        } else {
            childView = LayoutInflater.from(this).inflate(getContentViewId(), null);
        }
        if (childView != null) {
            container.addView(childView);
        }
    }

    /** init */
    public abstract void init();

    @Override
    public Activity getActivity() {
        return this;
    }

    public boolean enableViewBinding() {
        return false;
    }

    @Nullable
    public View getLayoutView() {
        return null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {
                hideKeyboard();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void startFragment(Fragment fragment) {
        hideKeyboard();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (mCurrentFragment == null) {
            fragmentTransaction.add(R.id.container, fragment).commitAllowingStateLoss();
            mCurrentFragment = fragment;
        }
        if (mCurrentFragment != fragment) {
            if (!fragment.isAdded()) {
                fragmentTransaction
                        .hide(mCurrentFragment)
                        .add(R.id.container, fragment)
                        .commitAllowingStateLoss();
            } else {
                fragmentTransaction.hide(mCurrentFragment).show(fragment).commitAllowingStateLoss();
            }
            mCurrentFragment = fragment;
        }
    }

    /** Set transparent immersion bar : white backgrand black text */
    public void diyWindow() {
        // other one write
        Window window = getWindow();
        if (keepScreenOn()) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        window.setStatusBarColor(Color.TRANSPARENT);
        setNavigationBarColor(Color.WHITE);
        window.getDecorView()
                .setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
    }

    // ?????????????????????????????????
    public void setStatusBarColor(int colorId) {
        // other one write
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().setStatusBarColor(colorId);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
    }

    public void setNavigationBarColor(int colorId) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            getWindow().setNavigationBarColor(colorId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBind != null) {
            mBind.unbind();
        }
        mCompositeDisposable.dispose();
        if (needEvents()) {
            EventBus.getDefault().unregister(this);
        }
    }

    /** ???????????? */
    public void updateTitle(int title) {
        runOnUiThread(
                () -> {
                    if (mTitle != null) {
                        mTitle.setText(title);
                    }
                });
    }

    /** ????????????????????? */
    private void requestSecure() {
        getWindow()
                .setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE);
    }

    /** ????????????????????????????????? */
    public boolean requireSecure() {
        return false;
    }

    /** ????????????eventBus??????????????? */
    public boolean needEvents() {
        return false;
    }

    /** ????????????????????????????????? */
    public boolean keepScreenOn() {
        return false;
    }

    // toast short
    public void mToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_SHORT);
        toast.setText(str);
        toast.show();
    }

    // toast long
    public void mlToast(String str) {
        Toast toast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        toast.setText(str);
        toast.show();
    }

    //  ??????true ??????????????? Base??? ToolBar???
    protected boolean showToolBar() {
        return false;
    }

    // ????????????tooBar ???????????????
    public void setLeftTitle(int textId) {
        if (leftTitle != null) {
            leftTitle.setText(getString(textId));
        }
    }

    // ????????????tooBar ??????????????????
    public void setRightTitle(String titleText) {
        if (rightTitle != null) {
            rightTitle.setText(titleText);
        }
    }

    // ??????BaseActivity?????????ToolBar??????????????????????????????????????????????????????????????????
    protected void leftBackCLicked() {
        finish();
    }

    public void showProgress() {
        runOnUiThread(
                () -> {
                    dismissProgress();
                    mProgressDialog = MyDialog.showDialog(this);
                    mProgressDialog.show();
                    mProgressDialog.onTouchOutside(false);
                });
    }

    public void dismissProgress() {
        runOnUiThread(
                () -> {
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
                        mProgressDialog.dismiss();
                        mProgressDialog = null;
                    }
                });
    }

    // Unified handling connect reset hardware  with BusinessAsyncTask
    public void dealWithHardConnect(String errorMsg, String deviceName) {
        if (errorMsg.contains(getString(R.string.cannot_pair))) {
            new BaseAlertBottomDialog.Builder(mContext)
                    .setTitle(R.string.fail)
                    .setMessage(
                            String.format(
                                    Locale.getDefault(),
                                    getString(R.string.cannot_pair_device),
                                    deviceName))
                    .build()
                    .showNow();
        } else {
            showToast(errorMsg);
        }
    }
}
