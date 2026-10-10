//? if cloth-config {
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
        Minecraft client = Minecraft.getInstance();
        ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle(ClothConfigScreenImpl.translate("modmenu.nameTranslation." + modId));
        ConfigCategory category = builder.getOrCreateCategory(builder.getTitle());
        ConfigEntryBuilder entries = builder.entryBuilder();

        // Exclusive Fullscreen Type
        List<String> fullscreenTypeSelections = FullscreenTypes.stream().map(FullscreenType::id).collect(Collectors.toList());
        category.addEntry(entries
            .startStringDropdownMenu(ClothConfigScreenImpl.translate(modId + ".options.exclusiveFullscreenType"), config.getExclusiveFullscreenType().id())
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setExclusiveFullscreenType))
            .setSelections(fullscreenTypeSelections)
            .setDefaultValue(FullscreenTypes.exclusive().id())
            .requireRestart() // This is a lie
            /*? cloth-config: >=2.0.0 {*/.setSuggestionMode(false)/*?}*/
            .build());

        // Borderless Fullscreen Type
        category.addEntry(entries
            .startStringDropdownMenu(ClothConfigScreenImpl.translate(modId + ".options.borderlessFullscreenType"), config.getBorderlessFullscreenType().id())
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setBorderlessFullscreenType))
            .setSelections(fullscreenTypeSelections)
            .setDefaultValue(FullscreenTypes.borderless().id())
            .requireRestart() // This is a lie
            /*? cloth-config: >=2.0.0 {*/.setSuggestionMode(false)/*?}*/
            .build());

        // Windowed Fullscreen Type
        category.addEntry(entries
            .startStringDropdownMenu(ClothConfigScreenImpl.translate(modId + ".options.windowedFullscreenType"), config.getWindowedFullscreenType().id())
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setWindowedFullscreenType))
            .setSelections(fullscreenTypeSelections)
            .setDefaultValue(FullscreenTypes.windowed().id())
            .requireRestart() // This is a lie
            /*? cloth-config: >=2.0.0 {*/.setSuggestionMode(false)/*?}*/
            .build());

        // Use Delayed Fullscreen
        category.addEntry(entries
            .startBooleanToggle(ClothConfigScreenImpl.translate(modId + ".options.useDelayedFullscreen"), config.getUseDelayedFullscreen())
            .setSaveConsumer(config::setUseDelayedFullscreen)
            .setDefaultValue(true)
            .requireRestart()
            .build());

        // Use Scaled Framebuffer
        category.addEntry(entries
            .startBooleanToggle(ClothConfigScreenImpl.translate(modId + ".options.useScaledFramebuffer"), config.getUseScaledFramebuffer())
            .setSaveConsumer(config::setUseScaledFramebuffer)
            .setDefaultValue(true)
            .requireRestart()
            .build());

        // Pause on Lost Focus
        category.addEntry(entries
            .startBooleanToggle(ClothConfigScreenImpl.translate(modId + ".options.pauseOnLostFocus"), client.options.pauseOnLostFocus)
            .setSaveConsumer(x -> client.options.pauseOnLostFocus = x)
            .setDefaultValue(true)
            .build());

        // Pause on Lost Focus (Multiplayer)
        category.addEntry(entries
            .startBooleanToggle(ClothConfigScreenImpl.translate(modId + ".options.pauseOnLostFocusDuringMultiplayer"), config.getPauseOnLostFocusDuringMultiplayer())
            .setSaveConsumer(config::setPauseOnLostFocusDuringMultiplayer)
            .setDefaultValue(true)
            .build());

        return builder.build();
    }

    //? if cloth-config: >=3.0.0 {
    private static Component translate(String key) {
        return Components.translatable(key);
    }
    //?} else {
    /*private static String translate(String key) {
        return Components.translatable(key).getString();
    }
    *///?}

    private ClothConfigScreenImpl() { }
}
//?}
