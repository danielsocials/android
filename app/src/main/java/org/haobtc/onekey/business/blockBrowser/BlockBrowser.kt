package org.haobtc.onekey.business.blockBrowser

interface BlockBrowser {
  fun uniqueTag(): String

  fun showName(): String

  fun url(): String

  fun browseAddressUrl(address: String): String

  fun browseTransactionDetailsUrl(txHash: String): String

  fun browseBlockUrl(block: String): String

  fun browseContractAddressUrl(contractAddress: String, address: String): String
}

interface ETHBlockBrowser : BlockBrowser

interface BTCBlockBrowser : BlockBrowser {
  override fun browseContractAddressUrl(contractAddress: String, address: String): String {
    return BlockBrowserManager.BLOCK_EMPTY_DEFAULT.browseContractAddressUrl(contractAddress, address)
  }
}
