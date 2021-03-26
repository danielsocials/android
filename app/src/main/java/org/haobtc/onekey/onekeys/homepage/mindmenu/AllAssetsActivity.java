package org.haobtc.onekey.onekeys.homepage.mindmenu;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.OnClick;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.layout.util.SmartUtil;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.adapter.WalletAssetAccountAdapter;
import org.haobtc.onekey.bean.AllWalletBalanceInfoDTO;
import org.haobtc.onekey.bean.BalanceCoinInfo;
import org.haobtc.onekey.bean.BalanceInfoDTO;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.bean.TokenList;
import org.haobtc.onekey.business.wallet.BalanceManager;
import org.haobtc.onekey.business.wallet.SystemConfigManager;
import org.haobtc.onekey.business.wallet.TokenManager;
import org.haobtc.onekey.exception.HardWareExceptions;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.viewmodel.AppWalletViewModel;

public class AllAssetsActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.test_all_assets)
    TextView testAllAssets;

    @BindView(R.id.search_et)
    EditText editSearch;

    @BindView(R.id.recl_assets)
    RecyclerView reclAssets;

    @BindView(R.id.tet_None)
    TextView tetNone;

    private List<BalanceInfoDTO> walletInfo;
    private ArrayList<BalanceInfoDTO> mAdapterDatas;
    private SystemConfigManager mSystemConfigManager;
    private BalanceManager mBalanceManager;
    private Disposable mLoadDisposable;
    private AppWalletViewModel mAppWalletViewModel;
    private ArrayList<BalanceInfoDTO> mTempList;
    private WalletAssetAccountAdapter mWalletAssetAccountAdapter;
    private TokenManager mTokenManager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_all_assets;
    }

    @Override
    public void initView() {
        mSystemConfigManager = new SystemConfigManager(this);
        mBalanceManager = new BalanceManager();
        mTokenManager = new TokenManager();
        editSearch.addTextChangedListener(this);
    }

    @Override
    public void initData() {
        mAppWalletViewModel =
                new ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel.class);
        mAdapterDatas = new ArrayList<>();
        mTempList = new ArrayList<>();
        walletInfo = new ArrayList<>();
        HorizontalDividerItemDecoration decoration =
                new HorizontalDividerItemDecoration.Builder(mContext)
                        .color(
                                ResourcesCompat.getColor(
                                        mContext.getResources(),
                                        R.color.color_select_wallet_divider,
                                        mContext.getTheme()))
                        .sizeResId(R.dimen.line_hight)
                        .margin(SmartUtil.dp2px(12F), 0)
                        .build();

        mWalletAssetAccountAdapter =
                new WalletAssetAccountAdapter(
                        mContext, mAppWalletViewModel, decoration, mAdapterDatas);
        reclAssets.setAdapter(mWalletAssetAccountAdapter);
        reclAssets.setNestedScrollingEnabled(false);
        // get all wallet
        getAllWalletList();
    }

    private void getAllWalletList() {
        if (mLoadDisposable != null && !mLoadDisposable.isDisposed()) {
            mLoadDisposable.dispose();
        }
        mLoadDisposable =
                Observable.create(
                                (ObservableOnSubscribe<PyResponse<AllWalletBalanceInfoDTO>>)
                                        emitter -> {
                                            PyResponse<AllWalletBalanceInfoDTO> allWalletBalance =
                                                    PyEnv.getAllWalletBalance();
                                            emitter.onNext(allWalletBalance);
                                            emitter.onComplete();
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> showProgress())
                        .doFinally(this::dismissProgress)
                        .subscribe(
                                allWalletBalanceString -> {
                                    if (Strings.isNullOrEmpty(allWalletBalanceString.getErrors())) {
                                        AllWalletBalanceInfoDTO allWalletBalance =
                                                allWalletBalanceString.getResult();
                                        if (allWalletBalance != null
                                                && allWalletBalance.getWalletInfo().size() > 0) {
                                            tetNone.setVisibility(View.GONE);

                                            String allBalance = allWalletBalance.getAllBalance();
                                            String fiat =
                                                    allBalance.substring(
                                                            0, allBalance.indexOf(" "));
                                            float f = Float.parseFloat(fiat);
                                            DecimalFormat decimalFormat =
                                                    new DecimalFormat(
                                                            "0.00"); // 构造方法的字符格式这里如果小数不足2位,会以0补足.
                                            String money = decimalFormat.format(f);
                                            String currencySymbol =
                                                    mSystemConfigManager.getCurrentFiatSymbol();
                                            testAllAssets.setText(
                                                    String.format("%s %s", currencySymbol, money));
                                            convertAdapterData(allWalletBalance.getWalletInfo());
                                            walletInfo.clear();
                                            walletInfo.addAll(allWalletBalance.getWalletInfo());
                                            mAdapterDatas.addAll(walletInfo);
                                            mWalletAssetAccountAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        tetNone.setVisibility(View.VISIBLE);
                                        mToast(allWalletBalanceString.getErrors());
                                    }
                                },
                                e -> {
                                    tetNone.setVisibility(View.VISIBLE);
                                    mToast(HardWareExceptions.getThrowString(e));
                                    e.printStackTrace();
                                });
    }

    private void convertAdapterData(List<BalanceInfoDTO> walletInfo) {
        if (walletInfo != null && walletInfo.size() > 0) {
            for (BalanceInfoDTO balanceInfoDTO : walletInfo) {
                if (balanceInfoDTO.getWallets() != null && balanceInfoDTO.getWallets().size() > 0) {
                    for (BalanceCoinInfo wallet : balanceInfoDTO.getWallets()) {
                        if (!Strings.isNullOrEmpty(wallet.address)) {
                            TokenList.ERCToken tokenByAddress =
                                    mTokenManager.getTokenByAddress(wallet.address);
                            if (tokenByAddress != null) {
                                wallet.icon = tokenByAddress.logoURI;
                            }
                        }
                    }
                }
            }
        }
        Logger.json(JSON.toJSONString(walletInfo));
    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        mAdapterDatas.clear();
        mTempList.clear();
        if (!TextUtils.isEmpty(s.toString()) && walletInfo != null && walletInfo.size() > 0) {
            mTempList.addAll(getSearchList(s.toString()));
            mAdapterDatas.addAll(mTempList);
        } else {
            mAdapterDatas.addAll(walletInfo);
        }
        mWalletAssetAccountAdapter.notifyDataSetChanged();
    }

    private List<BalanceInfoDTO> getSearchList(String searchText) {
        List<BalanceInfoDTO> list = new ArrayList<>();
        for (BalanceInfoDTO balanceInfoDTO : walletInfo) {
            if (balanceInfoDTO.getName().contains(searchText.toLowerCase())
                    || balanceInfoDTO.getName().contains(searchText.toUpperCase())) {
                list.add(balanceInfoDTO);
            } else {
                if (balanceInfoDTO.getWallets() != null && balanceInfoDTO.getWallets().size() > 0) {
                    List<BalanceCoinInfo> tokenList =
                            getTokenList(balanceInfoDTO.getWallets(), searchText);
                    if (tokenList.size() > 0) {
                        BalanceInfoDTO balanceInfo = new BalanceInfoDTO();
                        balanceInfo.setName(balanceInfoDTO.getName());
                        balanceInfo.setWallets(tokenList);
                        list.add(balanceInfo);
                    }
                }
            }
        }
        return list;
    }

    private List<BalanceCoinInfo> getTokenList(List<BalanceCoinInfo> wallets, String searchText) {
        List<BalanceCoinInfo> list = new ArrayList<>();
        for (BalanceCoinInfo wallet : wallets) {
            if (wallet.getCoin().startsWith(searchText.toLowerCase())
                    || wallet.getCoin().startsWith(searchText.toUpperCase())) {
                list.add(wallet);
            }
        }
        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadDisposable != null && !mLoadDisposable.isDisposed()) {
            mLoadDisposable.dispose();
        }
    }
}
