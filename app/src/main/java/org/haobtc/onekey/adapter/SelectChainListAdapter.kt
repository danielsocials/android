package org.haobtc.onekey.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.LocalImage
import org.haobtc.onekey.business.assetsLogo.AssetsLogo.getLogoResources
import org.haobtc.onekey.constant.Vm

/**
 *
 * @Description:     java类作用描述
 * @Author:         peter Qin
 *
 */
class SelectChainListAdapter(data: List<Vm.CoinType>) :
  BaseQuickAdapter<Vm.CoinType, BaseViewHolder>(R.layout.item_chain_list, data) {


  override fun convert(helper: BaseViewHolder, item: Vm.CoinType?) {
    item?.chainType.let {
      LocalImage(getLogoResources(item)).intoTarget(helper.getView(R.id.chain_img))
    }
    item?.coinName?.let {
      helper.setText(R.id.chain_name, item.coinName)
    }
  }


}
