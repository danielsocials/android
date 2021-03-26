package org.haobtc.onekey.ui.dialog;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.onekey.R;
import org.haobtc.onekey.event.ShowMnemonicEvent;
import org.haobtc.onekey.ui.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * @author liyan
 * @date 12/17/20
 */

public class ScreenshotWarningDialog extends BaseDialogFragment {
    @BindView(R.id.img_cancel)
    ImageView imgCancel;
    @BindView(R.id.btn_next)
    Button btnNext;

    /***
     * init layout
     * @return
     */
    @Override
    public int getContentViewId() {
        return R.layout.dont_screenshot;
    }

    @OnClick({R.id.img_cancel, R.id.btn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_cancel:
                dismiss();
                break;
            case R.id.btn_next:
                EventBus.getDefault().post(new ShowMnemonicEvent());
                dismiss();
                break;
        }
    }
}
