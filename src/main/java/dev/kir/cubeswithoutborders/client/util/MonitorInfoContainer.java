package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MonitorInfoContainer {
    MonitorInfo getMonitorInfo();

    void setMonitorInfo(MonitorInfo monitorInfo);
}
