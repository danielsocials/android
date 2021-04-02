package org.haobtc.onekey.onekeys.dappbrowser.bean

import com.google.gson.annotations.SerializedName


data class AddEthereumChain(
    @SerializedName("blockExplorerUrls")
    val blockExplorerUrls: List<String>,
    @SerializedName("chainId")
    val chainId: String,
    @SerializedName("chainName")
    val chainName: String,
    @SerializedName("nativeCurrency")
    val nativeCurrency: NativeCurrency,
    @SerializedName("rpcUrls")
    val rpcUrls: List<String>
) {
  var leafPosition: Long = 0
}

data class NativeCurrency(
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("symbol")
    val symbol: String
)
