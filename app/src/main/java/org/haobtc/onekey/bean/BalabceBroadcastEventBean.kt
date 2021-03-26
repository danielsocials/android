package org.haobtc.onekey.bean

import com.google.gson.annotations.SerializedName

/**
 * {
 * "coin": "eth",
 * "address": "0x4500614ed5E31A08bD89873eCCe8A96eB1175696",
 * "balance": "5.300040130933210995",
 * "fiat": "51,430.41 CNY",
 * "tokens": [
 * {
 * "coin": "TTT",
 * "address": "0x9a42D6dCD2519E94D037439c447a7a752Ba9F66c",
 * "balance": "10000",
 * "fiat": "0.00 CNY"
 * }
 * ],
 * "sum_fiat": "51,430.41 CNY"
 * }
 */
data class BalanceBroadcastEventBean(
    @SerializedName("address")
    val address: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("coin")
    val coin: String,
    @SerializedName("fiat")
    val fiat: String,
    @SerializedName("sum_fiat")
    val sumFiat: String?,
    @SerializedName("tokens")
    val tokens: List<Token>?
) {
  data class Token(
      @SerializedName("address")
      val address: String,
      @SerializedName("balance")
      val balance: String,
      @SerializedName("coin")
      val coin: String,
      @SerializedName("fiat")
      val fiat: String
  )
}
