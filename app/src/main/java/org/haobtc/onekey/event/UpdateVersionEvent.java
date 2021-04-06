package org.haobtc.onekey.event;

public class UpdateVersionEvent {
    public int hardwareType;
    public String version;

    public UpdateVersionEvent(int hardwareType, String version) {
        this.hardwareType = hardwareType;
        this.version = version;
    }
}
