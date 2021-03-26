package org.haobtc.onekey.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import org.haobtc.onekey.utils.FontCache.getTypeface

object FontCache {
  private val fontCache: HashMap<String, Typeface?> = HashMap()
  fun getTypeface(fontname: String, context: Context): Typeface? {
    var typeface = fontCache[fontname]
    if (typeface == null) {
      typeface = try {
        Typeface.createFromAsset(context.assets, fontname)
      } catch (e: Exception) {
        return null
      }
      fontCache[fontname] = typeface
    }
    return typeface
  }
}

class CustomFontHelper {
  companion object {
    private const val ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android"

    fun getTextStyle(context: Context, attrs: AttributeSet?): Int {
      return attrs?.getAttributeIntValue(
          ANDROID_SCHEMA,
          "textStyle",
          Typeface.NORMAL
      ) ?: return Typeface.NORMAL
    }
  }

  fun selectTypeface(context: Context, textStyle: Int?): Typeface? {
    /*
    * information about the TextView textStyle:
    * http://developer.android.com/reference/android/R.styleable.html#TextView_textStyle
    */
    return when (textStyle) {
      Typeface.BOLD -> getTypeface("fonts/din_alternate_bold_mini.ttf", context)
      Typeface.NORMAL -> getTypeface("fonts/roboto_regular.ttf", context)
      else -> getTypeface("fonts/din_alternate_mini.ttf", context)
    }
  }
}
