package cn.com.heaton.blelibrary.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.IntRange;
import androidx.annotation.RequiresApi;
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleDisConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleGlobalConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleMtuCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotiftCallback;
import cn.com.heaton.blelibrary.ble.callback.BleReadCallback;
import cn.com.heaton.blelibrary.ble.callback.BleReadDescCallback;
import cn.com.heaton.blelibrary.ble.callback.BleReadRssiCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleStatusCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteDescCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteEntityCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.BleWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.BluetoothChangedObserver;
import cn.com.heaton.blelibrary.ble.callback.wrapper.DefaultBleWrapperCallback;
import cn.com.heaton.blelibrary.ble.exception.BleException;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.model.EntityData;
import cn.com.heaton.blelibrary.ble.proxy.RequestImpl;
import cn.com.heaton.blelibrary.ble.proxy.RequestLisenter;
import cn.com.heaton.blelibrary.ble.proxy.RequestProxy;
import cn.com.heaton.blelibrary.ble.queue.RequestTask;
import cn.com.heaton.blelibrary.ble.queue.WriteQueue;
import cn.com.heaton.blelibrary.ble.request.ConnectRequest;
import cn.com.heaton.blelibrary.ble.request.DescriptorRequest;
import cn.com.heaton.blelibrary.ble.request.Rproxy;
import cn.com.heaton.blelibrary.ble.request.ScanRequest;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** ??????????????????????????????????????????API Created by jerry on 2016/12/7. */
public final class Ble<T extends BleDevice> {

    private static final String TAG = "Ble";
    private static volatile Ble sInstance;
    private static volatile Options options;
    private static final long DEFALUT_WRITE_DELAY = 50L;
    private Context context;
    private RequestLisenter<T> request;
    private final Object locker = new Object();
    private BleRequestImpl bleRequestImpl;
    // ?????????????????????
    public static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothChangedObserver bleObserver;

    /**
     * Initializes a newly created {@code Ble} object so that it represents a bluetooth management
     * class . Note that use of this constructor is unnecessary since Can not be externally
     * constructed.
     */
    private Ble() {}

    /**
     * ???????????????
     *
     * @param context ???????????????
     * @return ?????????????????????
     */
    public void init(Context context, Options options) {
        if (this.context != null) {
            BleLog.d(TAG, "Ble is Initialized!");
            throw new BleException("Ble is Initialized!");
        }
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Ble.options = (options == null ? options() : options);
        BleLog.init();
        // ??????????????????
        request =
                (RequestLisenter)
                        RequestProxy.newProxy().bindProxy(context, RequestImpl.newRequestImpl());
        bleRequestImpl = BleRequestImpl.getBleRequest();
        bleRequestImpl.initialize(context);
        BleLog.d(TAG, "Ble init success!");
    }

    public static Ble<BleDevice> create(Context context) {
        return create(context, options());
    }

