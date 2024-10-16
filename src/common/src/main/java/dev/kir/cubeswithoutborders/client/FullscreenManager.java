package dev.kir.cubeswithoutborders.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public interface FullscreenManager {
    static FullscreenManager getInstance() {
        return (FullscreenManager)(Object)MinecraftClient.getInstance().getWindow();
    }

    FullscreenMode getFullscreenMode();

    void setFullscreenMode(FullscreenMode fullscreenMode);
}
