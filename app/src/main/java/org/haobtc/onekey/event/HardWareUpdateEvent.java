package org.haobtc.onekey.event;

public class HardWareUpdateEvent {
    public int hardwareType;
    public int progress;
    public int status;

    public HardWareUpdateEvent(int hardwareType, int progress, int status) {
        this.hardwareType = hardwareType;
        this.progress = progress;
        this.status = status;
    }
}
