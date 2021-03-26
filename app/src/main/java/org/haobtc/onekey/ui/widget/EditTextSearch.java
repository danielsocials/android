package org.haobtc.onekey.ui.widget;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatEditText;
import org.haobtc.onekey.R;

/** @Description: 自定义带删除按钮的搜索框 @Author: peter Qin */
public class EditTextSearch extends AppCompatEditText {

    /** 步骤1：定义左侧搜索图标 & 一键删除图标 & 语音图标 */
    private Drawable clearDrawable, searchDrawable;

    private Context context;

    private boolean isShowClear;

    public EditTextSearch(Context context) {
        super(context);
        this.context = context;
        init();
        // 初始化该组件时，对EditText_Clear进行初始化 ->>步骤2
    }

    public EditTextSearch(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public EditTextSearch(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /** 步骤2：初始化 图标资源 */
    private void init() {
        clearDrawable = getResources().getDrawable(R.mipmap.delete_search);
        clearDrawable.setBounds(0, 0, 40, 40);
        searchDrawable = getResources().getDrawable(R.drawable.search_icon);
        searchDrawable.setBounds(0, 0, 40, 40);
        setCompoundDrawables(searchDrawable, null, null, null);
    }

    /**
     * 步骤3：通过监听复写EditText本身的方法来确定是否显示删除图标 监听方法：onTextChanged（） & onFocusChanged（） 调用时刻：当输入框内容变化时 &
     * 焦点发生变化时
     */
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        setClearIconVisible(hasFocus() && text.length() > 0);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        setClearIconVisible(focused && length() > 0);
        // focused = 是否获得焦点
        // 同样根据setClearIconVisible（）判断是否要显示删除图标
    }

    /** 关注1 作用：判断是否显示删除图标 当显示删除图标时就隐藏语音图标 */
    private void setClearIconVisible(boolean visible) {
        isShowClear = visible;
        setCompoundDrawables(searchDrawable, null, visible ? clearDrawable : null, null);
    }

    /** 步骤4：对删除图标区域设置点击事件，即"点击 = 清空搜索框内容" 原理：当手指抬起的位置在删除图标的区域，即视为点击了删除图标 = 清空搜索框内容 */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
                // 原理：当手指抬起的位置在删除图标的区域，即视为点击了删除图标 = 清空搜索框内容
                // isShowClear：为true,点击删除图标；为false，点击语音图标
            case MotionEvent.ACTION_UP:
                if (isShowClear) {
                    Drawable drawable = clearDrawable;
                    if (drawable != null
                            && event.getX() <= (getWidth() - getPaddingRight())
                            && event.getX()
                                    >= (getWidth()
                                            - getPaddingRight()
                                            - drawable.getBounds().width())) {
                        setText("");
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public interface onClearClick {
        void onClearSearchText();
    }
}
