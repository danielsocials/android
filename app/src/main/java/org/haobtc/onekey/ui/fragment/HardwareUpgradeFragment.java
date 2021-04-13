package org.haobtc.onekey.ui.fragment;

import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.google.common.base.Strings;
import java.util.Locale;
import org.haobtc.onekey.R;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.ui.activity.HardwareUpgradeActivity;
import org.haobtc.onekey.ui.base.BaseFragment;
import org.haobtc.onekey.ui.widget.SuperTextView;

/**
 * @author liyan
 * @date 12/3/20
 */
public class HardwareUpgradeFragment extends BaseFragment {

    @BindView(R.id.ble_version)
    TextView bleUpgradeVersion;

    @BindView(R.id.ble_update_description)
    TextView bleUpdateDescription;

    @BindView(R.id.ble_current_version)
    TextView bleCurrentVersion;

    @BindView(R.id.ble_layout)
    LinearLayout bleLayout;

    @BindView(R.id.firm_layout)
    LinearLayout firmLayout;

    @BindView(R.id.firm_version)
    TextView firmUpdateVersion;

    @BindView(R.id.firm_update_description)
    TextView firmUpdateDescription;

    @BindView(R.id.firm_current_version)
    TextView firmCurrentVersion;

    @BindView(R.id.latest_version_cardview)
    LinearLayout latestView;

    @BindView(R.id.update_cardView)
    LinearLayout updateView;

    @BindView(R.id.bottomLayout)
    LinearLayout bottomLayout;

    @BindView(R.id.update_btn)
    SuperTextView updateBtn;

    @BindView(R.id.split_line)
    View splitLine;

    /**
     * init views
     *
     * @param view
     */
    @Override
    public void init(View view) {
        refreshView();
    }

    public void refreshView() {
        if (Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)
                && Strings.isNullOrEmpty(HardwareUpgradeActivity.newBleVersion)) {
            latestView.setVisibility(View.VISIBLE);
            updateView.setVisibility(View.GONE);
            bottomLayout.setVisibility(View.GONE);
            updateBtn.setVisibility(View.GONE);
        } else {
            updateView.setVisibility(View.VISIBLE);
            if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)) {
                firmLayout.setVisibility(View.VISIBLE);
                firmUpdateVersion.setText(
                        String.format(
                                Locale.getDefault(),
                                "%s %s",
                                getString(R.string.firm_version),
                                HardwareUpgradeActivity.newFirmwareVersion));
                firmUpdateDescription.setText(HardwareUpgradeActivity.firmwareChangelog);
                if (Strings.isNullOrEmpty(HardwareUpgradeActivity.currentFirmwareVersion)) {
                    firmCurrentVersion.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%s %s",
                                    getString(R.string.current_version),
                                    getString(R.string.unknown_version)));
                } else {
                    firmCurrentVersion.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%s %s",
                                    getString(R.string.current_version),
                                    HardwareUpgradeActivity.currentFirmwareVersion));
                }
                if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newBleVersion)) {
                    splitLine.setVisibility(View.VISIBLE);
                }
            } else {
                firmLayout.setVisibility(View.GONE);
            }
            if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newBleVersion)) {
                bleLayout.setVisibility(View.VISIBLE);
                bleUpgradeVersion.setText(
                        String.format(
                                Locale.getDefault(),
                                "%s %s",
                                getString(R.string.ble_version),
                                HardwareUpgradeActivity.newBleVersion));
                bleUpdateDescription.setMovementMethod(ScrollingMovementMethod.getInstance());
                bleUpdateDescription.setText(HardwareUpgradeActivity.nrfChangelog);
                if (Strings.isNullOrEmpty(HardwareUpgradeActivity.currentBleVersion)) {
                    bleCurrentVersion.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%s %s",
                                    getString(R.string.current_version),
                                    getString(R.string.unknown_version)));
                } else {
                    bleCurrentVersion.setText(
                            String.format(
                                    Locale.getDefault(),
                                    "%s %s",
                                    getString(R.string.current_version),
                                    HardwareUpgradeActivity.currentBleVersion));
                }
                if (Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)) {
                    splitLine.setVisibility(View.GONE);
                }
            } else {
                bleLayout.setVisibility(View.GONE);
                if (!Strings.isNullOrEmpty(HardwareUpgradeActivity.newFirmwareVersion)) {
                    splitLine.setVisibility(View.GONE);
                }
            }
        }
    }

    /**
     * * init layout
     *
     * @return
     */
    @Override
    public int getContentViewId() {
        return R.layout.hardware_upgrade_fragment;
    }

    @SingleClick(value = 2000L)
    @OnClick({R.id.update_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.update_btn:
                if (getActivity() instanceof HardwareUpgradeActivity) {
                    HardwareUpgradeActivity activity = (HardwareUpgradeActivity) getActivity();
                    activity.replaceUpgradingFragment();
                    break;
                }
        }
    }
}
