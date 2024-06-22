package dev.kir.cubeswithoutborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.FullscreenWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import dev.kir.cubeswithoutborders.client.util.MonitorInfo;
import dev.kir.cubeswithoutborders.client.util.MonitorInfoContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.WindowProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    @Shadow
    public @Final GameOptions options;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/WindowProvider;createWindow(Lnet/minecraft/client/WindowSettings;Ljava/lang/String;Ljava/lang/String;)Lnet/minecraft/client/util/Window;", ordinal = 0))
    private Window createWindow(WindowProvider windowProvider, WindowSettings windowSettings, String videoMode, String title, Operation<Window> createWindow) {
        FullscreenOptions options = (FullscreenOptions)this.options;
        FullscreenWindowState settings = (FullscreenWindowState)windowSettings;

        MonitorInfo fullscreenMonitor = options.getFullscreenMonitor().getValue();
        FullscreenMode fullscreenMode = FullscreenMode.combine(
            settings.getFullscreenMode(),
            options.getFullscreenMode().getValue()
        );
        FullscreenMode preferredFullscreenMode = options.getPreferredFullscreenMode().getValue();

        settings.setPreferredFullscreenMode(preferredFullscreenMode);
        settings.setFullscreenMode(fullscreenMode);
        ((MonitorInfoContainer)settings).setMonitorInfo(fullscreenMonitor);

        Window window = createWindow.call(windowProvider, windowSettings, videoMode, title);
        FullscreenWindowState fullscreenWindow = (FullscreenWindowState)(Object)window;

        fullscreenWindow.setPreferredFullscreenMode(preferredFullscreenMode);
        fullscreenWindow.setFullscreenMode(fullscreenMode);

        options.getFullscreenMode().setValue(fullscreenWindow.getFullscreenMode());
        options.getPreferredFullscreenMode().setValue(fullscreenWindow.getPreferredFullscreenMode());

        return window;
    }
}
