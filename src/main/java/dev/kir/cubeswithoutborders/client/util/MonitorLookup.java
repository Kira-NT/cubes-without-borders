package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.VideoMode;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class MonitorLookup {
    public static Optional<Monitor> findMonitor(MonitorTracker monitorTracker, MonitorInfo monitorInfo) {
        int x = monitorInfo.getViewportX();
        int y = monitorInfo.getViewportY();
        int width = monitorInfo.getWidth();
        int height = monitorInfo.getHeight();

        return MonitorLookup.findMonitor(monitorTracker, x, y, width, height);
    }

    public static Optional<Monitor> findMonitor(MonitorTracker monitorTracker, int x, int y, int width, int height) {
        for (Monitor monitor : monitorTracker.pointerToMonitorMap.values()) {
            if (monitor.getViewportX() != x || monitor.getViewportY() != y) {
                continue;
            }

            if (!MonitorLookup.matchesDimensions(monitor, width, height)) {
                continue;
            }

            return Optional.of(monitor);
        }

        return Optional.empty();
    }

    private static boolean matchesDimensions(Monitor monitor, int width, int height) {
        int modeCount = monitor.getVideoModeCount();
        for (int i = 0; i < modeCount; i++) {
            VideoMode mode = monitor.getVideoMode(i);
            if (width >= 0 && mode.getWidth() != width) {
                continue;
            }

            if (height >= 0 && mode.getHeight() != height) {
                continue;
            }

            return true;
        }

        return false;
    }

    private MonitorLookup() { }
}
