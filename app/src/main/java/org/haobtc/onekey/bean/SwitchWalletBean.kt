package org.haobtc.onekey.bean

import com.google.gson.annotations.SerializedName


data class SwitchWalletBean(
    @SerializedName("label")
    val label: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("wallets")
    val wallets: List<WalletBean>
) {
  data class WalletBean(
      @SerializedName("address")
      val contractAddress: String,
      @SerializedName("coin")
      val coin: String
  )
}
