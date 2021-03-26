package org.haobtc.onekey.onekeys.walletprocess;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.scwang.smartrefresh.layout.util.SmartUtil;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;
import java.util.Arrays;
import java.util.List;
import org.haobtc.onekey.R;
import org.haobtc.onekey.adapter.SelectChainListAdapter;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.databinding.FragmentSelectorChainCoinBinding;
import org.haobtc.onekey.onekeys.walletprocess.createfasthd.CreateFastHDSoftWalletProvider;
import org.haobtc.onekey.onekeys.walletprocess.createsoft.CreateSoftWalletProvider;
import org.haobtc.onekey.onekeys.walletprocess.importsoft.ImportSoftWalletProvider;
import org.haobtc.onekey.ui.base.BaseFragment;

/**
 * 选择币种页面
 *
 * @author Onekey@QuincySx
 * @create 2021-01-15 5:13 PM
 */
public class SelectChainCoinFragment extends BaseFragment implements View.OnClickListener {

    private FragmentSelectorChainCoinBinding mBindingView;

    private ImportSoftWalletProvider mImportSoftWalletProvider = null;
    private CreateSoftWalletProvider mCreateSoftWalletProvider = null;
    private CreateFastHDSoftWalletProvider mCreateFastHDSoftWalletProvider = null;

    private OnSelectCoinTypeCallback mOnSelectCoinTypeCallback = null;
    private OnFinishViewCallBack mOnFinishViewCallBack = null;
    private SelectChainListAdapter mChainListAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectCoinTypeCallback) {
            mOnSelectCoinTypeCallback = (OnSelectCoinTypeCallback) context;
        }
        if (context instanceof OnFinishViewCallBack) {
            mOnFinishViewCallBack = (OnFinishViewCallBack) context;
        }
        if (context instanceof ImportSoftWalletProvider) {
            mImportSoftWalletProvider = (ImportSoftWalletProvider) context;
        }
        if (context instanceof CreateSoftWalletProvider) {
            mCreateSoftWalletProvider = (CreateSoftWalletProvider) context;
        }
        if (context instanceof CreateFastHDSoftWalletProvider) {
            mCreateFastHDSoftWalletProvider = (CreateFastHDSoftWalletProvider) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBindingView = FragmentSelectorChainCoinBinding.inflate(getLayoutInflater());
        init(mBindingView.getRoot());
        return mBindingView.getRoot();
    }

    @Override
    public void init(View view) {}

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBindingView.imgBack.setOnClickListener(this);
        mBindingView.relBtc.setOnClickListener(this);
        mBindingView.relEth.setOnClickListener(this);
        mChainListAdapter = new SelectChainListAdapter(Arrays.asList(Vm.CoinType.values()));
        mBindingView.chainRecycler.setAdapter(mChainListAdapter);
        if (mImportSoftWalletProvider != null && !mImportSoftWalletProvider.isImportHDWallet()) {
            mBindingView.tvTitleContent.setText(R.string.import_wallet);
        } else {
            mBindingView.tvTitleContent.setText(R.string.choose_amount);
        }

        HorizontalDividerItemDecoration build =
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(
                                ResourcesCompat.getColor(
                                        getResources(),
                                        R.color.color_select_wallet_divider,
                                        getActivity().getTheme()))
                        .sizeResId(R.dimen.line_hight)
                        .margin(SmartUtil.dp2px(20F), SmartUtil.dp2px(20F))
                        .build();
        mBindingView.chainRecycler.addItemDecoration(build);
        mChainListAdapter.setOnItemClickListener(
                new BaseQuickAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        List<Vm.CoinType> list = adapter.getData();
                        dealSelect(list.get(position));
                    }
                });
    }

    @Override
    public int getContentViewId() {
        return 0;
    }

    @SingleClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_back:
                if (mOnFinishViewCallBack != null) {
                    mOnFinishViewCallBack.onFinishView();
                }
                break;
        }
    }

    private void dealSelect(Vm.CoinType type) {
        if (mOnSelectCoinTypeCallback != null) {
            mOnSelectCoinTypeCallback.onSelectCoinType(type);
        }
    }

    public interface OnSelectCoinTypeCallback {
        void onSelectCoinType(Vm.CoinType coinType);
    }
}
