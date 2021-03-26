package org.haobtc.onekey.business.blockBrowser

fun getBlockBrowserHecoMain(): Map<String, getBlockBrowserHecoMain> {
  return arrayListOf(
      getBlockBrowserHecoMain.BscScan()
  ).associateBy { it.uniqueTag() }
}

abstract class getBlockBrowserHecoMain(val url: String) : ETHBlockBrowser {
  override fun showName() = url()
  override fun url() = url
  override fun uniqueTag() = "${blockBrowserTag()}HecoMain"
  override fun browseContractAddressUrl(contractAddress: String, address: String) = browseAddressUrl(address)
  abstract fun blockBrowserTag(): String

  class BscScan : getBlockBrowserHecoMain("https://hecoinfo.com/") {
    override fun browseAddressUrl(address: String) = "${url()}address/$address"
    override fun browseTransactionDetailsUrl(txHash: String) = "${url()}tx/$txHash"
    override fun browseBlockUrl(block: String) = "${url()}block/$block"
    override fun uniqueTag() = BlockBrowserManager.BLOCK_BROWSER_DEFAULT
    override fun blockBrowserTag() = "EtherScanCN"
    override fun browseContractAddressUrl(contractAddress: String, address: String) = "${url()}token/$contractAddress?a=$address"
  }
}
