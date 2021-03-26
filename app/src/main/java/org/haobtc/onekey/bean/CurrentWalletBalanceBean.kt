package org.haobtc.onekey.bean

import com.google.gson.annotations.SerializedName

data class CurrentWalletBalanceBean(
    @SerializedName("all_balance")
    val allBalance: String,
    @SerializedName("wallets")
    val wallets: List<WalletBalanceBean?>?
) {
  data class WalletBalanceBean(
      @SerializedName("balance")
      val balance: String,
      @SerializedName("address")
      val contractAddress: String?,
      @SerializedName("coin")
      val coin: String,
      @SerializedName("fiat")
      val fiat: String
  )
}
