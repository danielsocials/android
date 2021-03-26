package org.haobtc.onekey.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.common.base.Strings;
import com.noober.background.drawable.DrawableCreator;
import java.util.List;
import me.jessyan.autosize.utils.AutoSizeUtils;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.WalletInfo;
import org.haobtc.onekey.business.assetsLogo.AssetsLogo;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.utils.ClipboardUtils;

/** @Description: java类作用描述 @Author: peter Qin */
public class WalletListTypeAdapter extends BaseMultiItemQuickAdapter<WalletInfo, BaseViewHolder> {
    public static final int NoWallet = 0;
    public static final int WalletNorMal = 1;
    public static final int AddWallet = 2;
    public static final int AddHardwareWallet = 3;

    public WalletListTypeAdapter(List<WalletInfo> data) {
        super(data);
        addItemType(NoWallet, R.layout.item_no_wallet);
        addItemType(WalletNorMal, R.layout.hd_wallet_item);
        addItemType(AddWallet, R.layout.item_add_wallet);
        addItemType(AddHardwareWallet, R.layout.item_add_hardware_wallet);
    }

    @Override
    protected void convert(BaseViewHolder helper, WalletInfo item) {
        switch (item.itemType) {
            case NoWallet:
                helper.addOnClickListener(R.id.recl_add_hd_wallet);
                helper.addOnClickListener(R.id.recl_recovery_wallet);
                break;
            case WalletNorMal:
                helper.setText(R.id.text_name, item.label);
                ImageView imageView = helper.getView(R.id.hardware_label_img);
                if (!Strings.isNullOrEmpty(item.hardWareLabel)) {
                    imageView.setVisibility(View.VISIBLE);
                } else {
                    imageView.setVisibility(View.GONE);
                }
                RelativeLayout view = helper.getView(R.id.rel_background);
                Drawable build =
                        new DrawableCreator.Builder()
                                .setCornersRadius(AutoSizeUtils.dp2px(mContext, 20F))
                                .setSolidColor(AssetsLogo.getLogoBackgroundColor(item.mCoinType))
                                .build();
                view.setBackground(build);
                if (item.mWalletType == Vm.WalletType.MAIN) {
                    helper.setVisible(R.id.type_layout, true);
                    helper.setText(R.id.text_type, mContext.getString(R.string.main_account));
                } else if (item.mWalletType == Vm.WalletType.HARDWARE) {
                    helper.setVisible(R.id.type_layout, true);
                    if (!Strings.isNullOrEmpty(item.hardWareLabel)) {
                        helper.setText(R.id.text_type, item.hardWareLabel);
                    } else {
                        helper.setText(R.id.text_type, mContext.getString(R.string.hardwares));
                    }
                } else if (item.mWalletType == Vm.WalletType.IMPORT_WATCH) {
                    helper.setVisible(R.id.type_layout, true);
                    helper.setText(R.id.text_type, mContext.getString(R.string.watch));
                } else {
                    helper.setVisible(R.id.type_layout, false);
                }
                TextView textAddr = helper.getView(R.id.text_addr);
                String address = item.addr;
                String front6 = address.substring(0, 6);
                String after6 = address.substring(address.length() - 6);
                textAddr.setText(address);
                helper.setText(R.id.text_address, String.format("%s…%s", front6, after6));
                ImageView copyAddr = helper.getView(R.id.img_copy_addr);
                SharedPreferences preferences =
                        mContext.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                String loadWalletName =
                        preferences.getString(
                                org.haobtc.onekey.constant.Constant.CURRENT_SELECTED_WALLET_NAME,
                                "");
                ImageView chooseView = helper.getView(R.id.img_choose);
                if (loadWalletName.equals(item.name)) {
                    chooseView.setVisibility(View.VISIBLE);
                } else {
                    chooseView.setVisibility(View.GONE);
                }
                copyAddr.setOnClickListener(
                        v -> {
                            // copy text
                            ClipboardUtils.copyText(mContext, textAddr.getText().toString());
                        });
                helper.addOnClickListener(R.id.rel_background);
                break;
            case AddWallet:
                helper.addOnClickListener(R.id.recl_add_wallet);
            case AddHardwareWallet:
                helper.addOnClickListener(R.id.recl_add_hardware_wallet);
                break;
        }
    }
}
