package dev.kir.cubeswithoutborders.mixin;

import com.mojang.serialization.Codec;
import dev.kir.cubeswithoutborders.client.BorderlessWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;

@Environment(EnvType.CLIENT)
@Mixin(GameOptions.class)
abstract class GameOptionsMixin implements FullscreenOptions {
    private SimpleOption<FullscreenMode> fullscreenMode;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;load()V", shift = At.Shift.BEFORE))
    private void init(MinecraftClient client, File optionsFile, CallbackInfo ci) {
        this.fullscreenMode = new SimpleOption<>(
            "options.fullscreen",
            SimpleOption.emptyTooltip(),
            SimpleOption.enumValueText(),
            new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(FullscreenMode.values()), Codec.INT.xmap(FullscreenMode::get, FullscreenMode::getId)),
            FullscreenMode.OFF,
            value -> {
                Window window = MinecraftClient.getInstance().getWindow();
                BorderlessWindowState borderlessWindow = (BorderlessWindowState)(Object)window;
                if (window == null || value == FullscreenMode.of(window)) {
                    return;
                }

                this.getFullscreen().setValue(value == FullscreenMode.ON);
                borderlessWindow.setBorderless(value == FullscreenMode.BORDERLESS);
                this.getFullscreenMode().setValue(FullscreenMode.of(window));
            }
        );
    }

    @Inject(method = "accept", at = @At("HEAD"))
    private void accept(GameOptions.Visitor visitor, CallbackInfo ci) {
        visitor.accept("fullscreenMode", this.fullscreenMode);
    }

    @Shadow
    public abstract SimpleOption<Boolean> getFullscreen();

    @Override
    public SimpleOption<FullscreenMode> getFullscreenMode() {
        return this.fullscreenMode;
    }
}
