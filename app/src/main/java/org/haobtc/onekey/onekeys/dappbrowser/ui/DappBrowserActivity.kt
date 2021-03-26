package org.haobtc.onekey.onekeys.dappbrowser.ui

import android.content.Context
import android.content.Intent
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.DAppBrowserBean
import org.haobtc.onekey.ui.base.BaseActivity

/**
 * Dapp 浏览器
 *
 * @author Onekey@QuincySx
 * @create 2021-03-02 2:23 PM
 */
class DappBrowserActivity : BaseActivity(), OnFinishOrBackCallback {
  companion object {
    const val EXT_URL = "url"
    const val EXT_DAPP_BEAN = "bean"

    @JvmField
    val DEFAULT_URL = "file:///android_asset/test/local_test.html"

    @JvmStatic
    fun start(context: Context, url: String) {
      Intent(context, DappBrowserActivity::class.java).apply {
        putExtra(EXT_URL, url)
        context.startActivity(this)
      }
    }

    @JvmStatic
    fun start(context: Context, bean: DAppBrowserBean) {
      Intent(context, DappBrowserActivity::class.java).apply {
        putExtra(EXT_DAPP_BEAN, bean)
        putExtra(EXT_URL, bean.url)
        context.startActivity(this)
      }
    }
  }

  private var mOnBackPressedCallback: OnBackPressedCallback? = null
  override fun getContentViewId() = R.layout.activity_dapp_browser

  override fun init() {
    supportFragmentManager.beginTransaction()
        .add(R.id.fragment_container_view,
            DappBrowserFragment.start(
                intent.getStringExtra(EXT_URL) ?: DEFAULT_URL,
                intent.getParcelableExtra(EXT_DAPP_BEAN) as DAppBrowserBean?))
        .commit()
  }

  override fun onBackPressed() {
    if (mOnBackPressedCallback?.onBackPressed() == true) {
      super.onBackPressed()
    }
  }

  override fun setOnBackPressed(onBackPressed: OnBackPressedCallback) {
    mOnBackPressedCallback = onBackPressed
  }
}

interface OnFinishOrBackCallback {
  fun setOnBackPressed(onBackPressed: OnBackPressedCallback)

  fun finish()
}

interface OnBackPressedCallback {
  fun onBackPressed(): Boolean
}
