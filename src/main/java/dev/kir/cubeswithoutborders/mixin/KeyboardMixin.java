package dev.kir.cubeswithoutborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.FullscreenWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.Window;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = Keyboard.class, priority = 1000000)
abstract class KeyboardMixin {
    @Shadow
    private @Final MinecraftClient client;
}
