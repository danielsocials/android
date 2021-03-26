package org.haobtc.onekey.adapter;

import android.widget.RelativeLayout;
import android.widget.Switch;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;
import java.util.Locale;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.RemoteImage;
import org.haobtc.onekey.bean.TokenList;

/** @Description: 更多代币的适配器 @Author: peter Qin */
public class MoreTokenAdapter extends BaseQuickAdapter<TokenList.ERCToken, BaseViewHolder> {

    private onMoreSwitchClick mOnMoreSwitchClick;
    private List<TokenList.ERCToken> mList;

    public MoreTokenAdapter(
            @Nullable List<TokenList.ERCToken> data, onMoreSwitchClick mOnMoreSwitchClick) {
        super(R.layout.item_token_more_list, data);
        this.mOnMoreSwitchClick = mOnMoreSwitchClick;
        this.mList = data;
    }

    @Override
    protected void convert(BaseViewHolder helper, TokenList.ERCToken item) {
        int position = helper.getAdapterPosition();
        RelativeLayout backGround = helper.getView(R.id.back_layout);
        Switch switchBtn = helper.getView(R.id.switch_btn);
        helper.setText(R.id.token_name, item.symbol);
        String start = item.address.substring(0, 8);
        String end = item.address.substring(item.address.length() - 8);
        helper.setText(
                R.id.token_address, String.format(Locale.getDefault(), "%s...%s", start, end));
        switchBtn.setOnCheckedChangeListener(null);
        switchBtn.setChecked(item.isAdd);
        setAddData(item, backGround, position);
        switchBtn.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    item.isAdd = isChecked;
                    setAddData(item, backGround, position);
                    mOnMoreSwitchClick.onMoreCheckedListener(item, isChecked);
                });
        new RemoteImage(item.logoURI).intoTarget(helper.getView(R.id.icon_img));
    }

    private void setAddData(TokenList.ERCToken item, RelativeLayout backGround, int position) {
        if (item.isAdd) {
            if (position == 1) {
                backGround.setBackgroundResource(R.drawable.top_round_shape_gray);
            } else if (position == mList.size()) {
                backGround.setBackgroundResource(R.drawable.bottom_round_shape_gray);
            } else {
                backGround.setBackgroundColor(mContext.getColor(R.color.color_F9F9FB));
            }
        } else {
            if (position == 1) {
                backGround.setBackgroundResource(R.drawable.top_round_shape_white);
            } else if (position == mList.size()) {
                backGround.setBackgroundResource(R.drawable.bottom_round_shape_white);
            } else {
                backGround.setBackgroundColor(mContext.getColor(R.color.white));
            }
        }
    }

    public interface onMoreSwitchClick {
        void onMoreCheckedListener(TokenList.ERCToken item, boolean isChecked);
    }
}
