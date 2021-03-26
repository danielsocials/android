package org.haobtc.onekey.onekeys.homepage

import android.content.Context
import android.net.http.SslError
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebView
import androidx.lifecycle.ViewModelProvider
import com.github.lzyzsd.jsbridge.BridgeWebViewClient
import com.github.lzyzsd.jsbridge.CallBackFunction
import com.google.gson.Gson
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import org.haobtc.onekey.R
import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.bean.DAppBrowserBean
import org.haobtc.onekey.bean.JsBridgeRequestBean
import org.haobtc.onekey.bean.JsBridgeResponseBean
import org.haobtc.onekey.business.wallet.DappManager
import org.haobtc.onekey.constant.Vm
import org.haobtc.onekey.constant.Vm.CoinType
import org.haobtc.onekey.databinding.FragmentTabFindBinding
import org.haobtc.onekey.onekeys.dappbrowser.ui.BaseAlertBottomDialog
import org.haobtc.onekey.onekeys.dappbrowser.ui.DappBrowserActivity.Companion.start
import org.haobtc.onekey.onekeys.dappbrowser.ui.DappCollectionSheetDialog
import org.haobtc.onekey.onekeys.dappbrowser.ui.DappResultAlertDialog
import org.haobtc.onekey.ui.base.BaseFragment
import org.haobtc.onekey.ui.dialog.SelectAccountBottomSheetDialog
import org.haobtc.onekey.utils.URLUtils
import org.haobtc.onekey.viewmodel.AppWalletViewModel
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class FindFragment : BaseFragment(), OnBackPressedCallback {

  private lateinit var mBinding: FragmentTabFindBinding
  private val mDappManager = DappManager()
  private var mAppWalletViewModel: AppWalletViewModel? = null
  private val mGson = Gson()

  override fun getContentViewId() = 0

  override fun enableViewBinding() = true

  override fun getLayoutView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
    mBinding = FragmentTabFindBinding.inflate(inflater, container, false)
    return mBinding.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is SetOnBackCallback) {
      context.setOnBackPressed(this)
    }
  }

  override fun init(view: View) {}

  private fun loadPage(random: String = "1") {
    if (Vm.getEthNetwork() == Vm.PyenvETHNetworkType.MainNet) {
      mBinding.webviewBridge.loadUrl("https://dapp.onekey.so?nocache=$random")
    } else {
      mBinding.webviewBridge.loadUrl("https://dapp.test.onekey.so?nocache=$random")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mAppWalletViewModel = ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel::class.java)

    mBinding.layoutSwipeRefresh.setOnRefreshListener {
      loadPage(Random.nextInt().toString())
    }

    loadPage()

    mBinding.webviewBridge.settings.domStorageEnabled = true
    mBinding.webviewBridge.webViewClient = object : BridgeWebViewClient(mBinding.webviewBridge) {
      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        mBinding.layoutSwipeRefresh.isRefreshing = false
      }

      override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        val aDialog = DappResultAlertDialog(requireContext())
        aDialog.setTitle(R.string.title_warning)
        aDialog.setIcon(DappResultAlertDialog.ERROR)
        aDialog.setMessage(R.string.hint_https_error)
        aDialog.setButtonText(R.string.action_continue)
        aDialog.setButtonListener {
          handler?.proceed()
          aDialog.dismiss()
        }
        aDialog.setSecondaryButtonText(R.string.cancel)
        aDialog.setSecondaryButtonListener {
          mBinding.layoutSwipeRefresh.isRefreshing = false
          handler?.cancel()
          aDialog.dismiss()
        }
        aDialog.show()
      }
    }

    mBinding.webviewBridge.registerHandler(
        "callNativeMethod"
    ) { data: String?, function: CallBackFunction ->
      val (method, id, params) = mGson.fromJson(data, JsBridgeRequestBean::class.java)
      when (method) {
        "openDapp" -> {
          checkAccount(params, object : ResultCallback(id) {
            override fun onSuccess() {
              function.onCallBack(mGson.toJson(JsBridgeResponseBean(requestId, "success")))
            }

            override fun onError() {
              function.onCallBack(mGson.toJson(JsBridgeResponseBean(requestId, "error")))
            }
          })
        }
        "openURL" -> {
          start(requireContext(), params)
          function.onCallBack(mGson.toJson(JsBridgeResponseBean(id, "success")))
        }
        "openFavorite" -> {
          DappCollectionSheetDialog()
              .show(parentFragmentManager, "collection")
          function.onCallBack(mGson.toJson(JsBridgeResponseBean(id, "success")))
        }
        else -> function.onCallBack(
            mGson.toJson(JsBridgeResponseBean(id, "error")))
      }
    }
  }

  private fun checkAccount(data: String, callback: ResultCallback) {
    Single.fromCallable { mGson.fromJson(data, DAppBrowserBean::class.java) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe(
            { dAppBrowserBean: DAppBrowserBean ->
              if (dAppBrowserBean.chain == null) {
                return@subscribe
              }
              val coinType = CoinType.convertByCoinName(dAppBrowserBean.chain)
              // 判断不支持的币种
              if (coinType == null) {
                val dialog = BaseAlertBottomDialog(requireContext())
                dialog.show()
                dialog.setTitle(
                    getString(
                        R.string.title_does_not_support,
                        dAppBrowserBean.chain))
                dialog.setMessage(R.string.support_less_promote)
                callback.onError()
                return@subscribe
              }
              val currentWalletAccountInfo = mAppWalletViewModel!!.currentWalletAccountInfo
              if (currentWalletAccountInfo.value != null
                  && currentWalletAccountInfo.value!!.coinType
                  != coinType) {
                val dialog = BaseAlertBottomDialog(requireContext())
                dialog.show()
                dialog.setIcon(dAppBrowserBean.img ?: URLUtils.getWebFavicon(dAppBrowserBean.url))
                dialog.setTitle(
                    getString(
                        R.string.title_account_unavailable,
                        dAppBrowserBean.chain))
                dialog.setMessage(
                    getString(
                        R.string.hint_account_unavailable_content,
                        dAppBrowserBean.chain))
                dialog.setPrimaryButtonListener {
                  dialog.dismiss()
                  SelectAccountBottomSheetDialog.newInstance(coinType)
                      .setOnSelectAccountCallback { openDapp(dAppBrowserBean, callback) }
                      .show(
                          parentFragmentManager,
                          "SelectAccount")
                }
                dialog.setSecondaryButtonListener {
                  callback.onError()
                  dialog.dismiss()
                }
              } else {
                openDapp(dAppBrowserBean, callback)
              }
            }) { obj: Throwable ->
          callback.onError()
          obj.printStackTrace()
        }
        .isDisposed
  }

  private fun openDapp(bean: DAppBrowserBean, callback: ResultCallback) {
    Single
        .fromCallable {
          bean.firstUse = mDappManager.firstUse(bean.name)
          bean
        }
        .map { dAppBrowserBean: DAppBrowserBean ->
          if (!dAppBrowserBean.firstUse) {
            callback.onSuccess()
            start(requireContext(), dAppBrowserBean)
          }
          dAppBrowserBean
        }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ dAppBrowserBean: DAppBrowserBean ->
          if (dAppBrowserBean.firstUse) {
            val dialog = BaseAlertBottomDialog(requireContext())
            dialog.show()
            dialog.setIcon(dAppBrowserBean.img ?: URLUtils.getWebFavicon(dAppBrowserBean.url))
            dialog.setTitle(
                getString(
                    R.string.title_frist_use_dapp,
                    dAppBrowserBean.name))
            dialog.setMessage(
                getString(
                    R.string.title_frist_use_dapp_privacy,
                    dAppBrowserBean.name))
            dialog.setPrimaryButtonText(R.string.i_know_)
            dialog.setPrimaryButtonListener {
              callback.onSuccess()
              mDappManager.userDapp(dAppBrowserBean.name)
              start(requireContext(), dAppBrowserBean)
            }
            dialog.setSecondaryButtonListener {
              callback.onError()
              dialog.dismiss()
            }
          }
        }) { obj: Throwable ->
          callback.onError()
          obj.printStackTrace()
        }
        .isDisposed
  }

  override fun onBackPressed(): Boolean {
    val countDownLatch = CountDownLatch(1)
    var result = false;
    val toJson = mGson.toJson(JsBridgeRequestBean("handleBackPressed", "handleBack", ""))
    mBinding.webviewBridge.callHandler("callJavaScriptMethod", toJson) {
      val fromJson = mGson.fromJson(it, JsBridgeResponseBean::class.java)
      result = fromJson.result.equals("true", true)
      countDownLatch.countDown()
    }
    // 只等待 300ms 如果网页不反应，按不拦截处理。
    countDownLatch.await(300, TimeUnit.MILLISECONDS)
    return result
  }
}

abstract class ResultCallback(val requestId: String) {
  abstract fun onSuccess()
  abstract fun onError()
}

interface SetOnBackCallback {
  fun setOnBackPressed(onBackPressed: OnBackPressedCallback)
}

interface OnBackPressedCallback {
  fun onBackPressed(): Boolean
}
