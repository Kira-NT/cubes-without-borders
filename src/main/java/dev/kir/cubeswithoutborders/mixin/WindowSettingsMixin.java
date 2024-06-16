package dev.kir.cubeswithoutborders.mixin;

import dev.kir.cubeswithoutborders.client.FullscreenWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.WindowSettings;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(WindowSettings.class)
abstract class WindowSettingsMixin implements FullscreenWindowState {
    @Shadow
    public @Final @Mutable boolean fullscreen;

    private boolean borderless;

    private boolean prefersBorderless;

    @Override
    public FullscreenMode getFullscreenMode() {
        return FullscreenMode.get(this.fullscreen, this.borderless);
    }

    @Override
    public void setFullscreenMode(FullscreenMode mode) {
        this.borderless = mode == FullscreenMode.BORDERLESS;
        this.fullscreen = mode == FullscreenMode.ON;
        this.prefersBorderless = this.prefersBorderless & !this.fullscreen | this.borderless;
    }

    @Override
    public FullscreenMode getPreferredFullscreenMode() {
        return this.prefersBorderless ? FullscreenMode.BORDERLESS : FullscreenMode.ON;
    }

    @Override
    public void setPreferredFullscreenMode(FullscreenMode mode) {
        this.prefersBorderless = mode == FullscreenMode.BORDERLESS;
    }
}
