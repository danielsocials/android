package org.haobtc.onekey.onekeys.dappbrowser.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.StyleRes
import com.bumptech.glide.Glide
import org.haobtc.onekey.R
import org.haobtc.onekey.databinding.DialogBaseBottomBinding


/**
 * @author Onekey@QuincySx
 * @create 2021-03-03 10:23 AM
 */
class BaseAlertBottomDialog @JvmOverloads constructor(context: Context, @StyleRes themeResId: Int = 0) : Dialog(context, themeResId) {
  enum class TextStyle {
    CENTERED, LEFT
  }

  private val mBinding by lazy {
    DialogBaseBottomBinding.inflate(layoutInflater)
  }
  private var mPrimaryButtonListener: View.OnClickListener? = null
  private var mSecondaryButtonListener: View.OnClickListener? = null

  init {
    setContentView(mBinding.root)
    window?.apply {
      setLayout(
          ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
      setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      attributes?.apply {
        width = WindowManager.LayoutParams.MATCH_PARENT
        height = WindowManager.LayoutParams.WRAP_CONTENT
        gravity = Gravity.BOTTOM
        attributes = this
      }
      setWindowAnimations(R.style.AnimBottom)
      addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
      navigationBarColor = Color.WHITE

      decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
      decorView.setOnSystemUiVisibilityChangeListener {
        val uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or  //布局位于状态栏下方
            View.SYSTEM_UI_FLAG_FULLSCREEN or  //隐藏导航栏
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            0x00001000
        window?.decorView?.systemUiVisibility = uiOptions
      }
    }
    mBinding.tvButtonSecondary.setOnClickListener { v: View? ->
      mSecondaryButtonListener?.onClick(v)
      dismiss()
    }
    mBinding.tvButtonPrimary.setOnClickListener { v: View? ->
      mPrimaryButtonListener?.onClick(v)
      dismiss()
    }
  }

  fun setProgressMode(): BaseAlertBottomDialog {
    mBinding.layoutIconInfo.visibility = View.VISIBLE
    mBinding.ivDialogLogo.visibility = View.GONE
    mBinding.progressBar.setVisibility(View.VISIBLE)
    return this
  }

  fun setIcon(resId: Int): BaseAlertBottomDialog {
    mBinding.layoutIconInfo.visibility = View.VISIBLE
    mBinding.progressBar.visibility = View.GONE
    mBinding.ivDialogLogo.visibility = View.VISIBLE
    mBinding.ivDialogLogo.setImageResource(resId)
    return this
  }

  fun setIcon(url: String): BaseAlertBottomDialog {
    mBinding.layoutIconInfo.visibility = View.VISIBLE
    mBinding.progressBar.visibility = View.GONE
    mBinding.ivDialogLogo.visibility = View.VISIBLE
    Glide.with(mBinding.ivDialogLogo)
        .load(url)
        .centerCrop()
        .placeholder(R.mipmap.ic_launcher_foreground)
        .error(R.mipmap.ic_launcher_foreground)
        .fitCenter()
        .into(mBinding.ivDialogLogo)
    return this
  }

  fun setTitleOverride(resId: Int): BaseAlertBottomDialog {
    setTitle(resId)
    return this
  }

  fun setTitleOverride(message: CharSequence?): BaseAlertBottomDialog {
    setTitle(message)
    return this
  }

  override fun setTitle(resId: Int) {
    mBinding.dialogMainText.visibility = View.VISIBLE
    mBinding.dialogMainText.text = context.resources?.getString(resId)
  }

  override fun setTitle(message: CharSequence?) {
    mBinding.dialogMainText.visibility = View.VISIBLE
    mBinding.dialogMainText.text = message
  }

  fun setMessage(resId: Int): BaseAlertBottomDialog {
    mBinding.dialogSubText.visibility = View.VISIBLE
    mBinding.dialogSubText.text = context.resources?.getString(resId)
    return this
  }

  fun setMessage(message: CharSequence?): BaseAlertBottomDialog {
    mBinding.dialogSubText.visibility = View.VISIBLE
    mBinding.dialogSubText.text = message
    return this
  }

  fun setMessage(message: String?): BaseAlertBottomDialog {
    mBinding.dialogSubText.visibility = View.VISIBLE
    mBinding.dialogSubText.text = message
    return this
  }

  fun setPrimaryButtonText(resId: Int): BaseAlertBottomDialog {
    mBinding.tvButtonPrimary.visibility = View.VISIBLE
    mBinding.tvButtonPrimary.text = context.resources?.getString(resId)
    return this
  }

  fun setPrimaryButtonListener(listener: View.OnClickListener?): BaseAlertBottomDialog {
    mPrimaryButtonListener = listener
    return this
  }

  fun setSecondaryButtonText(resId: Int): BaseAlertBottomDialog {
    mBinding.tvButtonSecondary.visibility = View.VISIBLE
    mBinding.tvButtonSecondary.text = context.resources?.getString(resId)
    return this
  }

  fun setSecondaryButtonListener(listener: View.OnClickListener?): BaseAlertBottomDialog {
    mBinding.tvButtonSecondary.visibility = View.VISIBLE
    mBinding.viewButtonDivision.visibility = View.VISIBLE
    mSecondaryButtonListener = listener
    return this
  }

  fun setView(view: View?): BaseAlertBottomDialog {
    mBinding.dialogView.addView(view)
    return this
  }

  fun setTextStyle(style: TextStyle?): BaseAlertBottomDialog {
    when (style) {
      TextStyle.CENTERED -> mBinding.dialogSubText.gravity = Gravity.CENTER_HORIZONTAL
      TextStyle.LEFT -> mBinding.dialogSubText.gravity = Gravity.START
    }
    return this
  }
}
