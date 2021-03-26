package org.haobtc.onekey.business.blockBrowser

import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.constant.Vm

object BlockBrowserManager {
  private const val SAVE_UNIQUE_TAG = "setBlock"
  const val BLOCK_BROWSER_DEFAULT = "Default"

  val BLOCK_EMPTY_DEFAULT = object : BlockBrowser {
    override fun uniqueTag() = BLOCK_BROWSER_DEFAULT

    override fun showName() = "Onekey Test"

    override fun url() = "https://onekey.so/"

    override fun browseAddressUrl(address: String) = "https://onekey.so/"

    override fun browseTransactionDetailsUrl(txHash: String) = "https://onekey.so/"

    override fun browseBlockUrl(block: String) = "https://onekey.so/"

    override fun browseContractAddressUrl(contractAddress: String, address: String) = "https://onekey.so/"
  }

  private val mPreferences by lazy {
    MyApplication.getInstance().getSharedPreferences("Preferences", android.content.Context.MODE_PRIVATE)
  }

  private fun getSaveTag(coinType: Vm.CoinType): String {
    return "$SAVE_UNIQUE_TAG${coinType.name}"
  }

  fun browseAddressUrl(coinType: Vm.CoinType, address: String): String {
    return getCurrentBlockBrowser(coinType).browseAddressUrl(address)
  }

  fun browseAddressUrl(coinType: Vm.CoinType, contractAddress: String, address: String): String {
    return getCurrentBlockBrowser(coinType).browseContractAddressUrl(contractAddress, address)
  }

  fun browseTransactionDetailsUrl(coinType: Vm.CoinType, txHash: String): String {
    return getCurrentBlockBrowser(coinType).browseTransactionDetailsUrl(txHash)
  }

  fun browseBlockUrl(coinType: Vm.CoinType, block: String): String {
    return getCurrentBlockBrowser(coinType).browseBlockUrl(block)
  }

  fun setBlockBrowser(coinType: Vm.CoinType, uniqueTag: String) {
    mPreferences.edit().putString(getSaveTag(coinType), uniqueTag).apply()
  }

  fun getCurrentBlockBrowser(coinType: Vm.CoinType): BlockBrowser {
    val blockBrowserList = getBlockBrowserList(coinType)
    val key = mPreferences.getString(getSaveTag(coinType), BLOCK_BROWSER_DEFAULT)
    return blockBrowserList.getOrElse(key ?: BLOCK_BROWSER_DEFAULT) { BLOCK_EMPTY_DEFAULT }
  }

  fun getBlockBrowserList(coinType: Vm.CoinType): Map<String, BlockBrowser> {
    return when {
      coinType == Vm.CoinType.ETH && Vm.getEthNetwork() == Vm.PyenvETHNetworkType.MainNet -> {
        getBlockBrowserEthMain()
      }
      coinType == Vm.CoinType.ETH && Vm.getEthNetwork() == Vm.PyenvETHNetworkType.TestNet -> {
        getBlockBrowserEthRopsten()
      }
      coinType == Vm.CoinType.BTC && Vm.getEthNetwork() == Vm.BTCNetworkType.MainNet -> {
        getBlockBrowserBTCMain()
      }
      coinType == Vm.CoinType.BTC && Vm.getEthNetwork() == Vm.BTCNetworkType.TestNet -> {
        getBlockBrowserBTCTest()
      }
      coinType == Vm.CoinType.BSC && Vm.getEthNetwork() == Vm.PyenvETHNetworkType.MainNet -> {
        getBlockBrowserBscMain()
      }
      coinType == Vm.CoinType.BSC && Vm.getEthNetwork() == Vm.PyenvETHNetworkType.TestNet -> {
        getBlockBrowserBscTest()
      }
      coinType == Vm.CoinType.HECO && Vm.getEthNetwork() == Vm.PyenvETHNetworkType.MainNet -> {
        getBlockBrowserHecoMain()
      }
      coinType == Vm.CoinType.HECO && Vm.getEthNetwork() == Vm.PyenvETHNetworkType.TestNet -> {
        getBlockBrowserHecoTest()
      }
      else -> mapOf(Pair(BLOCK_BROWSER_DEFAULT, BLOCK_EMPTY_DEFAULT))
    }
  }
}
