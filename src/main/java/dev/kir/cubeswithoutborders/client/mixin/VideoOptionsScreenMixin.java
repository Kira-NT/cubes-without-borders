package dev.kir.cubeswithoutborders.client.mixin;

import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(VideoOptionsScreen.class)
abstract class VideoOptionsScreenMixin extends GameOptionsScreen {
    private VideoOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void applyVideoMode(CallbackInfo ci) {
        // In cases where a user presses ESC to close this screen without
        // clicking on "Done" first, video mode changes won't be applied.
        Window window = this.client == null ? null : this.client.getWindow();
        if (window != null) {
            window.applyVideoMode();
        }
    }

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
