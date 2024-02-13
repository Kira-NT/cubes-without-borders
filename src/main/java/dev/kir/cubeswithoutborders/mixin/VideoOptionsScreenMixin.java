package dev.kir.cubeswithoutborders.mixin;

import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(VideoOptionsScreen.class)
abstract class VideoOptionsScreenMixin {
    @Inject(method = "getOptions", at = @At("RETURN"))
    private static void patchFullscreenOption(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        SimpleOption<?>[] options = cir.getReturnValue();
        SimpleOption<Boolean> booleanFullscreenOption = gameOptions.getFullscreen();
        SimpleOption<FullscreenMode> enumFullscreenOption = ((FullscreenOptions)gameOptions).getFullscreenMode();

        for (int i = 0; i < options.length; i++) {
            if (options[i] == booleanFullscreenOption) {
                options[i] = enumFullscreenOption;
                break;
            }
        }
    }
}
