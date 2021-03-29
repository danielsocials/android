package org.haobtc.onekey.onekeys.homepage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import org.haobtc.onekey.R
import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.constant.Vm
import org.haobtc.onekey.databinding.FragmentTabSwapBinding
import org.haobtc.onekey.onekeys.dappbrowser.ui.BaseAlertBottomDialog
import org.haobtc.onekey.onekeys.dappbrowser.ui.DappBrowserFragment
import org.haobtc.onekey.ui.base.BaseFragment
import org.haobtc.onekey.ui.dialog.SelectAccountBottomSheetDialog
import org.haobtc.onekey.viewmodel.AppWalletViewModel


class SwapFragment : BaseFragment() {

  private lateinit var mBinding: FragmentTabSwapBinding
  private var mAppWalletViewModel: AppWalletViewModel? = null
  private var dappBrowserFragment: DappBrowserFragment? = null

  override fun getContentViewId() = 0

  override fun enableViewBinding() = true

  override fun getLayoutView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    mBinding = FragmentTabSwapBinding.inflate(inflater, container, false)
    return mBinding.root
  }

  override fun init(view: View) {}

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    dappBrowserFragment = DappBrowserFragment.start("https://swap.onekey.so/#/swap", browserMode = false)

    childFragmentManager.beginTransaction()
        .add(R.id.layout_fragment_container, dappBrowserFragment!!)
        .commitAllowingStateLoss()

    mBinding.layoutFragmentContainer
    mAppWalletViewModel = ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel::class.java)
  }

  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser) {
      checkAccount()
      dappBrowserFragment?.registerEventBus()
    } else {
      dappBrowserFragment?.unregisterEventBus()
    }
  }

  private fun checkAccount(): Boolean {
    mAppWalletViewModel?.currentWalletAccountInfo?.value?.let { currentWalletAccountInfo ->
      if (currentWalletAccountInfo.coinType != Vm.CoinType.ETH
          && currentWalletAccountInfo.coinType != Vm.CoinType.BSC) {
        BaseAlertBottomDialog(requireContext()).apply {
          setTitle(getString(R.string.title_account_unavailable,
              "${Vm.CoinType.ETH.coinName}/${Vm.CoinType.BSC.coinName}"))
          setMessage(getString(R.string.hint_onekey_swap_account_unavailable_content,
              "${Vm.CoinType.ETH.coinName}/${Vm.CoinType.BSC.coinName}"))
          setPrimaryButtonListener {
            dismiss()
            SelectAccountBottomSheetDialog.newInstance(arrayListOf(Vm.CoinType.ETH, Vm.CoinType.BSC))
                .setOnSelectAccountCallback { dappBrowserFragment?.refreshEvent() }
                .show(
                    parentFragmentManager,
                    "SelectAccount")
          }
          setSecondaryButtonListener {
            dismiss()
          }
          show()
        }
        return true
      }
    }
    return false
  }
}
