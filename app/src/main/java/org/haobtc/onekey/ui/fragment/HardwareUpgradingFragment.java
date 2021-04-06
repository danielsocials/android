package org.haobtc.onekey.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.common.base.Strings;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.UpdateSuccessEvent;
import org.haobtc.onekey.databinding.FragmentHardwareUpgradingBinding;
import org.haobtc.onekey.event.HardWareUpdateEvent;
import org.haobtc.onekey.event.UpdateEvent;
import org.haobtc.onekey.ui.activity.HardwareUpgradeActivity;
import org.haobtc.onekey.ui.base.BaseFragment;
import org.haobtc.onekey.ui.custom.UpdateLoadingView;

/**
 * @author liyan
 * @date 12/3/20 update 1/4 peter
 */
public class HardwareUpgradingFragment extends BaseFragment {

    private FragmentHardwareUpgradingBinding mBinding;
    private HardwareUpgradeActivity mActivity;

    @IntDef({
        HardwareType.BLE,
        HardwareType.FIRMWARE,
    })
    public @interface HardwareType {
        int BLE = 0;
        int FIRMWARE = 1;
    }

    @IntDef({ProgressStatus.DOWNLOAD, ProgressStatus.INSTALL})
    public @interface ProgressStatus {
        int DOWNLOAD = 0;
        int INSTALL = 1;
    }

    private boolean mInstallComplete = false;

    /**
     * init views
     *
     * @param view
     */
    @Override
    public void init(View view) {
        if (getActivity() instanceof HardwareUpgradeActivity) {
            mActivity = (HardwareUpgradeActivity) getActivity();
        }
        if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)
                && !Strings.isNullOrEmpty(HardwareUpgradeActivity.newBleVersion)) {
            // have firmware and ble upload
            mBinding.downloadBle.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.installBle.setStatus(UpdateLoadingView.DownLodStatus.PREPARE);
            mBinding.downloadFirmware.setStatus(UpdateLoadingView.DownLodStatus.PREPARE);
            mBinding.installFirmware.setStatus(UpdateLoadingView.DownLodStatus.PREPARE);
            mBinding.installFirmware.setLineVisibility(View.GONE);
        } else if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)
                && Strings.isNullOrEmpty(HardwareUpgradeActivity.newBleVersion)) {
            // only have firmware upload
            mBinding.bleLayout.setVisibility(View.GONE);
            mBinding.downloadFirmware.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.installFirmware.setStatus(UpdateLoadingView.DownLodStatus.PREPARE);
            mBinding.installFirmware.setLineVisibility(View.GONE);
        } else if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newBleVersion)
                && Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)) {
            // only have ble upload
            mBinding.firmwareLayout.setVisibility(View.GONE);
            mBinding.downloadBle.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.installBle.setStatus(UpdateLoadingView.DownLodStatus.PREPARE);
            mBinding.installBle.setLineVisibility(View.GONE);
        }
        EventBus.getDefault().post(new UpdateEvent(UpdateEvent.BLE));

        mBinding.confirmButton.setOnClickListener(
                v -> {
                    mActivity.showUpdateComplete();
                });
    }

    /**
     * * init layout
     *
     * @return
     */
    @Override
    public int getContentViewId() {
        return 0;
    }

    @Override
    public boolean enableViewBinding() {
        return true;
    }

    @Nullable
    @Override
    public View getLayoutView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mBinding = FragmentHardwareUpgradingBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    private void setBleDownProgress(int progress) {
        if (progress == 100) {
            mBinding.downloadBle.setStatus(UpdateLoadingView.DownLodStatus.DONE);
            mBinding.downloadBle.setProgressShow(getString(R.string.download_complete));
            mBinding.installBle.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.installBle.setProgressShow(getString(R.string.install_now));
        } else {
            mBinding.downloadBle.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.downloadBle.setProgressShow(
                    String.format(getString(R.string.download_now) + "%d%%", progress));
        }
    }

    private void setBleInstallProgress(int progress) {
        if (progress == 100) {
            mBinding.installBle.setStatus(UpdateLoadingView.DownLodStatus.DONE);
            mBinding.installBle.setProgressShow(getString(R.string.install_complete));
            mInstallComplete = true;
            if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)) {
                mBinding.downloadFirmware.setStatus(UpdateLoadingView.DownLodStatus.START);
            }
        } else {
            mBinding.installBle.setProgressShow(
                    String.format(getString(R.string.install_now) + "%d%%", progress));
            mBinding.installBle.setStatus(UpdateLoadingView.DownLodStatus.START);
        }

        if (mBinding.downloadBle.getIsProgress()) {
            mBinding.downloadBle.setStatus(UpdateLoadingView.DownLodStatus.DONE);
            mBinding.downloadBle.setProgressShow(getString(R.string.download_complete));
        }
    }

    private void setFirmWareInstallProgress(int progress) {

        if (progress == 100) {
            mBinding.installFirmware.setStatus(UpdateLoadingView.DownLodStatus.DONE);
            mBinding.installFirmware.setProgressShow(getString(R.string.install_complete));
            mInstallComplete = true;
        } else {
            mBinding.installFirmware.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.installFirmware.setProgressShow(
                    String.format(getString(R.string.install_now) + "%d%%", progress));
        }
        if (mBinding.downloadFirmware.getIsProgress()) {
            mBinding.downloadFirmware.setStatus(UpdateLoadingView.DownLodStatus.DONE);
            mBinding.downloadFirmware.setProgressShow(getString(R.string.download_complete));
        }
    }

    private void setFirmWareDownProgress(int progress) {
        if (progress == 100) {
            mBinding.downloadFirmware.setStatus(UpdateLoadingView.DownLodStatus.DONE);
            mBinding.installFirmware.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.downloadFirmware.setProgressShow(getString(R.string.download_complete));
            mBinding.installFirmware.setProgressShow(getString(R.string.install_now));
        } else {
            mBinding.downloadFirmware.setStatus(UpdateLoadingView.DownLodStatus.START);
            mBinding.downloadFirmware.setProgressShow(
                    String.format(getString(R.string.download_now) + "%d%%", progress));
        }
    }

    /**
     * event.status : 0--> start download ;1--> start install
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdating(HardWareUpdateEvent event) {
        if (event.hardwareType == HardwareType.BLE) {
            if (event.status == ProgressStatus.DOWNLOAD) {
                setBleDownProgress(event.progress);
            } else {
                setBleInstallProgress(event.progress);
            }
        } else if (event.hardwareType == HardwareType.FIRMWARE) {
            if (event.status == ProgressStatus.DOWNLOAD) {
                setFirmWareDownProgress(event.progress);
            } else {
                setFirmWareInstallProgress(event.progress);
            }
        }
    }

    public boolean getUpgradeComplete() {
        return mBinding.confirmButton.isEnabled();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdating(UpdateSuccessEvent event) {
        mBinding.confirmButton.setEnabled(true);
        mBinding.confirmButton.setTextColor(getResources().getColor(R.color.white));
    }

    @Override
    public boolean needEvents() {
        return true;
    }
}
