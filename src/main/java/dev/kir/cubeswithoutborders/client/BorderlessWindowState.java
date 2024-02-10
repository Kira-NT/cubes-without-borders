package dev.kir.cubeswithoutborders.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface BorderlessWindowState {
    boolean isBorderless();

    void setBorderless(boolean borderless);
}
