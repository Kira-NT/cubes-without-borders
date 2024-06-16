package dev.kir.cubeswithoutborders.client;

import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FullscreenWindowState {
    FullscreenMode getFullscreenMode();

    void setFullscreenMode(FullscreenMode mode);
}
