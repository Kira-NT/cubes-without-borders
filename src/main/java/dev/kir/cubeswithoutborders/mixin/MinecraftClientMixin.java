package dev.kir.cubeswithoutborders.mixin;

import dev.kir.cubeswithoutborders.client.BorderlessWindowState;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
    @Shadow
    public @Final GameOptions options;

    @Shadow
    private @Final Window window;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/Window;setVsync(Z)V", ordinal = 0))
    private void init(RunArgs args, CallbackInfo ci) {
        BorderlessWindowState settings = (BorderlessWindowState)args.windowSettings;
        BorderlessWindowState window = (BorderlessWindowState)(Object)this.window;
        SimpleOption<FullscreenMode> fullscreenMode = ((FullscreenOptions)this.options).getFullscreenMode();
        boolean shouldBeBorderless = fullscreenMode.getValue() == FullscreenMode.BORDERLESS || settings.isBorderless();

        window.setBorderless(shouldBeBorderless);
        fullscreenMode.setValue(FullscreenMode.of(this.window));
    }
}
