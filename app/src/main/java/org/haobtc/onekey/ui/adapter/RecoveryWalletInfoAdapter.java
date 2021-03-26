package org.haobtc.onekey.ui.adapter;

import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.common.base.Strings;
import java.util.List;
import java.util.Locale;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.CreateWalletBean;
import org.haobtc.onekey.business.assetsLogo.AssetsLogo;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.onekeys.walletprocess.OnWalletCheckListener;

/** @Description: 恢复钱包的 WalletInfoBean Adapter @Author: peter Qin */
public class RecoveryWalletInfoAdapter
        extends BaseQuickAdapter<CreateWalletBean.WalletInfoBean, BaseViewHolder> {

    private OnWalletCheckListener mOnWalletCheckListener;

    public RecoveryWalletInfoAdapter(
            int layoutResId,
            @Nullable List<CreateWalletBean.WalletInfoBean> data,
            OnWalletCheckListener mOnWalletCheckListener) {
        super(layoutResId, data);
        this.mOnWalletCheckListener = mOnWalletCheckListener;
    }

    @Override
    protected void convert(BaseViewHolder helper, CreateWalletBean.WalletInfoBean item) {
        ImageView coinImg = helper.getView(R.id.token_btc);
        Vm.CoinType coinType = Vm.convertCoinType(item.getCoinType());
        coinImg.setBackgroundResource(AssetsLogo.getLogoResources(coinType));
        helper.setText(
                R.id.text_wallet_balance,
                String.format(Locale.getDefault(), "0 %s", coinType.defUnit));
        if (!Strings.isNullOrEmpty(item.getLabel())) {
            helper.setText(R.id.text_wallet_name, item.getLabel());
        }
        CheckBox checkBox = helper.getView(R.id.check_wallet);
        if (!Strings.isNullOrEmpty(item.getExist())) {
            if (item.getExist().equals("0")) {
                helper.setGone(R.id.exist_tv, false);
            } else {
                helper.setGone(R.id.exist_tv, true);
                checkBox.setBackgroundResource(R.mipmap.check_not_selected);
            }
        }

        checkBox.setOnClickListener(
                v -> {
                    if (item.getExist().equals("1")) {
                        Toast.makeText(mContext, R.string.hint_wallet_existence, Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        checkBox.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        mOnWalletCheckListener.onCheck(item.getName(), isChecked));
    }
}
