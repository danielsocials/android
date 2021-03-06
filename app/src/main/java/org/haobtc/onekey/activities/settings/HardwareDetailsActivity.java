package org.haobtc.onekey.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.OnClick;
import com.google.common.base.Strings;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.CenterPopupView;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.onekey.aop.SingleClick;
import org.haobtc.onekey.asynctask.BusinessAsyncTask;
import org.haobtc.onekey.bean.HardwareFeatures;
import org.haobtc.onekey.bean.HardwareVerifyResponse;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.bean.UpdateInfo;
import org.haobtc.onekey.business.language.LanguageManager;
import org.haobtc.onekey.business.version.VersionManager;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.constant.PyConstant;
import org.haobtc.onekey.event.BleConnectionEx;
import org.haobtc.onekey.event.ButtonRequestEvent;
import org.haobtc.onekey.event.ChangePinEvent;
import org.haobtc.onekey.event.ConnectedEvent;
import org.haobtc.onekey.event.ExitEvent;
import org.haobtc.onekey.event.FixBixinkeyNameEvent;
import org.haobtc.onekey.event.GotVerifyInfoEvent;
import org.haobtc.onekey.event.NotifySuccessfulEvent;
import org.haobtc.onekey.event.PinInputComplete;
import org.haobtc.onekey.event.PostVerifyInfoEvent;
import org.haobtc.onekey.event.UpdateVersionEvent;
import org.haobtc.onekey.event.VerifyFailedEvent;
import org.haobtc.onekey.event.VerifySuccessEvent;
import org.haobtc.onekey.event.WipeEvent;
import org.haobtc.onekey.exception.HardWareExceptions;
import org.haobtc.onekey.manager.BleManager;
import org.haobtc.onekey.manager.PreferencesManager;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.ui.activity.CheckXpubActivity;
import org.haobtc.onekey.ui.activity.ConfirmOnHardWareActivity;
import org.haobtc.onekey.ui.activity.HardwareUpgradeActivity;
import org.haobtc.onekey.ui.activity.PinNewActivity;
import org.haobtc.onekey.ui.activity.ResetDevicePromoteActivity;
import org.haobtc.onekey.ui.activity.VerifyHardwareActivity;
import org.haobtc.onekey.ui.activity.VerifyPinActivity;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.dialog.ConnectingDialog;
import org.haobtc.onekey.ui.dialog.DeleteLocalDeviceDialog;
import org.haobtc.onekey.ui.dialog.InvalidDeviceIdWarningDialog;
import org.haobtc.onekey.ui.fragment.HardwareUpgradingFragment;

/** @author liyan */
public class HardwareDetailsActivity extends BaseActivity implements BusinessAsyncTask.Helper {

    public static final String TAG =
            "org.haobtc.onekey.activities.settings.HardwareDetailsActivity";
    public static final String TAG_VERIFICATION = "VERIFICATION";

    @BindView(R.id.img_back)
    ImageView imgBack;

    @BindView(R.id.tet_keyName)
    TextView tetKeyName;

    @BindView(R.id.lin_OnckOne)
    RelativeLayout linOnckOne;

    @BindView(R.id.tet_code)
    TextView tetCode;

    @BindView(R.id.lin_OnckTwo)
    LinearLayout linOnckTwo;

    @BindView(R.id.change_pin)
    LinearLayout changePin;

    @BindView(R.id.wipe_device)
    LinearLayout wipeDevice;

    @BindView(R.id.verified)
    TextView verified;

    @BindView(R.id.check_xpub)
    TextView checkXpub;

    @BindView(R.id.hide_wallet)
    LinearLayout hideWalletLayout;

    public static boolean dismiss;
    private String bleName;
    private String serialNum;
    private String label;
    private String bleMac;
    private String firmwareVersion;
    private String nrfVersion;
    private String currentMethod;
    private CenterPopupView dialog;
    private VersionManager mVersionManager;

