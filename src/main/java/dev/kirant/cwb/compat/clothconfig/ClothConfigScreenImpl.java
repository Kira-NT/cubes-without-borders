//? if cloth-config: >0.0.0 {
package dev.kirant.cwb.compat.clothconfig;

import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import me.shedaniel.clothconfig2.api.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.stream.Collectors;

final class ClothConfigScreenImpl {
    public static Screen create(CWBConfig config, String modId, Screen parent) {
        //? if cloth-config: >=3.0.0 {
        Component title = Components.translatable("modmenu.nameTranslation." + modId);
        Component fullscreenTypeText = Components.translatable(modId + ".options.fullscreenType");
        Component borderlessFullscreenTypeText = Components.translatable(modId + ".options.borderlessFullscreenType");
        Component useScaledFramebufferText = Components.translatable(modId + ".options.useScaledFramebuffer");
        Component pauseOnLostFocusText = Components.translatable(modId + ".options.pauseOnLostFocus");
        Component pauseOnLostFocusDuringMultiplayerText = Components.translatable(modId + ".options.pauseOnLostFocusDuringMultiplayer");
        //?} else {
        /*String title = Components.translatable("modmenu.nameTranslation." + modId).getString();
        String fullscreenTypeText = Components.translatable(modId + ".options.fullscreenType").getString();
        String borderlessFullscreenTypeText = Components.translatable(modId + ".options.borderlessFullscreenType").getString();
        String useScaledFramebufferText = Components.translatable(modId + ".options.useScaledFramebuffer").getString();
        String pauseOnLostFocusText = Components.translatable(modId + ".options.pauseOnLostFocus").getString();
        String pauseOnLostFocusDuringMultiplayerText = Components.translatable(modId + ".options.pauseOnLostFocusDuringMultiplayer").getString();
        *///?}

        Minecraft client = Minecraft.getInstance();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(title);
        ConfigCategory category = builder.getOrCreateCategory(title);
        ConfigEntryBuilder entries = builder.entryBuilder();

        // Fullscreen Type
        FullscreenType defaultFullscreenType = FullscreenTypes.exclusive();
        FullscreenType currentFullscreenType = FullscreenTypes.validate(config.getFullscreenType(), defaultFullscreenType);
        List<String> fullscreenTypeSelections = FullscreenTypes.stream().map(FullscreenType::getId).collect(Collectors.toList());
        category.addEntry(entries
            .startStringDropdownMenu(fullscreenTypeText, currentFullscreenType.getId())
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setFullscreenType))
            .setSelections(fullscreenTypeSelections)
            .setDefaultValue(defaultFullscreenType.getId())
            .requireRestart() // This is a lie
            /*? cloth-config: >=2.0.0 {*/.setSuggestionMode(false)/*?}*/
            .build());

        // Borderless Fullscreen Type
        FullscreenType defaultBorderlessFullscreenType = FullscreenTypes.borderless();
        FullscreenType currentBorderlessFullscreenType = FullscreenTypes.validate(config.getBorderlessFullscreenType(), defaultBorderlessFullscreenType);
        category.addEntry(entries
            .startStringDropdownMenu(borderlessFullscreenTypeText, currentBorderlessFullscreenType.getId())
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setBorderlessFullscreenType))
            .setSelections(fullscreenTypeSelections)
            .setDefaultValue(defaultBorderlessFullscreenType.getId())
            .requireRestart() // This is a lie
            /*? cloth-config: >=2.0.0 {*/.setSuggestionMode(false)/*?}*/
            .build());

        // Use Scaled Framebuffer
        category.addEntry(entries
            .startBooleanToggle(useScaledFramebufferText, config.getUseScaledFramebuffer())
            .setSaveConsumer(config::setUseScaledFramebuffer)
            .setDefaultValue(true)
            .requireRestart()
            .build());

        // Pause on Lost Focus
        category.addEntry(entries
            .startBooleanToggle(pauseOnLostFocusText, client.options.pauseOnLostFocus)
            .setSaveConsumer(x -> client.options.pauseOnLostFocus = x)
            .setDefaultValue(true)
            .build());

        // Pause on Lost Focus (Multiplayer)
        category.addEntry(entries
            .startBooleanToggle(pauseOnLostFocusDuringMultiplayerText, config.getPauseOnLostFocusDuringMultiplayer())
            .setSaveConsumer(config::setPauseOnLostFocusDuringMultiplayer)
            .setDefaultValue(true)
            .build());

        return builder.build();
    }

    private ClothConfigScreenImpl() { }
}
//?}
