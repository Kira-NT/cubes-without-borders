package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.kirant.cwb.CWB;
import dev.kirant.cwb.FullscreenManager;
import dev.kirant.cwb.FullscreenMode;
import dev.kirant.cwb.util.Components;
import dev.kirant.cwb.util.MinecraftWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(VideoSettingsScreen.class)
abstract class VideoSettingsScreenMixin {
    //? if <1.21 {
    /*@Inject(method = "removed", at = @At("HEAD"))
    private void applyFullscreenVideoMode(CallbackInfo ci) {
        // In cases where a user presses ESC to close this screen without
        // clicking on "Done" first, video mode changes won't be applied.
        //
        // See:
        //  - https://bugs.mojang.com/browse/MC-175437
        Window window = MinecraftWindow.getInstance();
        if (window != null) {
            window.changeFullscreenVideoMode();
        }
    }
    *///?}

    private static boolean patchFullscreenOption(Object[] options) {
        if (CWB.CONFIG.getBorderlessFullscreenType() == CWB.CONFIG.getFullscreenType()) {
            return false;
        }

        Object booleanFullscreenOption = VideoSettingsScreenMixin.getBooleanFullscreenOption();
        Object enumFullscreenOption = VideoSettingsScreenMixin.createEnumFullscreenOption();
        for (int i = 0; i < options.length; i++) {
            if (options[i] == booleanFullscreenOption) {
                options[i] = enumFullscreenOption;
                return true;
            }
        }
        return false;
    }

    //? if >=1.19 {
    @ModifyReturnValue(method = /*? >=26.1 {*/"displayOptions"/*?} else {*//*"options"*//*?}*/, at = @At("RETURN"))
    private static net.minecraft.client.OptionInstance<?>[] patchDisplayOptions(net.minecraft.client.OptionInstance<?>[] displayOptions) {
        VideoSettingsScreenMixin.patchFullscreenOption(displayOptions);

        //? if >=26.1 {
        List<net.minecraft.client.OptionInstance<?>> displayOptionList = new ArrayList<>(Arrays.asList(displayOptions));
        displayOptionList.remove(Minecraft.getInstance().options.exclusiveFullscreen());
        displayOptions = displayOptionList.toArray(Arrays.copyOf(displayOptions, displayOptionList.size()));
        //?}

        return displayOptions;
    }

    private static Object getBooleanFullscreenOption() {
        return Minecraft.getInstance().options.fullscreen();
    }

    private static Object createEnumFullscreenOption() {
        FullscreenManager window = FullscreenManager.getInstance();
        return new net.minecraft.client.OptionInstance<>(
            "options.fullscreen",
            net.minecraft.client.OptionInstance.noTooltip(),
            (text, value) -> Components.translatable(value.getTranslationKey()),
            new net.minecraft.client.OptionInstance.Enum<>(Arrays.asList(FullscreenMode.values()), com.mojang.serialization.Codec.INT.xmap(FullscreenMode::get, FullscreenMode::getId)),
            window.getFullscreenMode(),
            window::setFullscreenMode
        );
    }
    //?} else {
    /*@WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/Option;)V", ordinal = 0))
    private void patchDisplayOptions(net.minecraft.client.gui.components.OptionsList list, net.minecraft.client.Option[] options, Operation<Void> addSmall) {
        net.minecraft.client.Option[] patchedOptions = Arrays.copyOf(options, options.length);
        addSmall.call(list, VideoSettingsScreenMixin.patchFullscreenOption(patchedOptions) ? patchedOptions : options);
    }

    private static Object getBooleanFullscreenOption() {
        return net.minecraft.client.Option.USE_FULLSCREEN;
    }

    //? if >=1.17 {
    private static Object createEnumFullscreenOption() {
        FullscreenManager window = FullscreenManager.getInstance();
        return net.minecraft.client.CycleOption.create(
            "options.fullscreen",
            Arrays.asList(FullscreenMode.values()),
            value -> Components.translatable(value.getTranslationKey()),
            gameOptions -> window.getFullscreenMode(),
            (gameOptions, option, value) -> window.setFullscreenMode(value)
        );
    }
    //?} else {
    /^private static Object createEnumFullscreenOption() {
        FullscreenManager window = FullscreenManager.getInstance();
        return new net.minecraft.client.CycleOption(
            "options.fullscreen",
            (gameOptions, amount) -> window.setFullscreenMode(FullscreenMode.get(window.getFullscreenMode().getId() + amount)),
            (gameOptions, option) -> {
                FullscreenMode value = window.getFullscreenMode();
                return new net.minecraft.network.chat.TranslatableComponent("options.fullscreen")
                    .append(": ")
                    .append(new net.minecraft.network.chat.TranslatableComponent(value.getTranslationKey()))
                    /^¹? <1.16 {¹^//^¹.getString()¹^//^¹?}¹^/;
            }
        );
    }
    ^///?}
    *///?}
}
