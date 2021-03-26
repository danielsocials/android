package org.haobtc.onekey.bean

import org.haobtc.onekey.constant.Vm

open class WalletAccountInfo(
    val coinType: Vm.CoinType,
    val id: String,
    var name: String,

    @Vm.WalletType
    val walletType: Int,
    val address: String,
    @Deprecated("Transitioned fields")
    val type: String,
    @Vm.HardwareType
    val deviceType: Int = Vm.HardwareType.OneKey,
    val deviceId: String? = null,
) {
  companion object {
    @JvmStatic
    fun convert(type: String,
                addr: String,
                name: String,
                label: String,
                deviceId: String? = null): WalletAccountInfo {
      return WalletAccountInfo(
          Vm.convertCoinType(type),
          name,
          label,
          Vm.convertWalletType(type),
          addr,
          type,
          Vm.HardwareType.OneKey,
          deviceId
      )
    }
  }
}

open class WalletAccountBalanceInfo(
  coinType: Vm.CoinType,
  id: String,
  name: String,

  @Vm.WalletType
  walletType: Int,
  address: String,
  type: String,
  @Vm.HardwareType
  deviceType: Int = Vm.HardwareType.OneKey,
  deviceId: String? = null,
  val hardwareLabel: String? = null,
  var balance: AssetsBalance = defWalletBalance(coinType.defUnit)
) : WalletAccountInfo(
    coinType,
    id,
    name,

    walletType,
    address,
    type,
    deviceType,
    deviceId,
) {
  companion object {
    @JvmStatic
    fun convert(
      type: String,
      addr: String,
      name: String,
      label: String,
      deviceId: String? = null,
      hardwareLabel: String?
    ): WalletAccountBalanceInfo {
      return WalletAccountBalanceInfo(
        Vm.convertCoinType(type),
        name,
        label,
        Vm.convertWalletType(type),
        addr,
        type,
        Vm.HardwareType.OneKey,
        deviceId, hardwareLabel
      )
    }
  }
}
