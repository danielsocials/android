package org.haobtc.onekey.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import com.google.common.base.Strings;
import com.lxj.xpopup.XPopup;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.constant.PyConstant;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.databinding.ActivityCreateHardwareWalletNewBinding;
import org.haobtc.onekey.event.ButtonRequestEvent;
import org.haobtc.onekey.event.ChangePinEvent;
import org.haobtc.onekey.event.CreateSuccessEvent;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.HomeOneKeyActivity;
import org.haobtc.onekey.onekeys.walletprocess.OnFinishViewCallBack;
import org.haobtc.onekey.onekeys.walletprocess.SelectChainCoinFragment;
import org.haobtc.onekey.onekeys.walletprocess.SoftWalletNameSettingFragment;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.dialog.custom.SelectWalletTypeDialog;
import org.haobtc.onekey.ui.fragment.DevicePINFragment;
import org.haobtc.onekey.utils.ToastUtils;

public class CreateHardWalletActivity extends BaseActivity
        implements SelectChainCoinFragment.OnSelectCoinTypeCallback,
                SoftWalletNameSettingFragment.OnSetWalletNameCallback,
                OnFinishViewCallBack {

    private ActivityCreateHardwareWalletNewBinding mBinding;
    private NavController mNavController;
    private Vm.CoinType mCoinType;
    private String mBtcWalletType = null;

    public static void start(Context context) {
        context.startActivity(new Intent(context, CreateHardWalletActivity.class));
    }

    @Override
    public void init() {

        NavHostFragment navHostFragment =
                (NavHostFragment)
                        getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        mNavController = navHostFragment.getNavController();
    }

    @Override
    public int getContentViewId() {
        return 0;
    }

    @Override
    public boolean enableViewBinding() {
        return true;
    }

    @Nullable
    @Override
    public View getLayoutView() {
        mBinding = ActivityCreateHardwareWalletNewBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    public void onSelectCoinType(Vm.CoinType coinType) {
        mCoinType = coinType;
        if (mCoinType == Vm.CoinType.BTC) {
            new XPopup.Builder(mContext)
                    .asCustom(
                            new SelectWalletTypeDialog(
                                    mContext,
                                    mode -> {
                                        switch (mode) {
                                            case SelectWalletTypeDialog.RecommendType:
                                                mBtcWalletType =
                                                        PyConstant.ADDRESS_TYPE_P2SH_P2WPKH;
                                                break;
                                            case SelectWalletTypeDialog.NativeType:
                                                mBtcWalletType = PyConstant.ADDRESS_TYPE_P2WPKH;
                                                break;
                                            case SelectWalletTypeDialog.NormalType:
                                                mBtcWalletType = PyConstant.ADDRESS_TYPE_P2PKH;
                                                break;
                                        }
                                        mNavController.navigate(R.id.set_name_fragment);
                                    }))
                    .show();
        } else if (mCoinType.chainType.equalsIgnoreCase(Vm.CoinType.ETH.chainType)) {
            mNavController.navigate(R.id.set_name_fragment);
        } else {
            ToastUtils.toast(getString(R.string.support_less_promote));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreateWalletSuccess(CreateSuccessEvent event) {
        startActivity(new Intent(this, HomeOneKeyActivity.class));
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangePin(ChangePinEvent event) {
        // 回写PIN码
        PyEnv.setPin(event.getPinNew());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        if (PyConstant.PIN_CURRENT == event.getType()) {
            startFragment(new DevicePINFragment(PyConstant.PIN_CURRENT));
        }
    }

    @Override
    public void onFinishView() {
        if (!mNavController.navigateUp()) {
            finish();
        }
    }

    @Override
    public boolean needEvents() {
        return true;
    }

    @Override
    public void onSetWalletName(String name) {
        if (mCoinType == Vm.CoinType.BTC) {

        } else if (mCoinType.chainType.equalsIgnoreCase(Vm.CoinType.ETH.chainType)) {
            mBtcWalletType = "";
        }
        Disposable disposable =
                getXpubObserVable(mCoinType.callFlag, mBtcWalletType)
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(show -> showProgress())
                        .flatMap(
                                (Function<PyResponse<String>, ObservableSource<PyResponse<String>>>)
                                        response -> {
                                            String xpub = response.getResult();
                                            String xpubs =
                                                    "[[\""
                                                            + xpub
                                                            + "\", \""
                                                            + PyEnv.currentHwFeatures.getSerialNum()
                                                            + "\"]]";
                                            return getCreateObservable(
                                                    name, xpubs, mCoinType.callFlag);
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doFinally(this::dismissProgress)
                        .subscribe(
                                result -> {
                                    if (!Strings.isNullOrEmpty(result.getErrors())) {
                                        mToast(result.getErrors());
                                    }
                                },
                                error -> dismissProgress());
        mCompositeDisposable.add(disposable);
    }

    private Observable<PyResponse<String>> getXpubObserVable(String coinType, String addressType) {
        return Observable.create(
                emitter -> {
                    PyResponse<String> response =
                            PyEnv.createEthHwDerivedWallet(
                                    MyApplication.getInstance().getDeviceWay(),
                                    coinType,
                                    addressType);
                    emitter.onNext(response);
                    emitter.onComplete();
                });
    }

    private Observable<PyResponse<String>> getCreateObservable(
            String name, String xpub, String coinType) {
        return Observable.create(
                emitter -> {
                    PyResponse<String> response = PyEnv.createWalletNew(name, 1, 1, xpub, coinType);
                    emitter.onNext(response);
                    emitter.onComplete();
                });
    }

    @Override
    protected void onDestroy() {
        PyEnv.cancelPinInput();
        super.onDestroy();
    }
}
