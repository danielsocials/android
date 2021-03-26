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

/** @Description: 热门代币的适配器 @Author: peter Qin */
public class HotTokenAdapter extends BaseQuickAdapter<TokenList.ERCToken, BaseViewHolder> {

    private onHotSwitchClick mOnHotSwitchClick;
    private List<TokenList.ERCToken> mData;

    public HotTokenAdapter(
            @Nullable List<TokenList.ERCToken> data, onHotSwitchClick mOnHotSwitchClick) {
        super(R.layout.item_token_list, data);
        this.mData = data;
        this.mOnHotSwitchClick = mOnHotSwitchClick;
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
        setAddData(item, backGround, position);
        switchBtn.setOnCheckedChangeListener(null);
        switchBtn.setChecked(item.isAdd);
        switchBtn.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    item.isAdd = isChecked;
                    setAddData(item, backGround, position);
                    mOnHotSwitchClick.onHotCheckedListener(
                            item, isChecked, helper.getAdapterPosition());
                });
        new RemoteImage(item.logoURI).intoTarget(helper.getView(R.id.icon_img));
    }

    private void setAddData(TokenList.ERCToken item, RelativeLayout backGround, int position) {
        if (mData.size() > 1) {
            if (position == 0) {
                if (item.isAdd) {
                    backGround.setBackgroundResource(R.drawable.top_round_shape_gray);
                } else {
                    backGround.setBackgroundResource(R.drawable.top_round_shape_white);
                }
            } else if (position == mData.size() - 1) {
                if (item.isAdd) {
                    backGround.setBackgroundResource(R.drawable.bottom_round_shape_gray);
                } else {
                    backGround.setBackgroundResource(R.drawable.bottom_round_shape_white);
                }
            } else {
                if (item.isAdd) {
                    backGround.setBackgroundColor(mContext.getColor(R.color.color_F9F9FB));
                } else {
                    backGround.setBackgroundColor(mContext.getColor(R.color.white));
                }
            }
        } else {
            if (item.isAdd) {
                backGround.setBackgroundResource(R.drawable.round_shape_gray);
            } else {
                backGround.setBackgroundResource(R.drawable.round_shape_white);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface onHotSwitchClick {
        void onHotCheckedListener(TokenList.ERCToken item, boolean isChecked, int position);
    }
}
