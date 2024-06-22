package dev.kir.cubeswithoutborders.client.option;

import dev.kir.cubeswithoutborders.client.util.MonitorInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.option.SimpleOption;

@Environment(EnvType.CLIENT)
public interface FullscreenOptions {
    SimpleOption<MonitorInfo> getFullscreenMonitor();

    SimpleOption<FullscreenMode> getFullscreenMode();

    SimpleOption<FullscreenMode> getPreferredFullscreenMode();
}
