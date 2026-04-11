package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kirant.cwb.CWB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
    @WrapOperation(method = "pauseGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 1), require = 0)
    private void ignorePauseOnLostFocusDuringMultiplayer(Minecraft it, Screen screen, Operation<Void> setScreen) {
        if (it.isWindowActive() || CWB.CONFIG.getPauseOnLostFocusDuringMultiplayer()) {
            setScreen.call(it, screen);
        }
    }
}
