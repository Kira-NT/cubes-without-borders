package dev.kir.cubeswithoutborders.mixin;

import dev.kir.cubeswithoutborders.client.BorderlessWindowState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.WindowSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(WindowSettings.class)
abstract class WindowSettingsMixin implements BorderlessWindowState {
    @Shadow
    public @Final @Mutable boolean fullscreen;

    private boolean borderless;

    @Override
    public boolean isBorderless() {
        return this.borderless;
    }

    @Override
    public void setBorderless(boolean borderless) {
        this.borderless = borderless;
        this.fullscreen = this.fullscreen && !borderless;
    }
}
