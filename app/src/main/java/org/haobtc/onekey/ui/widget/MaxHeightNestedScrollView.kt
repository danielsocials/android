package org.haobtc.onekey.ui.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.widget.NestedScrollView
import org.haobtc.onekey.R


class MaxHeightNestedScrollView @JvmOverloads constructor(
    @NonNull context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NestedScrollView(context, attrs, defStyleAttr) {

  private var maxHeight = -1

  init {
    init(context, attrs, defStyleAttr)
  }

  private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
    val a = context.obtainStyledAttributes(
        attrs, R.styleable.MaxHeightNestedScrollView, defStyleAttr, 0)
    maxHeight = a.getDimensionPixelSize(R.styleable.MaxHeightNestedScrollView_maxHeight, 0)
    a.recycle()
  }

  fun getMaxHeight(): Int {
    return maxHeight
  }

  fun setMaxHeight(maxHeight: Int) {
    this.maxHeight = maxHeight
  }

  fun setMaxHeightDensity(dps: Int) {
    maxHeight = (dps * context.resources.displayMetrics.density).toInt()
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    var newHeightMeasureSpec = heightMeasureSpec
    if (maxHeight > 0) {
      newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
    }
    super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
  }
}
