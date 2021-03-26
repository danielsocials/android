package org.haobtc.onekey.ui.dialog.custom;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.lxj.xpopup.core.BottomPopupView;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.RemoteImage;
import org.haobtc.onekey.bean.TokenList;

/** @Description: 自定义添加代币弹窗 @Author: peter Qin */
public class CustomAddTokenDialog extends BottomPopupView {

    private Unbinder bind;

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.token_name)
    TextView tokenNameTV;

    @BindView(R.id.token_address)
    TextView tokenAddressTV;

    @BindView(R.id.cancel_button)
    TextView cancelBtn;

    @BindView(R.id.add_token_btn)
    TextView addTokenBtn;

    private onConfirmClick mOnConfirmClick;
    private TokenList.ERCToken token;

    public CustomAddTokenDialog(@NonNull Context context, onConfirmClick mOnConfirmClick) {
        super(context);
        this.mOnConfirmClick = mOnConfirmClick;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        bind = ButterKnife.bind(this);
        if (token == null) {
            return;
        }
        new RemoteImage(token.logoURI).intoTarget(mImageView);
        tokenNameTV.setText(token.symbol);
        tokenAddressTV.setText(token.address);
        cancelBtn.setOnClickListener(view -> dismiss());
        addTokenBtn.setOnClickListener(view -> mOnConfirmClick.onConfirmClickListener());
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_add_token;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }

    public void setToken(TokenList.ERCToken token) {
        this.token = token;
    }

    public interface onConfirmClick {
        void onConfirmClickListener();
    }
}
