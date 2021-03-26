package org.haobtc.onekey.adapter;

import androidx.core.content.res.ResourcesCompat;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import org.haobtc.onekey.R;
import org.haobtc.onekey.business.assetsLogo.AssetsLogo;
import org.haobtc.onekey.constant.Vm;

/** @Description: java类作用描述 @Author: peter Qin */
public class SelectAccountCoinItemAdapter extends BaseQuickAdapter<Vm.CoinType, BaseViewHolder> {

    private int selectIndex = 0;

    public SelectAccountCoinItemAdapter() {
        super(R.layout.item_select_coin_logo);
    }

    @Override
    protected void convert(BaseViewHolder helper, Vm.CoinType item) {
        int logoResources;
        if (helper.getLayoutPosition() == selectIndex) {
            logoResources = AssetsLogo.getLogoResources(item);
        } else {
            logoResources = AssetsLogo.getLogoDarkResources(item);
        }
        helper.setImageDrawable(
                R.id.img_coin_type,
                ResourcesCompat.getDrawable(helper.itemView.getResources(), logoResources, null));
    }

    public void selectIndex(int index) {
        selectIndex = index;
        notifyDataSetChanged();
    }
}
