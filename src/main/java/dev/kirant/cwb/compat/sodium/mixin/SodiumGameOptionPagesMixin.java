//? if sodium && sodium: <0.8.0 {
/*package dev.kirant.cwb.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.caffeinemc.mods.sodium.client.gui.*;
import net.caffeinemc.mods.sodium.client.gui.options.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.util.*;

@Mixin(value = SodiumGameOptionPages.class, remap = false)
abstract class SodiumGameOptionPagesMixin {
    @SuppressWarnings("unchecked")
    @WrapOperation(method = "general", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/gui/options/OptionImpl$Builder;build()Lnet/caffeinemc/mods/sodium/client/gui/options/OptionImpl;"), remap = false)
    private static <S, T> OptionImpl<S, T> rebuildFullscreenOptions(OptionImpl.Builder<S, T> builder, Operation<OptionImpl<S, T>> optionFactory) {
        FullscreenManager fullscreenManager = FullscreenManager.getInstance();
        OptionImpl<S, T> option = optionFactory.call(builder);

        //? if sodium: >=0.6.0 {
        if (Components.translatable("options.fullscreen.resolution").equals(option.getName())) {
            return optionFactory.call(builder.setEnabled(() -> true));
        }
        //?}

        if (/^? sodium: >=0.3.0 {^/Components.translatable("options.fullscreen")/^?} else {^//^"Fullscreen"^//^?}^/.equals(option.getName())) {
            return (OptionImpl<S, T>)OptionImpl.createBuilder(FullscreenMode.class, option.getStorage())
                .setName(Components.translatable(CWB.MOD_ID + ".options.fullscreenMode")/^? sodium && sodium: <0.3.0 {^//^.getString()^//^?}^/)
                .setTooltip(Components.translatable(CWB.MOD_ID + ".options.fullscreenMode.tooltip" + (OS.isWindows() ? ".windows" : OS.isMacOS() ? ".macos" : ""))/^? sodium && sodium: <0.3.0 {^//^.getString()^//^?}^/)
                .setImpact(OS.isWindows() ? OptionImpact.HIGH : OS.isMacOS() ? OptionImpact.MEDIUM : OptionImpact.LOW)
                .setControl(o -> new CyclingControl<>(o, FullscreenMode.class, Arrays.stream(FullscreenMode.values()).map(x -> Components.translatable(x.translationKey())/^? sodium: >= 0.3.0 {^/).toArray(Component[]::new)/^?} else {^//^.getString()).toArray(String[]::new)^//^?}^/))
                .setBinding((o, x) -> fullscreenManager.setFullscreenMode(x), o -> fullscreenManager.getFullscreenMode())
                .build();
        }

        return option;
    }
}
*///?}
