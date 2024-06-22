package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Monitor;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public final class MonitorInfo {
    private final int viewportX;

    private final int viewportY;

    private final int width;

    private final int height;

    public MonitorInfo(int viewportX, int viewportY, int width, int height) {
        this.viewportX = viewportX;
        this.viewportY = viewportY;
        this.width = width;
        this.height = height;
    }

    public static MonitorInfo of(Monitor monitor) {
        int x = monitor.getViewportX();
        int y = monitor.getViewportY();
        int width = monitor.getCurrentVideoMode().getWidth();
        int height = monitor.getCurrentVideoMode().getHeight();
        return new MonitorInfo(x, y, width, height);
    }

    public static MonitorInfo primary() {
        return new MonitorInfo(0, 0, -1, -1);
    }

    public static Optional<MonitorInfo> parse(String format) {
        if (format == null) {
            return Optional.empty();
        }

        String[] stringParts = format.split(",");
        int[] parts = new int[4];
        parts[0] = parts[1] = 0;
        parts[2] = parts[3] = -1;

        int length = Math.min(stringParts.length, parts.length);
        for (int i = 0; i < length; i++) {
            try {
                parts[i] = Integer.parseInt(stringParts[i]);
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        return Optional.of(new MonitorInfo(parts[0], parts[1], parts[2], parts[3]));
    }

    public int getViewportX() {
        return this.viewportX;
    }

    public int getViewportY() {
        return this.viewportY;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    @Override
    public String toString() {
        return this.viewportX + "," + this.viewportY + "," + this.width + "," + this.height;
    }
}
