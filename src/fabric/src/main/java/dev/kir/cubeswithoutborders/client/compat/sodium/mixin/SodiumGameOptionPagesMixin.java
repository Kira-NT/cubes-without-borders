package dev.kir.cubeswithoutborders.client.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.FullscreenManager;
import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.control.CyclingControl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
@Mixin(value = SodiumGameOptionPages.class, remap = false)
abstract class SodiumGameOptionPagesMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "general", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gui/options/OptionImpl$Builder;build()Lme/jellysquid/mods/sodium/client/gui/options/OptionImpl;"), remap = false)
    private static <S, T> OptionImpl<S, T> rebuildFullscreenOption(OptionImpl.Builder<S, T> builder, Operation<OptionImpl<S, T>> optionFactory) {
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        OptionImpl<S, T> option = optionFactory.call(builder);
        if (config.getBorderlessFullscreenType() == config.getFullscreenType()) {
            // If the user changes both regular fullscreen and borderless to use
            // the exact same underlying logic, there's no need to provide access
            // to the "Borderless" option, as it becomes meaningless in this context.
            return option;
        }

        FullscreenManager fullscreenManager = FullscreenManager.getInstance();
        SimpleOption<Boolean> fullscreenOption = MinecraftClient.getInstance().options.getFullscreen();
        String fullscreenName = fullscreenOption.toString();
        String optionName = option.getName().getString();
        if (!fullscreenName.equals(optionName)) {
            return option;
        }

        return (OptionImpl<S, T>)OptionImpl.createBuilder(FullscreenMode.class, option.getStorage())
            .setName(option.getName())
            .setTooltip(option.getTooltip())
            .setControl(opts -> new CyclingControl<>(opts, FullscreenMode.class, Arrays.stream(FullscreenMode.values()).map(x -> Text.translatable(x.getTranslationKey())).toArray(Text[]::new)))
            .setBinding((opts, value) -> fullscreenManager.setFullscreenMode(value), opts -> fullscreenManager.getFullscreenMode())
            .build();
    }
}
