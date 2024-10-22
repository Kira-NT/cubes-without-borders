package dev.kir.cubeswithoutborders.client.mixin;

import com.mojang.serialization.Codec;
import dev.kir.cubeswithoutborders.client.FullscreenManager;
import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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

import java.util.Arrays;

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
        //
        // See:
        //  - https://bugs.mojang.com/browse/MC-175437
        Window window = this.client == null ? null : this.client.getWindow();
        if (window != null) {
            window.applyVideoMode();
        }
    }

    @Inject(method = "getOptions", at = @At("RETURN"))
    private static void patchFullscreenOption(GameOptions gameOptions, CallbackInfoReturnable<SimpleOption<?>[]> cir) {
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        if (config.getBorderlessFullscreenType() == config.getFullscreenType()) {
            // If the user changes both regular fullscreen and borderless to use
            // the exact same underlying logic, there's no need to provide access
            // to the "Borderless" option, as it becomes meaningless in this context.
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        FullscreenManager window = (FullscreenManager)(Object)client.getWindow();
        SimpleOption<?>[] options = cir.getReturnValue();
        SimpleOption<Boolean> booleanFullscreenOption = gameOptions.getFullscreen();
        SimpleOption<FullscreenMode> enumFullscreenOption = new SimpleOption<>(
            "options.fullscreen",
            SimpleOption.emptyTooltip(),
            (text, value) -> Text.translatable(value.getTranslationKey()),
            new SimpleOption.PotentialValuesBasedCallbacks<>(Arrays.asList(FullscreenMode.values()), Codec.INT.xmap(FullscreenMode::get, FullscreenMode::getId)),
            window == null ? FullscreenMode.OFF : window.getFullscreenMode(),
            value -> {
                if (window == null || value == window.getFullscreenMode()) {
                    return;
                }

                window.setFullscreenMode(value);
                booleanFullscreenOption.setValue(value != FullscreenMode.OFF);
            }
        );

        for (int i = 0; i < options.length; i++) {
            if (options[i] == booleanFullscreenOption) {
                options[i] = enumFullscreenOption;
                break;
            }
        }
    }
}
