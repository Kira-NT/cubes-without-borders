//? if sodium: >=0.8.0 {
package dev.kirant.cwb.compat.sodium;

import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.caffeinemc.mods.sodium.api.config.*;
import net.caffeinemc.mods.sodium.api.config.option.*;
import net.caffeinemc.mods.sodium.api.config.structure.*;
import net.minecraft.resources.Identifier;

@ConfigEntryPointForge(CWB.MOD_ID)
public final class SodiumConfigBuilder implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        EnumOptionBuilder<FullscreenMode> fullscreenOption = builder
            .createEnumOption(Identifier.fromNamespaceAndPath(CWB.MOD_ID, "general.fullscreen"), FullscreenMode.class)
            .setStorageHandler(CWB.CONFIG::save)
            .setName(Components.translatable(CWB.MOD_ID + ".options.fullscreenMode"))
            .setTooltip(Components.translatable(CWB.MOD_ID + ".options.fullscreenMode.tooltip" + (OS.isWindows() ? ".windows" : OS.isMacOS() ? ".macos" : "")))
            .setImpact(OS.isWindows() ? OptionImpact.HIGH : OS.isMacOS() ? OptionImpact.MEDIUM : OptionImpact.LOW)
            .setDefaultValue(FullscreenMode.OFF)
            .setElementNameProvider(x -> Components.translatable(x.translationKey()))
            .setBinding(FullscreenManager.getInstance()::setFullscreenMode, FullscreenManager.getInstance()::getFullscreenMode);

        ModOptionsBuilder options = builder.registerOwnModOptions();
        SodiumConfigBuilder.registerOptionReplacement(options, "sodium:general.fullscreen", fullscreenOption);
        SodiumConfigBuilder.registerOptionReplacement(options, "sodium:general.fullscreen_mode", fullscreenOption);
        SodiumConfigBuilder.registerOptionOverlay(options, "sodium:general.fullscreen_resolution", builder.createIntegerOption(Identifier.parse("sodium:general.fullscreen_resolution")).setEnabledProvider(x -> true));
        SodiumConfigBuilder.registerOptionOverlay(options, "sodium:general.mac_fullscreen_menu_visibility", builder.createBooleanOption(Identifier.parse("sodium:general.mac_fullscreen_menu_visibility")).setBinding(
            x -> { CWB.CONFIG.setAutoHideDock(x); CWB.CONFIG.setAutoHideMenuBar(x); }, () -> CWB.CONFIG.getAutoHideDock() || CWB.CONFIG.getAutoHideMenuBar())
        );
    }

    private static void registerOptionReplacement(ModOptionsBuilder builder, String id, OptionBuilder option) {
        //? if sodium: >=0.8.13 <0.9.0 || sodium: >=0.9.2 {
        try {
            builder.registerOptionReplacement(Identifier.parse(id), option, Integer.MAX_VALUE);
            return;
        } catch (Throwable t) {
            // Ignore `MethodNotFoundException` if the player has an older version of Sodium installed.
        }
        //?}

        builder.registerOptionReplacement(Identifier.parse(id), option);
    }

    private static void registerOptionOverlay(ModOptionsBuilder builder, String id, OptionBuilder option) {
        //? if sodium: >=0.8.13 <0.9.0 || sodium: >=0.9.2 {
        try {
            builder.registerOptionOverlay(Identifier.parse(id), option, Integer.MAX_VALUE);
            return;
        } catch (Throwable t) {
            // Ignore `MethodNotFoundException` if the player has an older version of Sodium installed.
        }
        //?}

        builder.registerOptionOverlay(Identifier.parse(id), option);
    }
}
//?}
