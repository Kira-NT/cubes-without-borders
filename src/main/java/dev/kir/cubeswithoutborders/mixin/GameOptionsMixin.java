package dev.kir.cubeswithoutborders.mixin;

import com.mojang.serialization.Codec;
import dev.kir.cubeswithoutborders.client.FullscreenWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.SimpleOptionCallbacks;
import dev.kir.cubeswithoutborders.client.util.MonitorInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
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
    private SimpleOption<MonitorInfo> fullscreenMonitor;

    private SimpleOption<FullscreenMode> fullscreenMode;

    private SimpleOption<FullscreenMode> preferredFullscreenMode;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;load()V", shift = At.Shift.BEFORE))
    private void init(MinecraftClient client, File optionsFile, CallbackInfo ci) {
        this.fullscreenMonitor = new SimpleOption<>(
            "options.fullscreenMonitor",
            SimpleOption.emptyTooltip(),
            (option, value) -> Text.literal(value.toString()),
            new SimpleOptionCallbacks<>(
                Codec.STRING.xmap(x -> MonitorInfo.parse(x).orElse(MonitorInfo.primary()), MonitorInfo::toString)
            ),
            MonitorInfo.primary(),
            value -> { }
        );

        this.fullscreenMode = new SimpleOption<>(
            "options.fullscreen",
            SimpleOption.emptyTooltip(),
            SimpleOption.enumValueText(),
            new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(FullscreenMode.values()), Codec.INT.xmap(FullscreenMode::get, FullscreenMode::getId)),
            FullscreenMode.OFF,
            value -> {
                Window window = MinecraftClient.getInstance().getWindow();
                FullscreenWindowState borderlessWindow = (FullscreenWindowState)(Object)window;
                if (window == null || value == borderlessWindow.getFullscreenMode()) {
                    return;
                }

                borderlessWindow.setFullscreenMode(value);
                FullscreenMode fullscreenMode = borderlessWindow.getFullscreenMode();
                FullscreenMode preferredFullscreenMode = borderlessWindow.getPreferredFullscreenMode();

                this.getFullscreenMode().setValue(fullscreenMode);
                this.getPreferredFullscreenMode().setValue(preferredFullscreenMode);
                this.getFullscreen().setValue(fullscreenMode == FullscreenMode.ON);
            }
        );

        this.preferredFullscreenMode = new SimpleOption<>(
            "options.preferredFullscreenMode",
            SimpleOption.emptyTooltip(),
            SimpleOption.enumValueText(),
            new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(FullscreenMode.values()), Codec.INT.xmap(FullscreenMode::get, FullscreenMode::getId)),
            FullscreenMode.ON,
            value -> {
                Window window = MinecraftClient.getInstance().getWindow();
                FullscreenWindowState borderlessWindow = (FullscreenWindowState)(Object)window;
                if (window == null || value == borderlessWindow.getPreferredFullscreenMode()) {
                    return;
                }

                borderlessWindow.setPreferredFullscreenMode(value);
                FullscreenMode preferredFullscreenMode = borderlessWindow.getPreferredFullscreenMode();

                this.getPreferredFullscreenMode().setValue(preferredFullscreenMode);
            }
        );
    }

    @Inject(method = "accept", at = @At("HEAD"))
    private void accept(GameOptions.Visitor visitor, CallbackInfo ci) {
        visitor.accept("fullscreenMonitor", this.fullscreenMonitor);
        visitor.accept("fullscreenMode", this.fullscreenMode);
        visitor.accept("preferredFullscreenMode", this.preferredFullscreenMode);
    }

    @Shadow
    public abstract SimpleOption<Boolean> getFullscreen();

    @Override
    public SimpleOption<MonitorInfo> getFullscreenMonitor() {
        return this.fullscreenMonitor;
    }

    @Override
    public SimpleOption<FullscreenMode> getFullscreenMode() {
        return this.fullscreenMode;
    }

    @Override
    public SimpleOption<FullscreenMode> getPreferredFullscreenMode() {
        return this.preferredFullscreenMode;
    }
}
