package org.haobtc.onekey.onekeys.homepage.mindmenu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doOnTextChanged
import com.alibaba.fastjson.JSON
import com.lxj.xpopup.XPopup
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.PyResponse
import org.haobtc.onekey.bean.TokenList
import org.haobtc.onekey.databinding.ActivityAddNewBinding
import org.haobtc.onekey.manager.PyEnv
import org.haobtc.onekey.onekeys.TokenManagerActivity
import org.haobtc.onekey.ui.base.BaseActivity
import org.haobtc.onekey.ui.dialog.custom.CustomAddTokenDialog

class AddNewTokenActivity : BaseActivity() {


  companion object {
    @JvmField
    val ASSET_JSON = "asset_json"

    @JvmStatic
    fun start(context: Context) {
      context.startActivity(Intent(context, AddNewTokenActivity::class.java))
    }
  }

  val mBinding by lazy {
    ActivityAddNewBinding.inflate(layoutInflater)
  }


  override fun getContentViewId() = 0

  override fun init() {
    ResourcesCompat.getColor(resources, R.color.button_bk_light_grey, null).apply {
      setStatusBarColor(this)
      toolbar.background = ColorDrawable(this)
    }
    setLeftTitle(R.string.add_token)
    mBinding.addText.alpha = 0.3F
    mBinding.editReceiverAddress.doOnTextChanged { text, start, before, count ->
      when {
        count > 0 -> {
          mBinding.addText.isEnabled = true
          mBinding.addText.alpha = 1.0f
        }
        else -> {
          mBinding.addText.isEnabled = false
          mBinding.addText.alpha = 0.3f
        }
      }
    }
    mBinding.addLayout.setOnClickListener {
      if (mBinding.editReceiverAddress.toString().isEmpty()) {
        return@setOnClickListener
      }

      if (mCompositeDisposable.isDisposed) {
        mCompositeDisposable.dispose()
      }
      mBinding.itemProgressBar.visibility = View.VISIBLE
      val dispose = Observable.create<PyResponse<TokenList.ERCToken>> {
        val result = PyEnv.getCustomerTokenInfo(mBinding.editReceiverAddress.text.toString())
        it.onNext(result)
        it.onComplete()
      }.subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            mBinding.itemProgressBar.visibility = View.GONE
            when {
              it.errors.isNullOrBlank() -> {
                mBinding.invalidLayout.visibility = View.INVISIBLE
                val token = it.result
                val dialog = CustomAddTokenDialog(mContext) {
                  addToken(token)
                }
                dialog.setToken(token)
                XPopup.Builder(mContext)
                    .asCustom(dialog).show()
              }
              else -> mBinding.invalidLayout.visibility = View.VISIBLE
            }
          }
      mCompositeDisposable.add(dispose)
    }

  }

  private fun addToken(token: TokenList.ERCToken) {
    val dispose = Observable.create<PyResponse<String>> {
      val result = PyEnv.addToken(token.symbol, token.address);
      it.onNext(result)
      it.onComplete()
    }.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
          when {
            it.errors.isNullOrBlank() -> {
              val intent = Intent()
              intent.setClass(mContext, TokenManagerActivity::class.java)
              intent.putExtra(ASSET_JSON, JSON.toJSONString(token))
              setResult(Activity.RESULT_OK, intent)
              finish()
            }
            else -> mToast(it.errors)
          }
        }
    mCompositeDisposable.add(dispose)
  }

  override fun enableViewBinding() = true

  override fun getLayoutView(): View? {
    return mBinding.root
  }

  override fun showToolBar(): Boolean {
    return true
  }

  override fun onDestroy() {
    super.onDestroy()
    mCompositeDisposable.dispose()

  }


}
