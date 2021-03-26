package org.haobtc.onekey.ui.activity

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.core.content.res.ResourcesCompat
import org.haobtc.onekey.R
import org.haobtc.onekey.databinding.ActivityOnekeyScanQrResultBinding
import org.haobtc.onekey.ui.base.BaseActivity

class OnekeyScanQrResultActivity : BaseActivity() {
  companion object {
    private const val EXT_RESULT = "result"

    @JvmStatic
    fun start(context: Context, result: String) {
      Intent(context, OnekeyScanQrResultActivity::class.java).apply {
        putExtra(EXT_RESULT, result)
        context.startActivity(this)
      }
    }
  }

  private val mBinding by lazy {
    ActivityOnekeyScanQrResultBinding.inflate(getLayoutInflater())
  }

  override fun getLayoutView() = mBinding.root

  override fun enableViewBinding() = true

  override fun showToolBar() = true

  override fun getContentViewId() = 0

  override fun init() {
    ResourcesCompat.getColor(resources, R.color.button_bk_light_grey, null).apply {
      setStatusBarColor(this)
      toolbar.background = ColorDrawable(this)
    }
    toolbar.setTitle(R.string.title_qr_code_scan_results)
    mBinding.tvContent.text = intent.getStringExtra(EXT_RESULT) ?: ""
  }
}
