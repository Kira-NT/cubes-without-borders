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

    @WrapOperation(method = "onKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/SimpleOption;setValue(Ljava/lang/Object;)V"))
    private <T> void saveOptions(SimpleOption<T> option, T value, Operation<Void> setValue) {
        // Minecraft doesn't save options when they are changed during a key press.
        // This means that, for example, if you toggle fullscreen mode via F11,
        // the new window state won't be preserved upon a game restart.
        // So, we force Minecraft to save options whenever some new value is set.
        setValue.call(option, value);
        this.client.options.write();
    }
}
