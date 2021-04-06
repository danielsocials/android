package org.haobtc.onekey.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import com.google.common.base.Strings;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.HardwareFeatures;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.bean.UpdateSuccessEvent;
import org.haobtc.onekey.business.wallet.DeviceManager;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.constant.PyConstant;
import org.haobtc.onekey.dfu.service.DfuService;
import org.haobtc.onekey.event.ButtonRequestEvent;
import org.haobtc.onekey.event.ChangePinEvent;
import org.haobtc.onekey.event.ExceptionEvent;
import org.haobtc.onekey.event.HardWareUpdateEvent;
import org.haobtc.onekey.event.UpdateEvent;
import org.haobtc.onekey.exception.HardWareExceptions;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.dappbrowser.ui.BaseAlertBottomDialog;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.fragment.HardwareUpgradeFragment;
import org.haobtc.onekey.ui.fragment.HardwareUpgradingFragment;
import org.haobtc.onekey.utils.OKHttpUtils;
import org.haobtc.onekey.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author liyan
 * @date 12/3/20
 */
public class HardwareUpgradeActivity extends BaseActivity
        implements DeviceManager.OnConnectDeviceListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    // 当前固件版本
    public static String currentFirmwareVersion;
    // 当前蓝牙版本
    public static String currentBleVersion;
    // 固件的新版本
    public static String newFirmwareVersion;
    // 蓝牙的新版本
    public static String newBleVersion;
    public static String firmwareChangelog;
    public static String nrfChangelog;
    private String firmwareUrl;
    private String nrfUrl;
    private String mac;
    private String label;
    private String deviceId;
    private boolean isForceUpdate;
    private HardwareUpgradeFragment hardwareUpgradeFragment;
    protected HardwareUpgradingFragment hardwareUpgradingFragment;
    private String cacheDir;
    private MyTask task;
    private SharedPreferences devices;
    private DeviceManager deviceManager;
    /** dfu callback */
    private final DfuProgressListener dfuProgressListener =
            new DfuProgressListenerAdapter() {
                @Override
                public void onDfuCompleted(@NonNull String deviceAddress) {
                    super.onDfuCompleted(deviceAddress);
                    // 更新本地信息
                    String features = devices.getString(deviceId, "");
                    if (!Strings.isNullOrEmpty(features)) {
                        HardwareFeatures features1 = HardwareFeatures.objectFromData(features);
                        features1.setBleVer(newBleVersion);
                        devices.edit().putString(deviceId, features1.toString()).apply();
                    }
                    currentBleVersion = newBleVersion;
                    newBleVersion = null;
                    if (!Strings.isNullOrEmpty(newFirmwareVersion)) {
                        deviceManager.connectDeviceByMacAddress(mac, HardwareUpgradeActivity.this);
                    } else {
                        EventBus.getDefault().post(new UpdateSuccessEvent());
                    }
                }

                @Override
                public void onDfuProcessStarted(@NonNull String deviceAddress) {
                    super.onDfuProcessStarted(deviceAddress);
                }

                @Override
                public void onDfuProcessStarting(@NonNull String deviceAddress) {
                    super.onDfuProcessStarting(deviceAddress);
                }

                @Override
                public void onDfuAborted(@NonNull String deviceAddress) {
                    super.onDfuAborted(deviceAddress);
                    EventBus.getDefault().post(new ExceptionEvent("abort"));
                }

                @Override
                public void onProgressChanged(
                        @NonNull String deviceAddress,
                        int percent,
                        float speed,
                        float avgSpeed,
                        int currentPart,
                        int partsTotal) {
                    super.onProgressChanged(
                            deviceAddress, percent, speed, avgSpeed, currentPart, partsTotal);
                    if (hardwareUpgradingFragment.isAdded()
                            && hardwareUpgradingFragment.isVisible()) {
                        EventBus.getDefault()
                                .post(
                                        new HardWareUpdateEvent(
                                                HardwareUpgradingFragment.HardwareType.BLE,
                                                percent,
                                                HardwareUpgradingFragment.ProgressStatus.INSTALL));
                    }
                }

                @Override
                public void onError(
                        @NonNull String deviceAddress, int error, int errorType, String message) {
                    super.onError(deviceAddress, error, errorType, message);
                    if (Constant.DEVICE_NOT_BOND.equals(message)) {
                        BluetoothAdapter.getDefaultAdapter()
                                .getRemoteDevice(deviceAddress)
                                .createBond();
                    } else {
                        EventBus.getDefault().post(new ExceptionEvent(message));
                    }
                }

                @Override
                public void onDeviceConnected(@NonNull String deviceAddress) {
                    super.onDeviceConnected(deviceAddress);
                }

                @Override
                public void onEnablingDfuMode(@NonNull String deviceAddress) {
                    super.onEnablingDfuMode(deviceAddress);
                }

                @Override
                public void onDeviceDisconnected(@NonNull String deviceAddress) {
                    super.onDeviceDisconnected(deviceAddress);
                }
            };

    private void cancelDfu() {
        final Intent pauseAction = new Intent(DfuBaseService.BROADCAST_ACTION);
        pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.ACTION_ABORT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(pauseAction);
    }

    /** init */
    @Override
    public void init() {
        devices = getSharedPreferences("devices", Context.MODE_PRIVATE);
        dealBundle(Objects.requireNonNull(getIntent().getExtras()));
        hardwareUpgradeFragment = new HardwareUpgradeFragment();
        startFragment(hardwareUpgradeFragment);
        hardwareUpgradingFragment = new HardwareUpgradingFragment();
        deviceManager = DeviceManager.getInstance();
    }

    private void dealBundle(Bundle bundle) {
        currentFirmwareVersion = bundle.getString(Constant.TAG_FIRMWARE_VERSION);
        currentBleVersion = bundle.getString(Constant.TAG_NRF_VERSION);
        newFirmwareVersion = bundle.getString(Constant.TAG_FIRMWARE_VERSION_NEW);
        newBleVersion = bundle.getString(Constant.TAG_NRF_VERSION_NEW);
        firmwareChangelog = bundle.getString(Constant.TAG_FIRMWARE_UPDATE_DES);
        nrfChangelog = bundle.getString(Constant.TAG_NRF_UPDATE_DES);
        firmwareUrl = bundle.getString(Constant.TAG_FIRMWARE_DOWNLOAD_URL);
        nrfUrl = bundle.getString(Constant.TAG_NRF_DOWNLOAD_URL);
        mac = bundle.getString(Constant.BLE_MAC);
        label = bundle.getString(Constant.TAG_LABEL);
        deviceId = bundle.getString(Constant.SERIAL_NUM);
        cacheDir = getExternalCacheDir().getPath();
        isForceUpdate = bundle.getBoolean(Constant.FORCE_UPDATE, false);
        String features = devices.getString(deviceId, "");
        if (!Strings.isNullOrEmpty(features)) {
            HardwareFeatures hardware = HardwareFeatures.objectFromData(features);
            if (hardware.getBleVer().equals(newBleVersion)) {
                currentBleVersion = newBleVersion;
                newBleVersion = null;
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
        return R.layout.activity_hardware_upgrade;
    }

    @Override
    public boolean needEvents() {
        return true;
    }

    private void startUploadFirmware() {
        String path =
                String.format(
                        "%s/%s%s%s",
                        cacheDir,
                        Constant.UPDATE_FILE_NAME,
                        newFirmwareVersion,
                        Constant.FIRMWARE_UPDATE_FILE_SUFFIX);
        task =
                new MyTask(
                        new MyTask.CallBack() {
                            @Override
                            public void onSuccess() {
                                HardwareUpgradeActivity.this.onSuccess();
                            }

                            @Override
                            public void onCancel() {
                                HardwareUpgradeActivity.this.onCancel();
                            }
                        },
                        hardwareUpgradingFragment);

        task.execute(path, firmwareUrl);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateEvent(UpdateEvent event) {
        switch (event.getType()) {
            case UpdateEvent.BLE:
                startUpgrade();
                break;
            default:
        }
    }

    private void startUpgrade() {
        deviceManager.connectDeviceByMacAddress(
                mac,
                new DeviceManager.OnConnectDeviceListener<BleDevice>() {
                    @Override
                    public void onSuccess(BleDevice bleDevice) {
                        if (!Strings.isNullOrEmpty(newBleVersion)) {
                            startUploadBle();
                        } else if (!Strings.isNullOrEmpty(newFirmwareVersion)) {
                            startUploadFirmware();
                        }
                    }

                    @Override
                    public void onException(@Nullable BleDevice bleDevice, @NotNull Exception e) {
                        showPromptMessage(R.string.update_failed);
                        showUpdateComplete();
                    }
                });
    }

    private void startUploadBle() {
        String path =
                String.format(
                        "%s/%s%s%s",
                        cacheDir,
                        Constant.UPDATE_FILE_NAME,
                        newBleVersion,
                        Constant.NRF_UPDATE_FILE_SUFFIX);
        dfu(path, nrfUrl);
    }

    private void showPromptMessage(@StringRes int id) {
        runOnUiThread(
                () -> {
                    Toast.makeText(this, id, Toast.LENGTH_SHORT).show();
                });
    }

    /** 升级蓝牙固件就绪 */
    private void dfu(String path, String url) {
        File file = new File(path);
        if (file.exists()) {
            EventBus.getDefault()
                    .post(
                            new HardWareUpdateEvent(
                                    HardwareUpgradingFragment.HardwareType.BLE,
                                    100,
                                    HardwareUpgradingFragment.ProgressStatus.DOWNLOAD));
            beginDfu(path);
        } else {
            // start install ble
            OKHttpUtils.downloadFile(
                    url,
                    (currentBytes, contentLength, done) -> {
                        int progress = (int) (currentBytes * 100 / contentLength);
                        EventBus.getDefault()
                                .post(
                                        new HardWareUpdateEvent(
                                                HardwareUpgradingFragment.HardwareType.BLE,
                                                progress,
                                                HardwareUpgradingFragment.ProgressStatus.DOWNLOAD));
                        if (done) {
                            beginDfu(path);
                        }
                    },
                    new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {}

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response != null) {
                                try {
                                    InputStream is = response.body().byteStream();
                                    FileOutputStream fos = new FileOutputStream(new File(path));
                                    int len = 0;
                                    byte[] buffer = new byte[2048];
                                    while (-1 != (len = is.read(buffer))) {
                                        fos.write(buffer, 0, len);
                                    }
                                    fos.flush();
                                    fos.close();
                                    is.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }

    /** 升级蓝牙固件 */
    private void beginDfu(String path) {
        Ble.getInstance().disconnectAll();
        final DfuServiceInitiator starter = new DfuServiceInitiator(mac);
        starter.setDeviceName(label);
        starter.setKeepBond(true);
        /*
           Call this method to put Nordic nrf52832 into bootloader mode
        */
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        starter.setZip(null, path);
        DfuServiceInitiator.createDfuNotificationChannel(this);
        starter.start(this, DfuService.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
    }

    @Override
    protected void onDestroy() {
        task = null;
        cancelDfu();
        DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);
        super.onDestroy();
    }

    @OnClick(R.id.img_back)
    public void onViewClicked(View view) {
        if (hardwareUpgradingFragment.isAdded()
                && hardwareUpgradingFragment.isVisible()
                && !hardwareUpgradingFragment.getUpgradeComplete()) {
            showInterceptDialog();
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (hardwareUpgradingFragment.isAdded()
                && hardwareUpgradingFragment.isVisible()
                && !hardwareUpgradingFragment.getUpgradeComplete()) {
            showInterceptDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showInterceptDialog() {
        new BaseAlertBottomDialog.Builder(mContext)
                .setMessage(R.string.stop_update_tip)
                .setSecondaryButtonText(R.string.stop_back)
                .setTitle(R.string.stop_update)
                .setPrimaryButtonText(R.string.cancel)
                .setPrimaryButtonTextColor(R.color.color_993C3C43)
                .setSecondaryButtonTextColor(R.color.color_FF3B30)
                .setSecondaryButtonListener(
                        v -> {
                            if (Objects.nonNull(task) && !task.isCancelled()) {
                                task.cancel(true);
                                PyEnv.cancelAll();
                            }
                            finish();
                        })
                .build().showNow();
    }

    /** stm32固件升级成功回调 */
    public void onSuccess() {
        // 更新本地信息
        String features = devices.getString(deviceId, "");
        if (!Strings.isNullOrEmpty(features)) {
            HardwareFeatures features1 = HardwareFeatures.objectFromData(features);
            features1.setOneKeyVersion(newFirmwareVersion);
            devices.edit().putString(deviceId, features1.toString()).apply();
        }
        currentFirmwareVersion = newFirmwareVersion;
        newFirmwareVersion = null;
        EventBus.getDefault().post(new UpdateSuccessEvent());
    }

    /** stm32固件升级失败回调 */
    public void onCancel() {
        showPromptMessage(R.string.update_failed);
        finish();
    }

    @Override
    public void onSuccess(Object o) {
        startUploadFirmware();
    }

    @Override
    public void onException(@Nullable Object o, @NotNull Exception e) {
        showPromptMessage(R.string.update_failed);
        showUpdateComplete();
    }

    /** 固件升级的异步任务 */
    public static class MyTask extends AsyncTask<String, Object, Void> {
        private CallBack callBack;
        private HardwareUpgradingFragment fragment;

        public MyTask(CallBack callBack, HardwareUpgradingFragment fragment) {
            this.callBack = callBack;
            this.fragment = fragment;
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected Void doInBackground(String... params) {
            String path = params[0];
            String url = params[1];
            File file = new File(path);
            if (file.exists()) {
                doUpdateFirmWare(path);
            } else {
                OKHttpUtils.downloadFile(
                        url,
                        new OKHttpUtils.ProgressListener() {
                            @Override
                            public void onProgress(
                                    long currentBytes, long contentLength, boolean done) {
                                int progress = (int) (currentBytes * 100 / contentLength);
                                EventBus.getDefault()
                                        .post(
                                                new HardWareUpdateEvent(
                                                        HardwareUpgradingFragment.HardwareType
                                                                .FIRMWARE,
                                                        progress,
                                                        HardwareUpgradingFragment.ProgressStatus
                                                                .DOWNLOAD));
                                if (done) {
                                    doUpdateFirmWare(path);
                                }
                            }
                        },
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {}

                            @Override
                            public void onResponse(Call call, Response response)
                                    throws IOException {
                                if (response != null) {
                                    try {
                                        InputStream is = response.body().byteStream();
                                        FileOutputStream fos = new FileOutputStream(new File(path));
                                        int len = 0;
                                        byte[] buffer = new byte[2048];
                                        while (-1 != (len = is.read(buffer))) {
                                            fos.write(buffer, 0, len);
                                        }
                                        fos.flush();
                                        fos.close();
                                        is.close();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... progresses) {
            Utils.finishActivity(InputPinOnHardware.class);
            if (fragment.isAdded() && fragment.isResumed()) {
                EventBus.getDefault()
                        .post(
                                new HardWareUpdateEvent(
                                        HardwareUpgradingFragment.HardwareType.FIRMWARE,
                                        Integer.parseInt(((progresses[0]).toString())),
                                        HardwareUpgradingFragment.ProgressStatus.INSTALL));
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            callBack.onSuccess();
        }

        @Override
        protected void onCancelled() {
            PyEnv.cancelAll();
            callBack.onCancel();
        }

        // install firmware
        private void doUpdateFirmWare(String path) {
            if (fragment.isAdded() && fragment.isResumed()) {
                EventBus.getDefault()
                        .post(
                                new HardWareUpdateEvent(
                                        HardwareUpgradingFragment.HardwareType.FIRMWARE,
                                        100,
                                        HardwareUpgradingFragment.ProgressStatus.DOWNLOAD));
            }
            File file = new File(path);
            PyEnv.setProgressReporter(this);
            PyResponse<Void> response = PyEnv.firmwareUpdate(path);
            if (!Strings.isNullOrEmpty(response.getErrors())) {
                if (HardWareExceptions.FILE_FORMAT_ERROR
                        .getMessage()
                        .equals(response.getErrors())) {
                    Optional.of(file).ifPresent(File::delete);
                    // clear state
                    PyEnv.clearUpdateStatus();
                }
                cancel(true);
            }
        }

        interface CallBack {
            /** 升级完成回调 */
            void onSuccess();

            /** 失败回调 */
            void onCancel();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangePin(ChangePinEvent event) {
        // 回写PIN码
        if (isForceUpdate) {
            PyEnv.setPin(event.toString());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        if (isForceUpdate) {
            if (event.getType() == PyConstant.PIN_CURRENT) {
                Intent intent = new Intent(this, VerifyPinActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean keepScreenOn() {
        return true;
    }

    // start to update
    public void replaceUpgradingFragment() {
        if (!Strings.isNullOrEmpty(newFirmwareVersion) || !Strings.isNullOrEmpty(newBleVersion)) {
            hardwareUpgradingFragment = new HardwareUpgradingFragment();
            startFragment(hardwareUpgradingFragment);
        }
    }

    //  show update complete version
    public void showUpdateComplete() {
        if (hardwareUpgradeFragment != null) {
            startFragment(hardwareUpgradeFragment);
            hardwareUpgradeFragment.refreshView();
        }
    }
}
