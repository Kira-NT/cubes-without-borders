package dev.kir.cubeswithoutborders.client.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.FullscreenManager;
import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages;
import net.caffeinemc.mods.sodium.client.gui.options.OptionImpl;
import net.caffeinemc.mods.sodium.client.gui.options.control.CyclingControl;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
@Mixin(value = SodiumGameOptionPages.class, remap = false)
abstract class SodiumGameOptionPagesMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "general", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionImpl$Builder;build()Lnet/caffeinemc/mods/sodium/client/gui/options/OptionImpl;"), remap = false)
    private static <S, T> OptionImpl<S, T> rebuildFullscreenOptions(OptionImpl.Builder<S, T> builder, Operation<OptionImpl<S, T>> optionFactory) {
        FullscreenManager fullscreenManager = FullscreenManager.getInstance();
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        OptionImpl<S, T> option = optionFactory.call(builder);
        Text optionName = option.getName();

        Text fullscreenResolutionName = Text.translatable("options.fullscreen.resolution");
        if (fullscreenResolutionName.equals(optionName)) {
            // We provide a custom scaling solution for our fullscreen modes
            // that works on any OS. Thus, always keep the option enabled.
            return optionFactory.call(builder.setEnabled(() -> true));
        }

        Text fullscreenName = Text.translatable("options.fullscreen");
        if (fullscreenName.equals(optionName) && config.getBorderlessFullscreenType() != config.getFullscreenType()) {
            // If the user changes both regular fullscreen and borderless
            // to use the same underlying logic, there is absolutely no
            // need to provide access to the "Borderless" option, as it
            // becomes meaningless in this context.
            return (OptionImpl<S, T>)OptionImpl.createBuilder(FullscreenMode.class, option.getStorage())
                .setName(option.getName())
                .setTooltip(option.getTooltip())
                .setControl(opts -> new CyclingControl<>(opts, FullscreenMode.class, Arrays.stream(FullscreenMode.values()).map(x -> Text.translatable(x.getTranslationKey())).toArray(Text[]::new)))
                .setBinding((__, x) -> fullscreenManager.setFullscreenMode(x), __ -> fullscreenManager.getFullscreenMode())
                .build();
        }

        return option;
    }
}
