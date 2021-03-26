package org.haobtc.onekey.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.google.common.base.Strings;
import com.noober.background.drawable.DrawableCreator;
import java.util.ArrayList;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.WalletAccountBalanceInfo;
import org.haobtc.onekey.business.assetsLogo.AssetsLogo;
import org.haobtc.onekey.constant.Vm;

/** @Description: java类作用描述 @Author: peter Qin */
public class SelectAccountAdapter
        extends BaseMultiItemQuickAdapter<
                SelectAccountAdapter.MultiWalletAccountBalanceInfo, BaseViewHolder> {

    public static final int NoWallet = 0;
    public static final int WalletNormal = 1;

    public SelectAccountAdapter() {
        super(new ArrayList<>());
        addItemType(NoWallet, R.layout.item_add_wallet_concise);
        addItemType(WalletNormal, R.layout.item_select_account);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiWalletAccountBalanceInfo wrapItem) {
        if (wrapItem.itemType == NoWallet) {
            helper.addOnClickListener(R.id.recl_add_wallet);
            return;
        }
        WalletAccountBalanceInfo item = wrapItem.data;
        helper.setText(R.id.text_name, item.getName());
        View view = helper.getView(R.id.layout_background);
        if (!Strings.isNullOrEmpty(item.getHardwareLabel())) {
            helper.setGone(R.id.hardware_label_img, true);
        } else {
            helper.setGone(R.id.hardware_label_img, false);
        }
        Vm.CoinType coinType = item.getCoinType();
        int walletType = item.getWalletType();

        Drawable build =
                new DrawableCreator.Builder()
                        .setCornersRadius(AutoSizeUtils.dp2px(mContext, 13F))
                        .setSolidColor(AssetsLogo.getLogoBackgroundColor(coinType))
                        .build();
        view.setBackground(build);

        if (walletType == Vm.WalletType.MAIN) {
            helper.setGone(R.id.type_layout, true);
            helper.setText(R.id.text_type, mContext.getString(R.string.main_account));
        } else if (walletType == Vm.WalletType.HARDWARE) {
            helper.setGone(R.id.type_layout, true);
            helper.setText(R.id.text_type, item.getHardwareLabel());
        } else if (walletType == Vm.WalletType.IMPORT_WATCH) {
            helper.setGone(R.id.type_layout, true);
            helper.setText(R.id.text_type, mContext.getString(R.string.watch));
        } else {
            helper.setGone(R.id.type_layout, false);
        }
        String address = item.getAddress();
        String front6 = address.substring(0, 6);
        String after6 = address.substring(address.length() - 6);
        helper.setText(R.id.text_address, String.format("%s…%s", front6, after6));
        SharedPreferences preferences =
                mContext.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        String loadWalletName =
                preferences.getString(
                        org.haobtc.onekey.constant.Constant.CURRENT_SELECTED_WALLET_NAME, "");
        ImageView chooseView = helper.getView(R.id.img_choose);
        if (loadWalletName.equals(item.getId())) {
            chooseView.setVisibility(View.VISIBLE);
        } else {
            chooseView.setVisibility(View.GONE);
        }
        helper.setText(R.id.tv_amount, item.getBalance().getBalanceFormat(8));
        helper.addOnClickListener(R.id.layout_background);
    }

    public static class MultiWalletAccountBalanceInfo implements MultiItemEntity {

        public MultiWalletAccountBalanceInfo(WalletAccountBalanceInfo data) {
            this.itemType = WalletNormal;
            this.data = data;
        }

        public MultiWalletAccountBalanceInfo(int itemType) {
            this.itemType = itemType;
        }

        public int itemType;
        public WalletAccountBalanceInfo data;

        @Override
        public int getItemType() {
            return itemType;
        }
    }
}
