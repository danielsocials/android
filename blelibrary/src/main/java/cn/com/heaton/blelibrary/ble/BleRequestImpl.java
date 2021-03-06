package cn.com.heaton.blelibrary.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.RequiresApi;
import androidx.core.os.HandlerCompat;
import cn.com.heaton.blelibrary.BuildConfig;
import cn.com.heaton.blelibrary.ble.callback.wrapper.ConnectWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.DescWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.MtuWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.NotifyWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.ReadRssiWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.ReadWrapperCallback;
import cn.com.heaton.blelibrary.ble.callback.wrapper.WriteWrapperCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.request.ConnectRequest;
import cn.com.heaton.blelibrary.ble.request.DescriptorRequest;
import cn.com.heaton.blelibrary.ble.request.MtuRequest;
import cn.com.heaton.blelibrary.ble.request.NotifyRequest;
import cn.com.heaton.blelibrary.ble.request.ReadRequest;
import cn.com.heaton.blelibrary.ble.request.Rproxy;
import cn.com.heaton.blelibrary.ble.request.WriteRequest;
import cn.com.heaton.blelibrary.ble.utils.ByteUtils;
import cn.com.heaton.blelibrary.ota.OtaListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BleRequestImpl<T extends BleDevice> {

    private static final String TAG = BleRequestImpl.class.getSimpleName();

    private static volatile BleRequestImpl instance;
    private Handler handler = BleHandler.of();
    private Ble.Options options;
    private Context context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private final Object locker = new Object();
    private List<BluetoothGattCharacteristic> notifyCharacteristics =
            new ArrayList<>(); // Notification attribute callback array
    private int notifyIndex = 0; // Notification feature callback list
    private BluetoothGattCharacteristic otaWriteCharacteristic; // Ota ble send the object
    private boolean otaUpdating = false; // Whether the OTA is updated
    private Map<String, BluetoothGattCharacteristic> writeCharacteristicMap = new HashMap<>();
    private Map<String, BluetoothGattCharacteristic> readCharacteristicMap = new HashMap<>();
    // Multiple device connections must put the gatt object in the collection
    private Map<String, BluetoothGatt> gattHashMap = new HashMap<>();
    // The address of the connected device
    private List<String> connectedAddressList = new ArrayList<>();
    private ConnectWrapperCallback<T> connectWrapperCallback;
    private NotifyWrapperCallback<T> notifyWrapperCallback;
    private MtuWrapperCallback<T> mtuWrapperCallback;
    private ReadRssiWrapperCallback<T> readRssiWrapperCallback;
    private ReadWrapperCallback<T> readWrapperCallback;
    private DescWrapperCallback<T> descWrapperCallback;
    private WriteWrapperCallback<T> writeWrapperCallback;
    private OtaListener otaListener; // Ota update operation listener

    private BleRequestImpl() {}

    // ???????????????????????????????????????????????????
    private final BluetoothGattCallback gattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    BluetoothDevice device = gatt.getDevice();
                    // remove timeout callback
                    cancelTimeout(device.getAddress());
                    T bleDevice = getBleDeviceInternal(device);
                    // There is a problem here Every time a new object is generated that causes the
                    // same device to be disconnected and the connection produces two objects
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            connectedAddressList.add(device.getAddress());
                            if (connectWrapperCallback != null) {
                                bleDevice.setConnectionState(BleStates.BleStatus.CONNECTED);
                                connectWrapperCallback.onConnectionChanged(bleDevice);
                            }
                            BleLog.d(TAG, "onConnectionStateChange:----device is connected.");
                            BluetoothGatt bluetoothGatt = gattHashMap.get(device.getAddress());
                            if (null != bluetoothGatt) {
                                // Attempts to discover services after successful connection.
                                BleLog.d(TAG, "Attempting to start service discovery");
                                gatt.discoverServices();
                            }
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            BleLog.d(TAG, "onConnectionStateChange:----device is disconnected.");
                            if (connectWrapperCallback != null) {
                                bleDevice.setConnectionState(BleStates.BleStatus.DISCONNECT);
                                connectWrapperCallback.onConnectionChanged(bleDevice);
                            }
                            close(device.getAddress());
                        }
                    } else {
                        // Occurrence 133 or 257 19 Equal value is not 0: Connection establishment
                        // failed due to protocol stack
                        BleLog.e(
                                TAG,
                                "onConnectionStateChange----: "
                                        + "Connection status is abnormal:"
                                        + status);
                        close(device.getAddress());
                        if (connectWrapperCallback != null) {
                            if (bleDevice != null) {
                                connectWrapperCallback.onConnectException(bleDevice);
                                bleDevice.setConnectionState(BleStates.BleStatus.DISCONNECT);
                                connectWrapperCallback.onConnectionChanged(bleDevice);
                            }
                        }
                    }
                }

                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                public void onMtuChanged(
                        android.bluetooth.BluetoothGatt gatt, int mtu, int status) {
                    if (gatt != null && gatt.getDevice() != null) {
                        BleLog.d(TAG, "onMtuChanged mtu=" + mtu + ",status=" + status);
                        if (null != mtuWrapperCallback) {
                            gatt.setPreferredPhy(
                                    BluetoothDevice.PHY_LE_2M_MASK,
                                    BluetoothDevice.PHY_LE_2M_MASK,
                                    BluetoothDevice.PHY_OPTION_NO_PREFERRED);
                            mtuWrapperCallback.onMtuChanged(
                                    getBleDeviceInternal(gatt.getDevice()), mtu, status);
                        }
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        // Empty the notification attribute list
                        notifyCharacteristics.clear();
                        notifyIndex = 0;
                        // Start setting notification feature
                        if (gatt.getServices().size() <= 3) {
                            refreshDeviceCache(gatt.getDevice().getAddress());
                            gatt.discoverServices();
                            return;
                        }
                        displayGattServices(
                                gatt.getDevice(),
                                getSupportedGattServices(gatt.getDevice().getAddress()));
                    } else {
                        BleLog.e(TAG, "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                public void onCharacteristicRead(
                        BluetoothGatt gatt,
                        BluetoothGattCharacteristic characteristic,
                        int status) {
                    BleLog.d(TAG, "onCharacteristicRead:" + status);
                    T bleDevice = getBleDeviceInternal(gatt.getDevice());
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (null != readWrapperCallback) {
                            readWrapperCallback.onReadSuccess(bleDevice, characteristic);
                        }
                    } else {
                        if (null != readWrapperCallback) {
                            readWrapperCallback.onReadFailed(
                                    getBleDeviceInternal(gatt.getDevice()), status);
                        }
                    }
                }

                @Override
                public void onCharacteristicWrite(
                        BluetoothGatt gatt,
                        BluetoothGattCharacteristic characteristic,
                        int status) {
                    synchronized (locker) {
                        T bleDevice = getBleDeviceInternal(gatt.getDevice());
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            if (null != writeWrapperCallback) {
                                writeWrapperCallback.onWriteSuccess(bleDevice, characteristic);
                            }
                            if (options.uuid_ota_write_cha.equals(characteristic.getUuid())) {
                                if (otaListener != null) {
                                    otaListener.onWrite();
                                }
                            }
                        } else {
                            if (null != writeWrapperCallback) {
                                writeWrapperCallback.onWiteFailed(bleDevice, status);
                            }
                        }
                    }
                }

                /**
                 * ?????????????????????????????????????????????????????????????????????????????????????????????????????? ???setNotify???true????????????????????????MCU????????????????????????????????????????????????????????????
                 *
                 * @param gatt ??????gatt??????
                 * @param characteristic ????????????????????????
                 */
                @Override
                public void onCharacteristicChanged(
                        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                    synchronized (locker) {
                        if (gatt.getDevice() == null) return;
                        BleLog.d(
                                TAG,
                                gatt.getDevice().getAddress()
                                        + " -- onCharacteristicChanged: "
                                        + (characteristic.getValue() != null
                                                ? ByteUtils.toHexString(characteristic.getValue())
                                                : ""));
                        T bleDevice = getBleDeviceInternal(gatt.getDevice());
                        if (notifyWrapperCallback != null) {
                            notifyWrapperCallback.onChanged(bleDevice, characteristic);
                        }
                        if (options.uuid_ota_write_cha.equals(characteristic.getUuid())
                                || options.uuid_ota_notify_cha.equals(characteristic.getUuid())) {
                            if (otaListener != null) {
                                otaListener.onChange(characteristic.getValue());
                            }
                        }
                    }
                }

                @Override
                public void onDescriptorWrite(
                        BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    UUID uuid = descriptor.getCharacteristic().getUuid();
                    BleLog.d(TAG, "onDescriptorWrite----descriptor_uuid:" + uuid);
                    synchronized (locker) {
                        T bleDevice = getBleDeviceInternal(gatt.getDevice());
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            if (null != descWrapperCallback) {
                                descWrapperCallback.onDescWriteSuccess(bleDevice, descriptor);
                            }
                            if (notifyCharacteristics.size() > 0
                                    && notifyIndex < notifyCharacteristics.size()) {
                                BleLog.d(
                                        TAG,
                                        "====setCharacteristicNotification notifyIndex is "
                                                + notifyIndex);
                                setCharacteristicNotification(gatt.getDevice().getAddress(), true);
                            } else {
                                BleLog.d(TAG, "====setCharacteristicNotification is completed===");
                                if (notifyWrapperCallback != null) {
                                    if (Arrays.equals(
                                                    descriptor.getValue(),
                                                    BluetoothGattDescriptor
                                                            .ENABLE_NOTIFICATION_VALUE)
                                            || Arrays.equals(
                                                    descriptor.getValue(),
                                                    BluetoothGattDescriptor
                                                            .ENABLE_INDICATION_VALUE)) {
                                        notifyWrapperCallback.onNotifySuccess(bleDevice);
                                    } else if (Arrays.equals(
                                            descriptor.getValue(),
                                            BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)) {
                                        notifyWrapperCallback.onNotifyCanceled(bleDevice);
                                    }
                                }
                            }
                        } else {
                            if (null != descWrapperCallback) {
                                descWrapperCallback.onDescWriteFailed(bleDevice, status);
                            }
                        }
                    }
                }

                @Override
                public void onDescriptorRead(
                        BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                    super.onDescriptorRead(gatt, descriptor, status);
                    UUID uuid = descriptor.getCharacteristic().getUuid();
                    BleLog.d(TAG, "onDescriptorRead----descriptor_uuid:" + uuid);
                    T bleDevice = getBleDeviceInternal(gatt.getDevice());
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (null != descWrapperCallback) {
                            descWrapperCallback.onDescReadSuccess(bleDevice, descriptor);
                        }
                    } else {
                        if (null != descWrapperCallback) {
                            descWrapperCallback.onDescReadFailed(bleDevice, status);
                        }
                    }
                }

                @Override
                public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                    BleLog.d(TAG, "onReadRemoteRssi----rssi: " + rssi);
                    if (null != readRssiWrapperCallback) {
                        readRssiWrapperCallback.onReadRssiSuccess(
                                getBleDeviceInternal(gatt.getDevice()), rssi);
                    }
                }
            };

    private T getBleDeviceInternal(BluetoothDevice device) {
        return Ble.<T>getInstance().getBleDevice(device);
    }

    /** @return ??????????????????????????? */
    public List<BluetoothDevice> getConnectedDevices() {
        if (bluetoothManager == null) return null;
        return bluetoothManager.getConnectedDevices(BluetoothProfile.GATT);
    }

    public static BleRequestImpl getBleRequest() {
        if (instance == null) {
            synchronized (BleRequestImpl.class) {
                if (instance == null) {
                    instance = new BleRequestImpl();
                }
            }
        }
        return instance;
    }

    boolean initialize(Context context) {
        this.connectWrapperCallback = Rproxy.getRequest(ConnectRequest.class);
        this.notifyWrapperCallback = Rproxy.getRequest(NotifyRequest.class);
        this.mtuWrapperCallback = Rproxy.getRequest(MtuRequest.class);
        this.readWrapperCallback = Rproxy.getRequest(ReadRequest.class);
        this.readRssiWrapperCallback = Rproxy.getRequest(ReadRssiWrapperCallback.class);
        this.writeWrapperCallback = Rproxy.getRequest(WriteRequest.class);
        this.descWrapperCallback = Rproxy.getRequest(DescriptorRequest.class);
        this.context = context;
        this.options = Ble.options();
        if (bluetoothManager == null) {
            bluetoothManager =
                    (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                BleLog.e(TAG, "Unable to initBLE BluetoothManager.");
                return false;
            }
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            BleLog.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    void release() {
        connectWrapperCallback = null;
        mtuWrapperCallback = null;
        notifyWrapperCallback = null;
        readRssiWrapperCallback = null;
        readWrapperCallback = null;
        writeWrapperCallback = null;
        handler.removeCallbacksAndMessages(null);
        BleLog.d(TAG, "BleRequestImpl is released");
    }

    public void cancelTimeout(String address) {
        handler.removeCallbacksAndMessages(address);
    }

    private boolean verifyBluetoothState() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            BleLog.e(TAG, "Bluetooth is not turned on");
            return false;
        }
        return true;
    }

    /**
     * ????????????
     *
     * @param address Bluetooth address
     * @return Connection result
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean connect(final String address) {
        //        if (connectedAddressList.contains(address)) {
        //            BleLog.d(TAG, "This is device already connected.");
        //            return true;
        //        }
        if (bluetoothAdapter == null) {
            BleLog.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        // getRemoteDevice(address) will throw an exception if the device address is invalid,
        // so it's necessary to check the address
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            BleLog.d(TAG, "the device address is invalid");
            return false;
        }
        // Previously connected device. Try to reconnect. ()
        final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            BleLog.d(TAG, "no device");
            return false;
        }
        // 10s after the timeout prompt
        HandlerCompat.postDelayed(
                handler,
                new Runnable() {
                    @Override
                    public void run() {
                        connectWrapperCallback.onConnectTimeOut(getBleDeviceInternal(device));
                        close(device.getAddress());
                    }
                },
                device.getAddress(),
                options.connectTimeout);
        if (connectWrapperCallback != null) {
            T bleDevice = getBleDeviceInternal(device);
            bleDevice.setConnectionState(BleStates.BleStatus.CONNECTING);
            connectWrapperCallback.onConnectionChanged(bleDevice);
        }
        // We want to directly connect to the device, so we are setting the autoConnect parameter to
        // false
        BluetoothGatt bluetoothGatt =
                device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
        if (bluetoothGatt != null) {
            gattHashMap.put(address, bluetoothGatt);
            BleLog.d(TAG, "Trying to create a new connection.");
            return true;
        }
        return false;
    }

    /**
     * ????????????
     *
     * @param address ????????????
     */
    public void disconnect(String address) {
        if (verifyParams(address)) return;
        boolean isValidAddress = BluetoothAdapter.checkBluetoothAddress(address);
        if (!isValidAddress) {
            BleLog.e(TAG, "the device address is invalid");
            return;
        }
        gattHashMap.get(address).disconnect();
        notifyIndex = 0;
        notifyCharacteristics.clear();
        writeCharacteristicMap.remove(address);
        readCharacteristicMap.remove(address);
        otaWriteCharacteristic = null;
    }

    /**
     * ???????????????????????????????????????????????????
     *
     * @param address ????????????
     */
    public void close(String address) {
        BluetoothGatt gatt = gattHashMap.get(address);
        if (gatt != null) {
            gatt.close();
            gattHashMap.remove(address);
        }
        connectedAddressList.remove(address);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public boolean setMtu(String address, int mtu) {
        if (verifyParams(address)) return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mtu > 20) {
                if (gattHashMap.get(address) != null) {
                    boolean result = gattHashMap.get(address).requestMtu(mtu);
                    BleLog.d(TAG, "requestMTU " + mtu + " result=" + result);
                    return result;
                }
            }
        }
        return false;
    }

    /** ?????????????????????????????? */
    public void close() {
        for (String address : connectedAddressList) {
            BluetoothGatt gatt = gattHashMap.get(address);
            if (gatt != null) {
                gatt.close();
            }
        }
        gattHashMap.clear();
        connectedAddressList.clear();
    }

    /** ?????????????????? */
    public boolean refreshDeviceCache(String address) {
        BluetoothGatt gatt = gattHashMap.get(address);
        if (gatt != null) {
            try {
                Method localMethod = gatt.getClass().getMethod("refresh", new Class[0]);
                boolean bool = (Boolean) localMethod.invoke(gatt, new Object[0]);
                return bool;
            } catch (Exception localException) {
                BleLog.e(TAG, "An exception occured while refreshing device");
            }
        }
        return false;
    }

    /**
     * ????????????
     *
     * @param address ????????????
     * @param value ?????????????????????
     * @return ??????????????????(?????????????????????????????????)
     */
    public boolean wirteCharacteristic(String address, byte[] value) {
        if (verifyParams(address)) return false;
        BluetoothGattCharacteristic gattCharacteristic = writeCharacteristicMap.get(address);
        if (gattCharacteristic != null) {
            try {
                if (options.uuid_write_cha.equals(gattCharacteristic.getUuid())) {
                    gattCharacteristic.setValue(value);
                    boolean result =
                            gattHashMap.get(address).writeCharacteristic(gattCharacteristic);
                    BleLog.d(TAG, address + " -- write data:" + result);
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean wirteCharacteristicByUuid(
            String address, byte[] value, UUID serviceUUID, UUID characteristicUUID) {
        if (verifyParams(address)) return false;
        BluetoothGatt bluetoothGatt = gattHashMap.get(address);
        BluetoothGattCharacteristic characteristic =
                gattCharacteristic(bluetoothGatt, serviceUUID, characteristicUUID);
        if (characteristic != null) {
            try {
                characteristic.setValue(value);
                boolean result = bluetoothGatt.writeCharacteristic(characteristic);
                BleLog.d(TAG, address + " -- write data:" + ByteUtils.toHexString(value));
                BleLog.d(TAG, address + " -- write result:" + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public BluetoothGattCharacteristic gattCharacteristic(
            BluetoothGatt gatt, UUID serviceUUID, UUID characteristicUUID) {
        BluetoothGattService gattService = gatt.getService(serviceUUID);
        if (gattService == null) {
            BleLog.e(TAG, "serviceUUID is null");
            return null;
        }
        BluetoothGattCharacteristic characteristic =
                gattService.getCharacteristic(characteristicUUID);
        if (characteristic == null) {
            BleLog.e(TAG, "characteristicUUID is null");
            return null;
        }
        return characteristic;
    }

    /**
     * ????????????
     *
     * @param address ????????????
     * @return ??????????????????(?????????????????????????????????)
     */
    public boolean readCharacteristic(String address) {
        if (verifyParams(address)) return false;
        BluetoothGattCharacteristic gattCharacteristic = readCharacteristicMap.get(address);
        if (gattCharacteristic != null) {
            try {
                if (options.uuid_read_cha.equals(gattCharacteristic.getUuid())) {
                    boolean result =
                            gattHashMap.get(address).readCharacteristic(gattCharacteristic);
                    BleLog.d(TAG, address + " -- read result:" + result);
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean readCharacteristicByUuid(
            String address, UUID serviceUUID, UUID characteristicUUID) {
        if (verifyParams(address)) return false;
        BluetoothGatt bluetoothGatt = gattHashMap.get(address);
        BluetoothGattCharacteristic gattCharacteristic =
                gattCharacteristic(bluetoothGatt, serviceUUID, characteristicUUID);
        if (gattCharacteristic != null) {
            try {
                boolean result = bluetoothGatt.readCharacteristic(gattCharacteristic);
                BleLog.d(TAG, address + " -- read result:" + result);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean readDescriptor(
            String address, UUID serviceUUID, UUID characteristicUUID, UUID descriptorUUID) {
        if (verifyParams(address)) return false;
        BluetoothGatt bluetoothGatt = gattHashMap.get(address);
        BluetoothGattCharacteristic gattCharacteristic =
                gattCharacteristic(bluetoothGatt, serviceUUID, characteristicUUID);
        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(descriptorUUID);
        if (descriptor != null) {
            return gattHashMap.get(address).readDescriptor(descriptor);
        }
        return false;
    }

    public boolean writeDescriptor(
            String address,
            byte[] data,
            UUID serviceUUID,
            UUID characteristicUUID,
            UUID descriptorUUID) {
        if (verifyParams(address)) return false;
        BluetoothGatt bluetoothGatt = gattHashMap.get(address);
        BluetoothGattCharacteristic gattCharacteristic =
                gattCharacteristic(bluetoothGatt, serviceUUID, characteristicUUID);
        BluetoothGattDescriptor descriptor = gattCharacteristic.getDescriptor(descriptorUUID);
        if (descriptor != null) {
            descriptor.setValue(data);
            return bluetoothGatt.writeDescriptor(descriptor);
        }
        return false;
    }

    private boolean verifyParams(String address) {
        if (bluetoothAdapter == null || gattHashMap.get(address) == null) {
            BleLog.e(TAG, "BluetoothAdapter or BluetoothGatt is null");
            return true;
        }
        return false;
    }

    /**
     * ????????????rssi
     *
     * @param address ????????????
     * @return ????????????rssi??????
     */
    public boolean readRssi(String address) {
        if (verifyParams(address)) return false;
        try {
            boolean result = gattHashMap.get(address).readRemoteRssi();
            BleLog.d(TAG, address + " -- read result:" + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ????????????????????????????????????
     *
     * @param address ????????????
     * @param enabled ????????????????????????
     */
    public void setCharacteristicNotification(String address, boolean enabled) {
        if (verifyParams(address)) return;
        if (notifyCharacteristics.size() > 0 && notifyIndex < notifyCharacteristics.size()) {
            BluetoothGattCharacteristic characteristic = notifyCharacteristics.get(notifyIndex++);
            setCharacteristicNotificationInternal(
                    gattHashMap.get(address), characteristic, enabled);
        }
    }

    public void setCharacteristicNotificationByUuid(
            String address, boolean enabled, UUID serviceUUID, UUID characteristicUUID) {
        if (verifyParams(address)) return;
        BluetoothGatt bluetoothGatt = gattHashMap.get(address);
        BluetoothGattCharacteristic characteristic =
                gattCharacteristic(bluetoothGatt, serviceUUID, characteristicUUID);
        setCharacteristicNotificationInternal(bluetoothGatt, characteristic, enabled);
    }

    private void setCharacteristicNotificationInternal(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, boolean enabled) {
        gatt.setCharacteristicNotification(characteristic, enabled);
        // If the number of descriptors in the eigenvalue of the notification is greater than zero
        if (characteristic.getDescriptors().size() > 0) {
            // Filter descriptors based on the uuid of the descriptor
            List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
            for (BluetoothGattDescriptor descriptor : descriptors) {
                if (descriptor != null) {
                    // Write the description value
                    if ((characteristic.getProperties()
                                    & BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                            != 0) {
                        descriptor.setValue(
                                enabled
                                        ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                        : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    } else if ((characteristic.getProperties()
                                    & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                            != 0) {
                        // ??????????????????????????????notify???indication??????????????????notify??????????????????????????????????????????????????????????????????
                        // ??????????????????????????????????????????indication???????????????????????????????????????????????????ack???????????????????????????????????????
                        // ?????????ack???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        descriptor.setValue(
                                enabled
                                        ? BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                                        : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
                    }
                    gatt.writeDescriptor(descriptor);
                    BleLog.d(TAG, "setCharacteristicNotificationInternal is " + enabled);
                }
            }
        }
    }

    /**
     * @param device ??????
     * @param gattServices ??????????????????
     */
    private void displayGattServices(
            final BluetoothDevice device, List<BluetoothGattService> gattServices) {
        if (gattServices == null || device == null) {
            BleLog.e(TAG, "displayGattServices gattServices or device is null");
            close(device.getAddress());
            return;
        }
        if (gattServices.isEmpty()) {
            BleLog.e(TAG, "displayGattServices gattServices size is 0");
            disconnect(device.getAddress());
            return;
        }
        if (connectWrapperCallback != null) {
            T bleDevice = getBleDeviceInternal(device);
            connectWrapperCallback.onServicesDiscovered(bleDevice, gattServices);
        }
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            String uuid = gattService.getUuid().toString();
            BleLog.d(TAG, "discovered gattServices: " + uuid);
            if (uuid.equals(options.uuid_service.toString()) || isContainUUID(uuid)) {
                BleLog.d(TAG, "service_uuid is set up successfully:" + uuid);
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    /*int charaProp = gattCharacteristic.getProperties();
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) != 0) {
                        Log.e(TAG, "The readable UUID for gattCharacteristic is:" + gattCharacteristic.getUuid());
                        readCharacteristicMap.put(address, gattCharacteristic);
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) {
                        Log.e(TAG, "The writable UUID for gattCharacteristic is:" + gattCharacteristic.getUuid());
                        writeCharacteristicMap.put(address, gattCharacteristic);
                    }
                    if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                        Log.e(TAG, "The PROPERTY_NOTIFY characteristic's UUID:" + gattCharacteristic.getUuid());
                        mNotifyCharacteristics.add(gattCharacteristic);
                    }
                    if((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0){
                        Log.e(TAG, "The PROPERTY_INDICATE characteristic's UUID:" + gattCharacteristic.getUuid());
                        mNotifyCharacteristics.add(gattCharacteristic);
                    }*/
                    uuid = gattCharacteristic.getUuid().toString();
                    BleLog.d(TAG, "characteristic_uuid: " + uuid);
                    if (uuid.equals(options.uuid_write_cha.toString())) {
                        BleLog.d("mWriteCharacteristic", uuid);
                        gattCharacteristic.setWriteType(
                                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        writeCharacteristicMap.put(device.getAddress(), gattCharacteristic);
                        // Notification feature
                    }
                    if (uuid.equals(options.uuid_read_cha.toString())) {
                        BleLog.d("mReadCharacteristic", uuid);
                        readCharacteristicMap.put(device.getAddress(), gattCharacteristic);
                        // Notification feature
                    }
                    if ((gattCharacteristic.getProperties()
                                    & BluetoothGattCharacteristic.PROPERTY_NOTIFY)
                            != 0) {
                        notifyCharacteristics.add(gattCharacteristic);
                        BleLog.d("mNotifyCharacteristics", "PROPERTY_NOTIFY");
                    }
                    if ((gattCharacteristic.getProperties()
                                    & BluetoothGattCharacteristic.PROPERTY_INDICATE)
                            != 0) {
                        notifyCharacteristics.add(gattCharacteristic);
                        BleLog.d("mNotifyCharacteristics", "PROPERTY_INDICATE");
                    }
                }
                /*//Really set up notifications
                if (notifyCharacteristics != null && notifyCharacteristics.size() > 0) {
                    BleLog.d("setCharaNotification", "setCharaNotification");
                    setCharacteristicNotification(address, notifyCharacteristics.get(notifyIndex++), true);
                }*/
                if (null != connectWrapperCallback) {
                    connectWrapperCallback.onReady(getBleDeviceInternal(device));
                }
            }
        }
    }

    // ???????????????uuid
    private boolean isContainUUID(String uuid) {
        for (UUID u : options.uuid_services_extra) {
            if (u != null && uuid.equals(u.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * ????????????????????????
     *
     * @param address ????????????
     * @return ??????????????????
     */
    public BluetoothGattCharacteristic getWriteCharacteristic(String address) {
        synchronized (locker) {
            if (writeCharacteristicMap != null) {
                return writeCharacteristicMap.get(address);
            }
            return null;
        }
    }

    /**
     * ????????????????????????
     *
     * @param address ????????????
     * @return ??????????????????
     */
    public BluetoothGattCharacteristic getReadCharacteristic(String address) {
        synchronized (locker) {
            if (readCharacteristicMap != null) {
                return readCharacteristicMap.get(address);
            }
            return null;
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be invoked
     * only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @param address ble address
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices(String address) {
        if (gattHashMap.get(address) == null) return null;

        return gattHashMap.get(address).getServices();
    }

    public BluetoothGatt getBluetoothGatt(String address) {
        return gattHashMap.get(address);
    }

    /**
     * ??????OTA??????
     *
     * @param address ????????????
     * @param value ??????????????????
     * @return ??????????????????
     */
    public boolean writeOtaData(String address, byte[] value) {
        if (verifyParams(address)) return false;
        try {
            if (otaWriteCharacteristic == null) {
                otaUpdating = true;
                BluetoothGattService bluetoothGattService =
                        gattHashMap.get(address).getService(options.uuid_ota_service);
                if (bluetoothGattService == null) {
                    return false;
                } else {
                    BluetoothGattCharacteristic mOtaNotifyCharacteristic =
                            bluetoothGattService.getCharacteristic(options.uuid_ota_notify_cha);
                    if (mOtaNotifyCharacteristic != null) {
                        gattHashMap
                                .get(address)
                                .setCharacteristicNotification(mOtaNotifyCharacteristic, true);
                    }
                    otaWriteCharacteristic =
                            bluetoothGattService.getCharacteristic(options.uuid_ota_write_cha);
                }
            }
            if (otaWriteCharacteristic != null
                    && options.uuid_ota_write_cha.equals(otaWriteCharacteristic.getUuid())) {
                otaWriteCharacteristic.setValue(value);
                boolean result =
                        writeCharacteristic(gattHashMap.get(address), otaWriteCharacteristic);
                BleLog.d(TAG, address + " -- write data:" + Arrays.toString(value));
                BleLog.d(TAG, address + " -- write result:" + result);
                return result;
            }
            return true;
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                e.printStackTrace();
            }
            close();
            return false;
        }
    }

    // The basic method of writing data
    private boolean writeCharacteristic(
            BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        synchronized (locker) {
            return !(gatt == null || characteristic == null)
                    && gatt.writeCharacteristic(characteristic);
        }
    }

    /** OTA???????????? */
    public void otaUpdateComplete() {
        otaUpdating = false;
    }

    /**
     * ??????OTA??????????????????
     *
     * @param updating ????????????
     */
    public void setOtaUpdating(boolean updating) {
        this.otaUpdating = updating;
    }

    /**
     * ??????OTA??????????????????
     *
     * @param otaListener ????????????
     */
    public void setOtaListener(OtaListener otaListener) {
        this.otaListener = otaListener;
    }
}
