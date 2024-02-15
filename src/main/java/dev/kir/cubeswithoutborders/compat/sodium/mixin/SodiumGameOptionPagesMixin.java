package dev.kir.cubeswithoutborders.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.minecraft.util.TranslatableOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
@Mixin(value = SodiumGameOptionPages.class, remap = false)
abstract class SodiumGameOptionPagesMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "general", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gui/options/OptionImpl$Builder;build()Lme/jellysquid/mods/sodium/client/gui/options/OptionImpl;"), remap = false)
    private static <S, T> OptionImpl<S, T> rebuildFullscreenOption(OptionImpl.Builder<S, T> builder, Operation<OptionImpl<S, T>> optionFactory) {
        SimpleOption<FullscreenMode> fullscreenMode = ((FullscreenOptions)MinecraftClient.getInstance().options).getFullscreenMode();
        OptionImpl<S, T> option = optionFactory.call(builder);

        String fullscreenName = fullscreenMode.toString();
        String optionName = option.getName().getString();
        if (!fullscreenName.equals(optionName)) {
            return option;
        }

        return (OptionImpl<S, T>)OptionImpl.createBuilder(FullscreenMode.class, option.getStorage())
            .setName(option.getName())
            .setTooltip(option.getTooltip())
            .setControl(opts -> new CyclingControl<>(opts, FullscreenMode.class, Arrays.stream(FullscreenMode.values()).map(TranslatableOption::getText).toArray(Text[]::new)))
            .setBinding((opts, value) -> fullscreenMode.setValue(value), opts -> fullscreenMode.getValue())
            .build();
    }
}
