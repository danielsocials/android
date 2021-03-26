package cn.com.heaton.blelibrary.ble.callback;

/** Created by LiuLei on 2017/10/23. */
public abstract class BleConnectCallback<T> {

    public void onConnectCancel(T device) {}

    /**
     * Set the notification feature to be successful and can send data
     *
     * @param device ble device object
     */
    public void onReady(T device) {}

    /**
     * When the callback when the error, such as app can only connect four devices at the same time
     * forcing the user to connect more than four devices will call back the method
     *
     * @param device ble device object
     * @param errorCode errorCode
     */
    public void onConnectException(T device, int errorCode) {}

    public void onConnectTimeOut(T device) {}
}