    public static Ble<BleDevice> create(Context context, Options options) {
        Ble<BleDevice> ble = getInstance();
        ble.init(context, options);
        return ble;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param callback
     */
    public void setBleStatusCallback(BleStatusCallback callback) {
        if (bleObserver == null) {
            this.bleObserver = new BluetoothChangedObserver(context);
            this.bleObserver.setBleScanCallbackInner(callback);
            this.bleObserver.registerReceiver();
        }
    }

    /**
     * ????????????
     *
     * @param callback ????????????
     */
    public void startScan(BleScanCallback<T> callback) {
        request.startScan(callback, options().scanPeriod);
    }

    public void startScan(BleScanCallback<T> callback, long scanPeriod) {
        request.startScan(callback, scanPeriod);
    }

    /** ???????????? */
    public void stopScan() {
        request.stopScan();
    }

    /**
     * ????????????
     *
     * @param device ??????????????????
     */
    public void connect(T device, BleConnectCallback<T> callback) {
        synchronized (locker) {
            request.connect(device, callback);
        }
    }

    public void setGlobalConnectStatusCallback(BleGlobalConnectCallback<T> callback) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            request.setGlobalConnectStatusCallback(callback);
        }
    }

    /**
     * ??????mac??????????????????
     *
     * @param address mac??????
     * @param callback ????????????
     */
    public void connect(String address, BleConnectCallback<T> callback) {
        synchronized (locker) {
            request.connect(address, callback);
        }
    }

    public void connects(List<T> devices, BleConnectCallback<T> callback) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            request.connect(devices, callback);
        }
    }

    public void cancelConnectting(T device) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            request.cancelConnectting(device);
        }
    }

    /**
     * ??????????????????????????????
     *
     * @param device ????????????
     * @param autoConnect ??????????????????
     */
    public void autoConnect(T device, boolean autoConnect) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            request.resetReConnect(device, autoConnect);
        }
    }

    /**
     * ???????????? ?????????
     *
     * @param device ??????????????????
     */
    public void disconnect(T device) {
        request.disconnect(device);
    }

    /**
     * ???????????? ?????????
     *
     * @param device ??????????????????
     */
    public void disconnect(T device, BleDisConnectCallback<T> callback) {
        request.disconnect(device, callback);
    }

    public void disconnectAll() {
        request.disconnectAll();
    }

    /**
     * ????????????????????????????????????
     *
     * @param device ??????????????????
     * @param callback ????????????
     * @deprecated Use {@link Ble#enableNotify(T, boolean, BleNotiftCallback)} instead.
     */
    public void startNotify(T device, BleNotiftCallback<T> callback) {
        request.notify(device, callback);
    }

    /**
     * ????????????
     *
     * @param device ??????????????????
     * @deprecated Use {@link Ble#enableNotify(T, boolean, BleNotiftCallback)} instead.
     */
    public void cancelNotify(T device, BleNotiftCallback<T> callback) {
        request.cancelNotify(device, callback);
    }

    /**
     * ????????????
     *
     * @param device ??????????????????
     * @param enable ??????/??????
     * @param callback ????????????
     */
    public void enableNotify(T device, boolean enable, BleNotiftCallback<T> callback) {
        request.enableNotify(device, enable, callback);
    }

    /**
     * ??????uuid??????????????????
     *
     * @param device ??????????????????
     * @param enable ??????/??????
     * @param serviceUUID ??????uuid
     * @param characteristicUUID ????????????uuid
     * @param callback ????????????
     */
    public void enableNotifyByUuid(
            T device,
            boolean enable,
            UUID serviceUUID,
            UUID characteristicUUID,
            BleNotiftCallback<T> callback) {
        request.enableNotifyByUuid(device, enable, serviceUUID, characteristicUUID, callback);
    }

    /**
     * ????????????
     *
     * @param device ??????????????????
     * @param callback ??????????????????
     */
    public boolean read(T device, BleReadCallback<T> callback) {
        return request.read(device, callback);
    }

    /**
     * ???????????????uuid??????
     *
     * @param device ??????????????????
     * @param serviceUUID ??????uuid
     * @param characteristicUUID ????????????uuid
     * @param callback ????????????
     */
    public boolean readByUuid(
            T device, UUID serviceUUID, UUID characteristicUUID, BleReadCallback<T> callback) {
        return request.readByUuid(device, serviceUUID, characteristicUUID, callback);
    }

    public boolean readDesByUuid(
            T device,
            UUID serviceUUID,
            UUID characteristicUUID,
            UUID descriptorUUID,
            BleReadDescCallback<T> callback) {
        DescriptorRequest<T> request = Rproxy.getRequest(DescriptorRequest.class);
        if (request != null) {
            return request.readDes(
                    device, serviceUUID, characteristicUUID, descriptorUUID, callback);
        }
        return false;
    }

    public boolean writeDesByUuid(
            T device,
            byte[] data,
            UUID serviceUUID,
            UUID characteristicUUID,
            UUID descriptorUUID,
            BleWriteDescCallback<T> callback) {
        DescriptorRequest<T> request = Rproxy.getRequest(DescriptorRequest.class);
        if (request != null) {
            return request.writeDes(
                    device, data, serviceUUID, characteristicUUID, descriptorUUID, callback);
        }
        return false;
    }

    /**
     * ????????????RSSI
     *
     * @param device ??????????????????
     * @param callback ????????????RSSI????????????
     */
    public void readRssi(T device, BleReadRssiCallback<T> callback) {
        request.readRssi(device, callback);
    }

    /**
     * ??????MTU
     *
     * @param address ??????????????????
     * @param mtu mtu??????
     * @return ??????????????????
     */
    public boolean setMTU(String address, int mtu, BleMtuCallback<T> callback) {
        return request.setMtu(address, mtu, callback);
    }

    /**
     * ????????????
     *
     * @param device ??????????????????
     * @param data ????????????????????????
     * @param callback ??????????????????
     * @return ??????????????????
     */
    public boolean write(T device, byte[] data, BleWriteCallback<T> callback) {
        return request.write(device, data, callback);
    }

    /**
     * ???????????????uuid??????
     *
     * @param device ??????????????????
     * @param data ??????
     * @param serviceUUID ??????uuid
     * @param characteristicUUID ????????????uuid
     * @param callback ????????????
     */
    public boolean writeByUuid(
            T device,
            byte[] data,
            UUID serviceUUID,
            UUID characteristicUUID,
            BleWriteCallback<T> callback) {
        return request.writeByUuid(device, data, serviceUUID, characteristicUUID, callback);
    }

    public void writeQueueDelay(long delay, RequestTask task) {
        WriteQueue.getInstance().put(delay, task);
    }

    public void writeQueue(RequestTask task) {
        writeQueueDelay(DEFALUT_WRITE_DELAY, task);
    }

    /**
     * ???????????????????????????????????????
     *
     * @param device ??????????????????
     * @param data ????????????????????????????????????????????????????????????
     * @param packLength ???????????????????????????
     * @param delay ???????????????????????????
     * @param callback ??????????????????
     * @deprecated Use {@link Ble#writeEntity(EntityData, BleWriteEntityCallback)} instead.
     */
    public void writeEntity(
            T device,
            final byte[] data,
            @IntRange(from = 1, to = 20) int packLength,
            int delay,
            BleWriteEntityCallback<T> callback) {
        request.writeEntity(device, data, packLength, delay, callback);
    }

    /**
     * ???????????????????????????????????????(??????) ?????????????????????????????????????????????????????????????????????????????????????????????????????????(??????)
     *
     * @param entityData ????????????
     * @param callback ????????????
     */
    public void writeEntity(EntityData entityData, BleWriteEntityCallback<T> callback) {
        request.writeEntity(entityData, callback);
    }

    public void cancelWriteEntity() {
        request.cancelWriteEntity();
    }

    public static <T extends BleDevice> Ble<T> getInstance() {
        if (sInstance == null) {
            synchronized (Ble.class) {
                if (sInstance == null) {
                    sInstance = new Ble();
                }
            }
        }
        return sInstance;
    }

    /**
     * ?????????????????????????????????
     *
     * @return ???????????????????????????
     */
    public BleRequestImpl getBleRequest() {
        return bleRequestImpl;
    }

    /**
     * ????????????????????????????????????
     *
     * @param address ????????????
     * @return ?????????????????????
     */
    public T getBleDevice(String address) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            return request.getBleDevice(address);
        }
        return null;
    }

    /**
     * ????????????????????????
     *
     * @param device ??????????????????
     * @return ??????????????????
     */
    public T getBleDevice(BluetoothDevice device) {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            return request.getBleDevice(device);
        }
        return null;
    }

    /** ????????????????????? */
    public Object getLocker() {
        return locker;
    }

    /** ?????????????????? */
    public boolean isScanning() {
        ScanRequest request = Rproxy.getRequest(ScanRequest.class);
        return request.isScanning();
    }

    /** @return ??????????????????????????? */
    public List<T> getConnetedDevices() {
        ConnectRequest<T> request = Rproxy.getRequest(ConnectRequest.class);
        if (request != null) {
            return request.getConnetedDevices();
        }
        return Collections.emptyList();
    }

    /** ?????????????????? */
    public void released() {
        releaseGatts();
        releaseBleObserver();
        if (isScanning()) {
            stopScan();
        }
        bleRequestImpl.release();
        bleRequestImpl = null;
        Rproxy.release();
        context = null;
        BleLog.d(TAG, "AndroidBLE already released");
    }

    /** Release Empty all resources */
    private void releaseGatts() {
        BleLog.d(TAG, "BluetoothGatts is released");
        synchronized (locker) {
            List<T> connetedDevices = getConnetedDevices();
            for (T bleDevice : connetedDevices) {
                disconnect(bleDevice);
            }
        }
    }

    private void releaseBleObserver() {
        BleLog.d(TAG, "BleObserver is released");
        if (bleObserver != null) {
            bleObserver.unregisterReceiver();
            bleObserver = null;
        }
    }

    /** @return ?????????????????? */
    public boolean isSupportBle(Context context) {
        return (bluetoothAdapter != null
                && context.getPackageManager()
                        .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE));
    }

    /** @return ?????????????????? */
    public boolean isBleEnable() {
        return bluetoothAdapter.isEnabled();
    }

    /**
     * ????????????(????????????--??????????????????)
     *
     * @param activity ???????????????
     */
    public void turnOnBlueTooth(Activity activity) {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!isBleEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    /** ???????????????????????????????????????????????? */
    public void turnOnBlueToothNo() {
        if (!isBleEnable()) {
            bluetoothAdapter.enable();
        }
    }

    /** ???????????? */
    public boolean turnOffBlueTooth() {
        return !bluetoothAdapter.isEnabled() || bluetoothAdapter.disable();
    }

    /**
     * ??????????????????
     *
     * @param address ??????????????????
     * @return ??????????????????
     */
    public boolean refreshDeviceCache(String address) {
        if (bleRequestImpl != null) {
            return bleRequestImpl.refreshDeviceCache(address);
        }
        return false;
    }

    public static Options options() {
        if (options == null) {
            options = new Options();
        }
        return options;
    }

    public Context getContext() {
        return context;
    }

    /** ??????????????????????????? */
    public static class Options {

        /** ???????????????????????? */
        public boolean logBleEnable = true;
        /** ??????TAG??????????????????????????? */
        public String logTAG = "AndroidBLE";
        /** ???????????????????????? */
        public boolean throwBleException = true;
        /** ?????????????????????????????????????????? */
        public boolean autoConnect = false;
        /** ???????????????????????? */
        public long connectTimeout = 10 * 1000L;
        /** ???????????????????????? */
        public long scanPeriod = 10 * 1000L;
        /** ?????????????????????????????? */
        public int serviceBindFailedRetryCount = 3;
        /** ?????????????????????????????? */
        public int connectFailedRetryCount = 3;
        /** ???????????????????????? */
        public boolean isFilterScan = false;
        /** ????????????????????? (????????????????????????????????????) */
        public boolean isParseScanData = false;
        /** ?????????,??????id */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public int manufacturerId = 65520; // 0xfff0

        public BleWrapperCallback bleWrapperCallback = new DefaultBleWrapperCallback();

        public Options setScanPeriod(long scanPeriod) {
            this.scanPeriod = scanPeriod;
            return this;
        }

        public String getLogTAG() {
            return logTAG;
        }

        public Options setLogTAG(String logTAG) {
            this.logTAG = logTAG;
            return this;
        }

        public boolean isLogBleEnable() {
            return logBleEnable;
        }

        public Options setLogBleEnable(boolean logBleEnable) {
            this.logBleEnable = logBleEnable;
            return this;
        }

        public boolean isThrowBleException() {
            return throwBleException;
        }

        public Options setThrowBleException(boolean throwBleException) {
            this.throwBleException = throwBleException;
            return this;
        }

        public boolean isAutoConnect() {
            return autoConnect;
        }

        public Options setAutoConnect(boolean autoConnect) {
            this.autoConnect = autoConnect;
            return this;
        }

        public long getConnectTimeout() {
            return connectTimeout;
        }

        public Options setConnectTimeout(long connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public long getScanPeriod() {
            return scanPeriod;
        }

        public int getServiceBindFailedRetryCount() {
            return serviceBindFailedRetryCount;
        }

        public Options setServiceBindFailedRetryCount(int serviceBindFailedRetryCount) {
            this.serviceBindFailedRetryCount = serviceBindFailedRetryCount;
            return this;
        }

        public int getConnectFailedRetryCount() {
            return connectFailedRetryCount;
        }

        public Options setConnectFailedRetryCount(int connectFailedRetryCount) {
            this.connectFailedRetryCount = connectFailedRetryCount;
            return this;
        }

        public boolean isFilterScan() {
            return isFilterScan;
        }

        public Options setFilterScan(boolean filterScan) {
            isFilterScan = filterScan;
            return this;
        }

        public boolean isParseScanData() {
            return isParseScanData;
        }

        public Options setParseScanData(boolean parseScanData) {
            isParseScanData = parseScanData;
            return this;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public int getManufacturerId() {
            return manufacturerId;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public Options setManufacturerId(int manufacturerId) {
            this.manufacturerId = manufacturerId;
            return this;
        }

        public BleWrapperCallback getBleWrapperCallback() {
            return bleWrapperCallback;
        }

        public Options setBleWrapperCallback(DefaultBleWrapperCallback defaultBleWrapperCallback) {
            this.bleWrapperCallback = defaultBleWrapperCallback;
            return this;
        }

        UUID[] uuid_services_extra = new UUID[] {};
        UUID uuid_service = UUID.fromString("0000fee9-0000-1000-8000-00805f9b34fb");
        UUID uuid_write_cha = UUID.fromString("d44bc439-abfd-45a2-b575-925416129600");
        UUID uuid_read_cha = UUID.fromString("d44bc439-abfd-45a2-b575-925416129600");
        UUID uuid_notify = UUID.fromString("d44bc439-abfd-45a2-b575-925416129601");
        UUID uuid_notify_desc = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        UUID uuid_ota_service = UUID.fromString("0000fee8-0000-1000-8000-00805f9b34fb");
        UUID uuid_ota_notify_cha = UUID.fromString("003784cf-f7e3-55b4-6c4c-9fd140100a16");
        UUID uuid_ota_write_cha = UUID.fromString("013784cf-f7e3-55b4-6c4c-9fd140100a16");

        public UUID[] getUuidServicesExtra() {
            return uuid_services_extra;
        }

        public Options setUuidServicesExtra(UUID[] uuid_services_extra) {
            this.uuid_services_extra = uuid_services_extra;
            return this;
        }

        public UUID getUuidService() {
            return uuid_service;
        }

        public Options setUuidService(UUID uuid_service) {
            this.uuid_service = uuid_service;
            return this;
        }

        public UUID getUuidWriteCha() {
            return uuid_write_cha;
        }

        public Options setUuidWriteCha(UUID uuid_write_cha) {
            this.uuid_write_cha = uuid_write_cha;
            return this;
        }

        public UUID getUuidReadCha() {
            return uuid_read_cha;
        }

        public Options setUuidReadCha(UUID uuid_read_cha) {
            this.uuid_read_cha = uuid_read_cha;
            return this;
        }

        public UUID getUuidNotify() {
            return uuid_notify;
        }

        public Options setUuidNotify(UUID uuid_notify) {
            this.uuid_notify = uuid_notify;
            return this;
        }

        public UUID getUuidNotifyDesc() {
            return uuid_notify_desc;
        }

        public Options setUuidNotifyDesc(UUID uuid_notify_desc) {
            this.uuid_notify_desc = uuid_notify_desc;
            return this;
        }

        public UUID getUuidOtaService() {
            return uuid_ota_service;
        }

        public Options setUuidOtaService(UUID uuid_ota_service) {
            this.uuid_ota_service = uuid_ota_service;
            return this;
        }

        public UUID getUuidOtaNotifyCha() {
            return uuid_ota_notify_cha;
        }

        public Options setUuidOtaNotifyCha(UUID uuid_ota_notify_cha) {
            this.uuid_ota_notify_cha = uuid_ota_notify_cha;
            return this;
        }

        public UUID getUuidOtaWriteCha() {
            return uuid_ota_write_cha;
        }

        public Options setUuidOtaWriteCha(UUID uuid_ota_write_cha) {
            this.uuid_ota_write_cha = uuid_ota_write_cha;
            return this;
        }

        public Ble<BleDevice> create(Context context) {
            return Ble.create(context);
        }
    }
}
