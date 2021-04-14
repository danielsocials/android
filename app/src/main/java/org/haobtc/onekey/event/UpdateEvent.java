package org.haobtc.onekey.event;

/**
 * @author liyan
 * @date 12/4/20
 */
public enum UpdateEvent {
    /** 同时有蓝牙和stm32的更新 */
    ALL,
    /** 同时有蓝牙的更新 */
    BLE_ONLY,
    /** 只有stm32的更新 */
    FIRMWARE_ONLY
}
