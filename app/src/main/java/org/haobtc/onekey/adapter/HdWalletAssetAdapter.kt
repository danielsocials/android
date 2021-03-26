package org.haobtc.onekey.adapter

import android.content.Context
import android.text.TextUtils
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.common.base.Strings
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.BalanceCoinInfo
import org.haobtc.onekey.bean.RemoteImage
import org.haobtc.onekey.business.assetsLogo.AssetsLogo
import org.haobtc.onekey.business.wallet.SystemConfigManager
import org.haobtc.onekey.constant.Vm
import org.haobtc.onekey.utils.CoinDisplayUtils
import java.math.BigDecimal
import java.math.RoundingMode

class HdWalletAssetAdapter(context: Context, data: List<BalanceCoinInfo?>?) : BaseQuickAdapter<BalanceCoinInfo, BaseViewHolder>(R.layout.all_assets_item, data) {
  private val mSystemConfigManager by lazy {
    SystemConfigManager(context.applicationContext)
  }

  override fun convert(helper: BaseViewHolder, item: BalanceCoinInfo) {

    if (Strings.isNullOrEmpty(item.address)) {
      val convertByCoinName = Vm.CoinType.convertByCoinName(item.coin)
      helper.getView<ImageView>(R.id.imageView)
        .setImageDrawable(mContext.getDrawable(AssetsLogo.getLogoResources(convertByCoinName)))
      helper.setText(
        R.id.text_balance,
        CoinDisplayUtils.getCoinPrecisionDisplay(item.balance, convertByCoinName)
      )
    } else {
      RemoteImage(item.icon).intoTarget(helper.getView(R.id.imageView))
      helper.setText(R.id.text_balance, getFormatBalance(item, 4))
    }
    helper.setText(R.id.text_wallet_name, item.coin.toUpperCase())

    val strFiat = if (!TextUtils.isEmpty(item.fiat) && item.fiat == "0") {
      "0.00"
    } else {
      item.fiat.substring(0, item.fiat.indexOf(" "))
    }
    if (!Strings.isNullOrEmpty(strFiat)) {
      helper.setText(R.id.text_fiat, mSystemConfigManager.currentFiatSymbol + " " + strFiat)
    }

  }

  fun getFormatBalance(item: BalanceCoinInfo, num: Int): String {
    return if (!TextUtils.isEmpty(item.balance) && !item.balance.equals("0")) {
      BigDecimal(item.balance)
        .setScale(num, RoundingMode.DOWN)
        .stripTrailingZeros().toPlainString()
    } else {
      "0"
    }
  }

}
