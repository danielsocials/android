package org.haobtc.onekey.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import androidx.appcompat.widget.AppCompatTextView
import org.haobtc.onekey.utils.CustomFontHelper

@Keep
class NumberTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(
    context,
    attrs,
    defStyleAttr
) {
  private val mCustomFontHelper by lazy {
    CustomFontHelper()
  }

  init {
    applyCustomFont(context, attrs)
  }

  private fun applyCustomFont(context: Context, attrs: AttributeSet?) {
    val textStyle = CustomFontHelper.getTextStyle(context, attrs)
    val customFont = mCustomFontHelper.selectTypeface(context, textStyle)
    typeface = customFont
  }
}
