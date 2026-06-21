package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kirant.cwb.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(/*? >=26.2 {*/net.minecraft.client.gui.Gui/*?} else {*//*Minecraft*//*?}*/.class)
abstract class GuiMixin {
    private static boolean IS_FULLSCREEN_STATE_INITIALIZED = false;

    //? if <26.2 {
    /*@WrapOperation(method = "pauseGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1), require = 0)
    private void ignorePauseOnLostFocusDuringMultiplayer(Minecraft it, Screen screen, Operation<Void> setScreen) {
    *///?} else {
    @WrapOperation(method = "setPauseScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1), require = 0)
    private void ignorePauseOnLostFocusDuringMultiplayer(net.minecraft.client.gui.Gui it, Screen screen, Operation<Void> setScreen) {
    //?}
        if (Minecraft.getInstance().isWindowActive() || CWB.CONFIG.getPauseOnLostFocusDuringMultiplayer()) {
            setScreen.call(it, screen);
        }
    }

    @Inject(method = "setOverlay", at = @At("RETURN"), require = 0)
    private void enableDelayedFullscreen(Overlay overlay, CallbackInfo ci) {
        CWBConfig config = CWB.CONFIG;
        FullscreenManager window = FullscreenManager.getInstance();
        if (overlay == null && !IS_FULLSCREEN_STATE_INITIALIZED && config.getUseDelayedFullscreen() && config.getFullscreenMode() != FullscreenMode.OFF && window.getFullscreenMode() == FullscreenMode.OFF) {
            window.setFullscreenMode(config.getFullscreenMode());
        }
        IS_FULLSCREEN_STATE_INITIALIZED = IS_FULLSCREEN_STATE_INITIALIZED | overlay == null;
    }
}
