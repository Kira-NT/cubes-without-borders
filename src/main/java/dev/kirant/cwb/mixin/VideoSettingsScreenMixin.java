package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.screens.options.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

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
        FullscreenType exclusiveFullscreenType = CWB.CONFIG.getExclusiveFullscreenType();
        FullscreenType borderlessFullscreenType = CWB.CONFIG.getBorderlessFullscreenType();
        FullscreenType windowedFullscreenType = CWB.CONFIG.getWindowedFullscreenType();
        if (exclusiveFullscreenType == borderlessFullscreenType && borderlessFullscreenType == windowedFullscreenType) {
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
    @ModifyReturnValue(method = "displayOptions", at = @At("RETURN"))
    private static net.minecraft.client.OptionInstance<?>[] patchDisplayOptions(net.minecraft.client.OptionInstance<?>[] displayOptions) {
        VideoSettingsScreenMixin.patchFullscreenOption(displayOptions);

        //? if >=26.1
        List<net.minecraft.client.OptionInstance<?>> displayOptionList = new ArrayList<>(Arrays.asList(displayOptions));
        //? if >=26.1
        displayOptionList.remove(Minecraft.getInstance().options.exclusiveFullscreen());
        //? if >=26.3
        displayOptionList.remove(Minecraft.getInstance().options.macFullscreenMenuVisibility());
        //? if >=26.1
        displayOptions = displayOptionList.toArray(Arrays.copyOf(displayOptions, displayOptionList.size()));

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
            (c, x) -> Components.translatable(x.translationKey()),
            new net.minecraft.client.OptionInstance.Enum<>(Arrays.asList(FullscreenMode.values()), com.mojang.serialization.Codec.INT.xmap(FullscreenMode::valueOf, FullscreenMode::ordinal)),
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
            value -> Components.translatable(value.translationKey()),
            gameOptions -> window.getFullscreenMode(),
            (gameOptions, option, value) -> window.setFullscreenMode(value)
        );
    }
    //?} else {
    /^private static Object createEnumFullscreenOption() {
        FullscreenManager window = FullscreenManager.getInstance();
        return new net.minecraft.client.CycleOption(
            "options.fullscreen",
            (gameOptions, amount) -> window.setFullscreenMode(FullscreenMode.valueOf(window.getFullscreenMode().ordinal() + amount)),
            (gameOptions, option) -> {
                FullscreenMode value = window.getFullscreenMode();
                return new net.minecraft.network.chat.TranslatableComponent("options.fullscreen")
                    .append(": ")
                    .append(new net.minecraft.network.chat.TranslatableComponent(value.translationKey()))
                    /^? <1.16 {^//^.getString()^//^?}^/;
            }
        );
    }
    ^///?}
    *///?}
}
