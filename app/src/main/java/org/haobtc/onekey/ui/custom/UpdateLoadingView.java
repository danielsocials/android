package org.haobtc.onekey.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import org.haobtc.onekey.R;

/** @Author: peter Qin */
public class UpdateLoadingView extends LinearLayout {

    private Context mContext;
    private TextView mText, progressTV;
    private ProgressBar mBar;
    private ImageView switchImg;
    private View line;
    private int mStatus;

    @IntDef
    public @interface DownLodStatus {
        int START = 0;
        int PREPARE = 1;
        int DONE = 2;
    }

    public UpdateLoadingView(Context context) {
        super(context);
        this.mContext = context;
    }

    public UpdateLoadingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        LayoutInflater.from(context).inflate(R.layout.item_upgrade_loading, this);
        mText = findViewById(R.id.status_text);
        mBar = findViewById(R.id.item_progress_bar);
        switchImg = findViewById(R.id.switch_img);
        line = findViewById(R.id.bottom_line);
        progressTV = findViewById(R.id.download_progress);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UpgradeTextView);
        String text = typedArray.getText(R.styleable.UpgradeTextView_statusText).toString();
        String progressDefault =
                typedArray.getText(R.styleable.UpgradeTextView_progress_text).toString();
        if (!TextUtils.isEmpty(text)) {
            mText.setText(text);
        }
        if (!TextUtils.isEmpty(progressDefault)) {
            progressTV.setText(progressDefault);
        }
        typedArray.recycle();
    }

    public void setStatus(int status) {
        this.mStatus = status;
        if (status == DownLodStatus.START) {
            mBar.setVisibility(VISIBLE);
            switchImg.setVisibility(GONE);
            mText.setTextColor(mContext.getColor(R.color.text_color2));
        } else if (status == DownLodStatus.PREPARE) {
            mBar.setVisibility(GONE);
            switchImg.setVisibility(VISIBLE);
        } else {
            mBar.setVisibility(GONE);
            switchImg.setVisibility(VISIBLE);
            switchImg.setBackground(ContextCompat.getDrawable(mContext, R.drawable.checked));
            mText.setTextColor(mContext.getColor(R.color.text_color2));
        }
    }

    public void setProgressShow(String progressShow) {
        progressTV.setText(progressShow);
    }

    // judge is in progress
    public boolean getIsProgress() {
        return mBar.getVisibility() == VISIBLE;
    }

    public void setLineVisibility(int visibility) {
        line.setVisibility(visibility);
    }
}
