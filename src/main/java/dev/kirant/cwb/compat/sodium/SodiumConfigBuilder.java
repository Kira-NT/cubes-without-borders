//? if sodium: >=0.8.0 {
package dev.kirant.cwb.compat.sodium;

import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.caffeinemc.mods.sodium.api.config.*;
import net.caffeinemc.mods.sodium.api.config.option.*;
import net.caffeinemc.mods.sodium.api.config.structure.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@ConfigEntryPointForge(CWB.MOD_ID)
public final class SodiumConfigBuilder implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        String fullscreenTooltipKey = "sodium.options.fullscreen_mode.tooltip";
        Component fullscreenTooltip = Components.translatable(fullscreenTooltipKey);
        if (fullscreenTooltipKey.equals(fullscreenTooltip.getString())) {
            fullscreenTooltip = Components.translatable("sodium.options.fullscreen.tooltip");
        }

        EnumOptionBuilder<FullscreenMode> fullscreenOption = builder
            .createEnumOption(Identifier.fromNamespaceAndPath(CWB.MOD_ID, "general.fullscreen"), FullscreenMode.class)
            .setStorageHandler(CWB.CONFIG::save)
            .setName(Components.translatable("options.fullscreen"))
            .setTooltip(fullscreenTooltip)
            .setImpact(OptionImpact.HIGH)
            .setDefaultValue(FullscreenMode.OFF)
            .setElementNameProvider(x -> Components.translatable(x.getTranslationKey()))
            .setBinding(FullscreenManager.getInstance()::setFullscreenMode, FullscreenManager.getInstance()::getFullscreenMode);

        builder.registerOwnModOptions()
            .registerOptionReplacement(Identifier.parse("sodium:general.fullscreen"), fullscreenOption)
            .registerOptionReplacement(Identifier.parse("sodium:general.fullscreen_mode"), fullscreenOption)
            .registerOptionOverlay(Identifier.parse("sodium:general.fullscreen_resolution"), builder.createIntegerOption(Identifier.parse("sodium:general.fullscreen_resolution")).setEnabledProvider(x -> true));
    }
}
//?}
