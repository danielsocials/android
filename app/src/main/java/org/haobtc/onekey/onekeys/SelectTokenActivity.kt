package org.haobtc.onekey.onekeys

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.scwang.smartrefresh.layout.util.SmartUtil
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import org.haobtc.onekey.R
import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.adapter.SelectTokenAdapter
import org.haobtc.onekey.bean.Assets
import org.haobtc.onekey.databinding.ActivitySelectTokenBinding
import org.haobtc.onekey.onekeys.homepage.process.SendEthActivity
import org.haobtc.onekey.ui.base.BaseActivity
import org.haobtc.onekey.viewmodel.AppWalletViewModel

/**
 *  转账 选择代币页面
 */
class SelectTokenActivity : BaseActivity() {


  companion object {

    @JvmField  val ASSET_ID ="asset_id"

    @JvmStatic
    fun start(context: Context) {
      val intent = Intent()
      intent.setClass(context, SelectTokenActivity::class.java)
      context.startActivity(intent)
    }
  }

  private val mAppViewModel by lazy {
    ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel::class.java)
  }

  private val mBinding by lazy {
    ActivitySelectTokenBinding.inflate(layoutInflater)
  }


  override fun init() {
    ResourcesCompat.getColor(resources, R.color.button_bk_light_grey, null).apply {
      setStatusBarColor(this)
      toolbar.background = ColorDrawable(this)
    }
    val build = HorizontalDividerItemDecoration.Builder(this)
        .color(ResourcesCompat.getColor(resources, R.color.color_select_wallet_divider, theme))
        .sizeResId(R.dimen.line_hight)
        .margin(SmartUtil.dp2px(12F), 0)
        .build()
    setLeftTitle(R.string.select_token)
    var list: MutableList<Assets> = ArrayList()
    mAppViewModel.currentWalletAssetsList.observe(
        this,
        { assets ->
          list.clear()
          list.addAll(assets)
        })
    var adapter = SelectTokenAdapter(data = list)
    mBinding.recyclerView.adapter = adapter
    mBinding.recyclerView.addItemDecoration(build)
    adapter.setOnItemClickListener { adapter, _, position ->
      val intent = Intent()
      intent.setClass(mContext, SendEthActivity::class.java)
      val unitId = (adapter.getItem(position) as Assets).uniqueId()
      intent.putExtra(ASSET_ID, unitId)
      setResult(Activity.RESULT_OK, intent)
      finish()
    }
  }


  override fun getContentViewId() = 0

  override fun getLayoutView() = mBinding.root


  override fun showToolBar(): Boolean {
    return true
  }

  override fun enableViewBinding(): Boolean {
    return true
  }
}
