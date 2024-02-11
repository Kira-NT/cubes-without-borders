package dev.kir.cubeswithoutborders.client.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;

@Environment(EnvType.CLIENT)
public interface FullscreenOptions {
    SimpleOption<FullscreenMode> getFullscreenMode();
}