    @SingleClick(value = 6000L)
    @OnClick({
        R.id.img_back,
        R.id.lin_OnckOne,
        R.id.lin_OnckTwo,
        R.id.change_pin,
        R.id.wipe_device,
        R.id.tetBuckup,
        R.id.tet_deleteWallet,
        R.id.tetVerification,
        R.id.check_xpub,
        R.id.text_hide_wallet
    })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                PyEnv.cancelPinInput();
                finish();
                break;
            case R.id.lin_OnckOne:
                Intent intent =
                        new Intent(HardwareDetailsActivity.this, OneKeyMessageActivity.class);
                intent.putExtra(Constant.TAG_BLE_NAME, bleName);
                intent.putExtra(Constant.TAG_LABEL, label);
                intent.putExtra(Constant.SERIAL_NUM, serialNum);
                intent.putExtra(Constant.TAG_FIRMWARE_VERSION, firmwareVersion);
                intent.putExtra(Constant.TAG_NRF_VERSION, nrfVersion);
                startActivity(intent);
                break;
            case R.id.lin_OnckTwo:
                if (Strings.isNullOrEmpty(bleMac)) {
                    showToast(getString(R.string.unknown_device));
                    return;
                }
                currentMethod = PyConstant.FIRMWARE_UPDATE;
                initBle();
                break;
            case R.id.change_pin:
                if (Strings.isNullOrEmpty(bleMac)) {
                    showToast(getString(R.string.unknown_device));
                    return;
                }
                currentMethod = BusinessAsyncTask.CHANGE_PIN;
                initBle();
                break;
            case R.id.wipe_device:
                if (Strings.isNullOrEmpty(bleMac)) {
                    showToast(getString(R.string.unknown_device));
                    return;
                }
                currentMethod = BusinessAsyncTask.WIPE_DEVICE;
                initBle();
                break;
            case R.id.tetBuckup:
                Intent intent7 = new Intent(this, BackupRecoveryActivity.class);
                intent7.putExtra("ble_name", bleName);
                startActivity(intent7);
                break;
            case R.id.tet_deleteWallet:
                new XPopup.Builder(mContext)
                        .dismissOnTouchOutside(false)
                        .isDestroyOnDismiss(true)
                        .asCustom(
                                new DeleteLocalDeviceDialog(
                                        mContext,
                                        serialNum,
                                        new DeleteLocalDeviceDialog.onClick() {
                                            @Override
                                            public void onBack() {
                                                finish();
                                            }
                                        }))
                        .show();
                break;
            case R.id.tetVerification:
                if (Strings.isNullOrEmpty(bleMac)) {
                    showToast(getString(R.string.unknown_device));
                    return;
                }
                currentMethod = BusinessAsyncTask.COUNTER_VERIFICATION;
                initBle();
                break;
            case R.id.check_xpub:
                if (Strings.isNullOrEmpty(bleMac)) {
                    showToast(getString(R.string.unknown_device));
                    return;
                }
                currentMethod = BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY;
                initBle();
                break;
            case R.id.text_hide_wallet:
                showToast(R.string.support_less_promote);
                break;
            default:
        }
    }

    private void verifyHardware() {
        String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
        new BusinessAsyncTask()
                .setHelper(this)
                .execute(
                        BusinessAsyncTask.COUNTER_VERIFICATION,
                        strRandom,
                        MyApplication.getInstance().getDeviceWay());
    }

    private void initBle() {
        showConnecting();
        BleManager bleManager = BleManager.getInstance(this);
        bleManager.connDevByMac(bleMac);
    }

    private void changePin() {
        new BusinessAsyncTask()
                .setHelper(this)
                .execute(BusinessAsyncTask.CHANGE_PIN, MyApplication.getInstance().getDeviceWay());
    }

    private void wipeDevice() {
        new BusinessAsyncTask()
                .setHelper(this)
                .execute(BusinessAsyncTask.WIPE_DEVICE, MyApplication.getInstance().getDeviceWay());
    }

    private void getXpub() {
        new BusinessAsyncTask()
                .setHelper(this)
                .execute(
                        BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY,
                        MyApplication.getInstance().getDeviceWay());
    }

    private void getUpdateInfo(boolean isBootloader) {
        Single.create(
                        (SingleOnSubscribe<UpdateInfo>)
                                emitter -> {
                                    UpdateInfo forceVersionInfo =
                                            mVersionManager.getForceLocalVersionInfo(
                                                    HardwareDetailsActivity.this);
                                    emitter.onSuccess(forceVersionInfo);
                                })
                .doFinally(this::dismissProgress)
                .doOnSubscribe(disposable -> showProgress())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new SingleObserver<UpdateInfo>() {
                            @Override
                            public void onSubscribe(
                                    @io.reactivex.rxjava3.annotations.NonNull Disposable d) {}

                            @Override
                            public void onSuccess(
                                    @io.reactivex.rxjava3.annotations.NonNull
                                            UpdateInfo updateInfo) {
                                if (updateInfo == null) {
                                    showToast(R.string.get_update_info_failed);
                                    return;
                                }
                                String urlPrefix = "https://onekey.so/";
                                String locate =
                                        LanguageManager.getInstance()
                                                .getLocalLanguage(HardwareDetailsActivity.this);
                                String info = updateInfo.toString();
                                Bundle bundle = getBundle(urlPrefix, locate, info, isBootloader);
                                Intent intentVersion =
                                        new Intent(
                                                HardwareDetailsActivity.this,
                                                HardwareUpgradeActivity.class);
                                intentVersion.putExtras(bundle);
                                startActivity(intentVersion);
                            }

                            @Override
                            public void onError(
                                    @io.reactivex.rxjava3.annotations.NonNull Throwable e) {}
                        });
    }

    @NonNull
    private Bundle getBundle(String urlPrefix, String locate, String info, boolean isBootloader) {
        UpdateInfo updateInfo = UpdateInfo.objectFromData(info);
        String urlNrf = updateInfo.getNrf().getUrl();
        String urlStm32 = updateInfo.getStm32().getUrl();
        String versionNrf = updateInfo.getNrf().getVersion();
        String versionStm32 = updateInfo.getStm32().getVersion().toString().replace(",", ".");
        versionStm32 = versionStm32.substring(1, versionStm32.length() - 1).replaceAll("\\s+", "");
        String descriptionNrf =
                "English".equals(locate)
                        ? updateInfo.getNrf().getChangelogEn()
                        : updateInfo.getNrf().getChangelogCn();
        String descriptionStm32 =
                "English".equals(locate)
                        ? updateInfo.getStm32().getChangelogEn()
                        : updateInfo.getStm32().getChangelogCn();
        if (urlNrf.startsWith("https") || urlStm32.startsWith("https")) {
            urlPrefix = "";
        }
        Bundle bundle = new Bundle();
        List<Integer> firmwareCurrentVersion = new ArrayList<>();
        Arrays.asList(firmwareVersion.split("\\."))
                .forEach(
                        (s) -> {
                            firmwareCurrentVersion.add(Integer.valueOf(s));
                        });
        List<Integer> firmwareNewVersion = updateInfo.getStm32().getVersion();
        if (firmwareNewVersion.get(0).compareTo(firmwareCurrentVersion.get(0)) > 0
                || firmwareNewVersion.get(1).compareTo(firmwareCurrentVersion.get(1)) > 0
                || firmwareNewVersion.get(2).compareTo(firmwareCurrentVersion.get(2)) > 0) {
            bundle.putString(Constant.TAG_FIRMWARE_DOWNLOAD_URL, urlPrefix + urlStm32);
            bundle.putString(Constant.TAG_FIRMWARE_VERSION_NEW, versionStm32);
            bundle.putString(Constant.TAG_FIRMWARE_UPDATE_DES, descriptionStm32);
        }
        boolean showNrf = getShowNrf(isBootloader, versionNrf);
        if (showNrf) {
            bundle.putString(Constant.TAG_NRF_DOWNLOAD_URL, urlPrefix + urlNrf);
            bundle.putString(Constant.TAG_NRF_VERSION_NEW, versionNrf);
            bundle.putString(Constant.TAG_NRF_UPDATE_DES, descriptionNrf);
        }
        bundle.putString(Constant.TAG_BLE_NAME, bleName);
        bundle.putString(Constant.TAG_FIRMWARE_VERSION, firmwareVersion);
        bundle.putString(Constant.TAG_NRF_VERSION, nrfVersion);
        bundle.putString(Constant.BLE_MAC, bleMac);
        bundle.putString(Constant.TAG_LABEL, label);
        bundle.putString(Constant.SERIAL_NUM, serialNum);
        return bundle;
    }

    /**
     * @param isBootloader ????????? Bootloader ?????????????????????????????????????????????????????????
     * @param versionNrf
     * @return
     */
    private boolean getShowNrf(boolean isBootloader, String versionNrf) {
        if (isBootloader) {
            return true;
        } else {
            if (!Strings.isNullOrEmpty(nrfVersion) && !Strings.isNullOrEmpty(versionNrf)) {
                return versionNrf.compareTo(nrfVersion) > 0
                        || Objects.equals(nrfVersion, Constant.BLE_OLDEST_VER);
            } else {
                return false;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReadyBle(NotifySuccessfulEvent event) {
        if (Objects.nonNull(dialog) && dialog.isShow()) {
            dialog.dismiss();
        }
        if (Objects.equals(currentMethod, PyConstant.FIRMWARE_UPDATE)
                && !hasWindowFocus()
                && !dialog.isShow()) {
            return;
        }
        PyEnv.getFeature(this, this::dealResponse);
    }

    public void showConnecting() {
        if (Objects.isNull(dialog)) {
            dialog =
                    (CenterPopupView)
                            new XPopup.Builder(mContext)
                                    .dismissOnTouchOutside(false)
                                    .dismissOnBackPressed(false)
                                    .asCustom(new ConnectingDialog(mContext));
        }
        dialog.show();
        dialog.postDelayed(
                () -> {
                    if (Objects.nonNull(dialog) && dialog.isShow()) {
                        dialog.dismiss();
                    }
                },
                10000);
    }

    private void dealResponse(PyResponse<HardwareFeatures> response) {
        HardwareFeatures features;
        String errors = response.getErrors();
        features = response.getResult();
        runOnUiThread(
                () -> {
                    if (Strings.isNullOrEmpty(errors) && Objects.nonNull(features)) {
                        if (!Objects.equals(serialNum, features.getSerialNum())
                                && !features.isBootloaderMode()) {
                            new InvalidDeviceIdWarningDialog()
                                    .show(getSupportFragmentManager(), "");
                            return;
                        } else if (features.isBootloaderMode()
                                && !Objects.equals(PyConstant.FIRMWARE_UPDATE, currentMethod)) {
                            new InvalidDeviceIdWarningDialog()
                                    .show(getSupportFragmentManager(), "");
                            return;
                        }
                    } else {
                        showToast(R.string.get_hard_msg_error);
                        return;
                    }
                    switch (currentMethod) {
                        case BusinessAsyncTask.CHANGE_PIN:
                            if (features.isInitialized()) {
                                changePin();
                            } else {
                                showToast(R.string.un_init_device_support_less);
                            }
                            break;
                        case BusinessAsyncTask.WIPE_DEVICE:
                            startActivity(new Intent(this, ResetDevicePromoteActivity.class));
                            break;
                        case BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY:
                            if (features.isInitialized()) {
                                getXpub();
                            } else {
                                showToast(R.string.un_init_device_support_less);
                            }
                            break;
                        case BusinessAsyncTask.COUNTER_VERIFICATION:
                            Intent intent1 = new Intent(this, VerifyHardwareActivity.class);
                            intent1.putExtra(
                                    Constant.BLE_INFO, Optional.ofNullable(label).orElse(bleName));
                            startActivity(intent1);
                            new Handler()
                                    .postDelayed(
                                            () -> {
                                                EventBus.getDefault().post(new ConnectedEvent());
                                            },
                                            1000);
                            new Handler().postDelayed(this::verifyHardware, 2000);
                            break;
                        case PyConstant.FIRMWARE_UPDATE:
                            getUpdateInfo(features.isBootloaderMode());
                            break;
                        default:
                    }
                });
    }

    @Subscribe
    public void onConnectionTimeout(BleConnectionEx connectionEx) {
        if (connectionEx == BleConnectionEx.BLE_CONNECTION_EX_TIMEOUT) {
            EventBus.getDefault().post(new ExitEvent());
            Toast.makeText(this, R.string.ble_connect_timeout, Toast.LENGTH_SHORT).show();
        }
        if (Objects.nonNull(dialog) && dialog.isShow()) {
            dialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChangePin(ChangePinEvent event) {
        // ??????PIN???
        PyEnv.setPin(event.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        switch (event.getType()) {
            case PyConstant.PIN_CURRENT:
                Intent intent = new Intent(this, VerifyPinActivity.class);
                if (BusinessAsyncTask.CHANGE_PIN.equals(currentMethod)) {
                    intent.setAction(BusinessAsyncTask.CHANGE_PIN);
                }
                startActivity(intent);
                break;
            case PyConstant.BUTTON_REQUEST_7:
                if (hasWindowFocus()) {
                    showToast(R.string.confirm_hardware_msg);
                } else {
                    if (BusinessAsyncTask.CHANGE_PIN.equals(currentMethod)) {
                        boolean isVerifyPinOnHardwareSp =
                                (boolean)
                                        PreferencesManager.get(
                                                mContext,
                                                "Preferences",
                                                Constant.PIN_VERIFY_ON_HARDWARE,
                                                true);
                        if (isVerifyPinOnHardwareSp) {
                            EventBus.getDefault().post(new PinInputComplete());
                        } else {
                            startActivity(new Intent(this, ConfirmOnHardWareActivity.class));
                            EventBus.getDefault().post(new ExitEvent());
                        }
                    }
                }
                break;
            case PyConstant.BUTTON_REQUEST_6:
                if (BusinessAsyncTask.WIPE_DEVICE.equals(currentMethod)) {
                    Intent intent1 = new Intent(this, ConfirmOnHardWareActivity.class);
                    intent1.setAction(BusinessAsyncTask.WIPE_DEVICE);
                    startActivity(intent1);
                } else {
                    startActivity(new Intent(this, ConfirmOnHardWareActivity.class));
                }
                EventBus.getDefault().post(new ExitEvent());
                break;
            case PyConstant.PIN_NEW_FIRST:
                startActivity(new Intent(this, PinNewActivity.class));
            default:
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWipeDevice(WipeEvent event) {
        wipeDevice();
    }

    private void verification(String result) {
        try {
            HardwareVerifyResponse verifyResponse = HardwareVerifyResponse.objectFromData(result);
            if (verifyResponse.isIsBixinkey() && verifyResponse.isIsVerified()) {
                verified.setVisibility(View.VISIBLE);
                EventBus.getDefault().post(new VerifySuccessEvent());
                // ????????????????????????
                String str =
                        PreferencesManager.get(
                                        HardwareDetailsActivity.this,
                                        Constant.DEVICES,
                                        serialNum,
                                        "")
                                .toString();
                HardwareFeatures features = HardwareFeatures.objectFromData(str);
                features.setVerify(true);
                PreferencesManager.put(
                        HardwareDetailsActivity.this,
                        Constant.DEVICES,
                        serialNum,
                        features.toString());
            } else {
                EventBus.getDefault()
                        .post(new VerifyFailedEvent(VerifyFailedEvent.FailedReason.VERIFY_FAILED));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(FixBixinkeyNameEvent event) {
        tetKeyName.setText(event.getKeyName());
    }

    /** init */
    @Override
    public void init() {
        Intent intent = getIntent();
        boolean isBackupOnly = intent.getBooleanExtra(Constant.TAG_IS_BACKUP_ONLY, false);
        if (isBackupOnly) {
            checkXpub.setVisibility(View.GONE);
            hideWalletLayout.setVisibility(View.GONE);
        }
        bleName = intent.getStringExtra(Constant.TAG_BLE_NAME);
        serialNum = intent.getStringExtra(Constant.SERIAL_NUM);
        label = intent.getStringExtra(Constant.TAG_LABEL);
        firmwareVersion = getIntent().getStringExtra(Constant.TAG_FIRMWARE_VERSION);
        nrfVersion = getIntent().getStringExtra(Constant.TAG_NRF_VERSION);
        tetKeyName.setText(label);
        boolean isVerified = intent.getBooleanExtra(Constant.TAG_HARDWARE_VERIFY, false);
        verified.setVisibility(isVerified ? View.VISIBLE : View.GONE);
        bleMac = PreferencesManager.get(this, Constant.BLE_INFO, bleName, "").toString();
        mVersionManager = new VersionManager();
    }

    @Override
    protected void onDestroy() {
        PyEnv.cancelAll();
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPreExecute() {}

    @Override
    public void onException(Exception e) {
        switch (currentMethod) {
            case BusinessAsyncTask.CHANGE_PIN:
            case BusinessAsyncTask.WIPE_DEVICE:
            case BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY:
                if (HardWareExceptions.PIN_INVALID.getMessage().equals(e.getMessage())) {
                    showToast(R.string.pin_wrong);
                } else {
                    showToast(R.string.fail);
                }
                EventBus.getDefault().post(new ExitEvent());
                break;
            case BusinessAsyncTask.COUNTER_VERIFICATION:
                EventBus.getDefault()
                        .post(new VerifyFailedEvent(VerifyFailedEvent.FailedReason.NETWORK_ERROR));
                break;
        }
        currentMethod = "";
    }

    @Override
    public void onResult(String s) {
        switch (currentMethod) {
            case BusinessAsyncTask.CHANGE_PIN:
            case BusinessAsyncTask.WIPE_DEVICE:
                if ("0".equals(s)) {
                    showToast(R.string.pin_wrong);
                    EventBus.getDefault().post(new ExitEvent());
                } else if ("1".equals(s)) {
                    EventBus.getDefault().post(new PinInputComplete());
                }
                break;
            case BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY:
                Intent intent = new Intent(this, CheckXpubActivity.class);
                intent.putExtra(Constant.EXTEND_PUBLIC_KEY, s);
                startActivity(intent);
                EventBus.getDefault().post(new ExitEvent());
                break;
            case BusinessAsyncTask.COUNTER_VERIFICATION:
                EventBus.getDefault().post(new GotVerifyInfoEvent());
                EventBus.getDefault().post(new PostVerifyInfoEvent());
                verification(s);
                break;
            default:
        }
        currentMethod = "";
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateVersion(UpdateVersionEvent updateVersionEvent) {
        switch (updateVersionEvent.hardwareType) {
            case HardwareUpgradingFragment.HardwareType.BLE:
                nrfVersion = updateVersionEvent.version;
                break;
            case HardwareUpgradingFragment.HardwareType.FIRMWARE:
                firmwareVersion = updateVersionEvent.version;
                break;
        }
    }

    @Override
    public void onCancelled() {}

    @Override
    public void currentMethod(String methodName) {
        currentMethod = methodName;
    }

    /**
     * * init layout
     *
     * @return
     */
    @Override
    public int getContentViewId() {
        return R.layout.activity_somemore;
    }

    @Override
    public boolean needEvents() {
        return true;
    }
}
