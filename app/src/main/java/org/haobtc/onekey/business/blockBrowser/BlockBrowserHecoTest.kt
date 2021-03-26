package org.haobtc.onekey.business.blockBrowser

fun getBlockBrowserHecoTest(): Map<String, getBlockBrowserHecoTest> {
  return arrayListOf(
      getBlockBrowserHecoTest.BscScan()
  ).associateBy { it.uniqueTag() }
}

abstract class getBlockBrowserHecoTest(val url: String) : ETHBlockBrowser {
  override fun showName() = url()
  override fun url() = url
  override fun uniqueTag() = "${blockBrowserTag()}HecoTest"
  override fun browseContractAddressUrl(contractAddress: String, address: String) = browseAddressUrl(address)
  abstract fun blockBrowserTag(): String

  class BscScan : getBlockBrowserHecoTest("https://testnet.hecoinfo.com/") {
    override fun browseAddressUrl(address: String) = "${url()}address/$address"
    override fun browseTransactionDetailsUrl(txHash: String) = "${url()}tx/$txHash"
    override fun browseBlockUrl(block: String) = "${url()}block/$block"
    override fun uniqueTag() = BlockBrowserManager.BLOCK_BROWSER_DEFAULT
    override fun blockBrowserTag() = "EtherScanCN"
    override fun browseContractAddressUrl(contractAddress: String, address: String) = "${url()}token/$contractAddress?a=$address"
  }
}
