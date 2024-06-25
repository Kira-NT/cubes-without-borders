package dev.kir.cubeswithoutborders.client.mixin;

import dev.kir.cubeswithoutborders.client.util.FullscreenWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.util.MonitorInfo;
import dev.kir.cubeswithoutborders.client.util.MonitorInfoContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.WindowSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(WindowSettings.class)
abstract class WindowSettingsMixin implements FullscreenWindowState, MonitorInfoContainer {
    @Shadow
    public @Final @Mutable boolean fullscreen;

    private boolean borderless;

    private MonitorInfo monitorInfo;

    @Override
    public FullscreenMode getFullscreenMode() {
        return FullscreenMode.get(this.fullscreen, this.borderless);
    }

    @Override
    public void setFullscreenMode(FullscreenMode mode) {
        this.borderless = mode == FullscreenMode.BORDERLESS;
        this.fullscreen = mode == FullscreenMode.ON;
    }

    @Override
    public MonitorInfo getMonitorInfo() {
        return this.monitorInfo == null ? MonitorInfo.primary() : this.monitorInfo;
    }

    @Override
    public void setMonitorInfo(MonitorInfo monitorInfo) {
        this.monitorInfo = monitorInfo;
    }
}
