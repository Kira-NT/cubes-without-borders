package dev.kir.cubeswithoutborders.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;

@Environment(EnvType.CLIENT)
public interface FullscreenType {
    String id();

    boolean supported();

    default int priority() {
        return 0;
    }

    void enable(Window window, Monitor monitor, VideoMode videoMode);

    void disable(Window window);
}
