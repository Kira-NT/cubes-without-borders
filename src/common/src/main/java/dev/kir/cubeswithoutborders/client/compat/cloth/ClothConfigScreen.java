package dev.kir.cubeswithoutborders.client.compat.cloth;

import dev.kir.cubeswithoutborders.client.FullscreenType;
import dev.kir.cubeswithoutborders.client.FullscreenTypes;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public final class ClothConfigScreen {
    public static Screen create(CubesWithoutBordersConfig config, String modId, Screen parent) {
        Text title = Text.translatable("modmenu.nameTranslation." + modId);
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(title);

        MinecraftClient client = MinecraftClient.getInstance();
        ConfigCategory category = builder.getOrCreateCategory(title);
        ConfigEntryBuilder entries = builder.entryBuilder();

        // Pause On Lost Focus
        Text pauseOnLostFocusText = Text.translatable("options.pauseOnLostFocus");
        boolean currentPauseOnLostFocus = client.options.pauseOnLostFocus;
        category.addEntry(entries
            .startBooleanToggle(pauseOnLostFocusText, currentPauseOnLostFocus)
            .setDefaultValue(true)
            .setSaveConsumer(x -> client.options.pauseOnLostFocus = x)
            .build());

        // Fullscreen Type
        Text fullscreenTypeText = Text.translatable("options.fullscreenType");
        FullscreenType defaultFullscreenType = FullscreenTypes.exclusive();
        FullscreenType currentFullscreenType = FullscreenTypes.validate(config.getFullscreenType(), defaultFullscreenType);
        List<String> fullscreenTypeSelections = FullscreenTypes.stream().map(FullscreenType::id).collect(Collectors.toList());
        category.addEntry(entries
            .startStringDropdownMenu(fullscreenTypeText, currentFullscreenType.id())
            .requireRestart() // This is a lie
            .setSuggestionMode(false)
            .setDefaultValue(defaultFullscreenType.id())
            .setSelections(fullscreenTypeSelections)
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setFullscreenType))
            .build());

        // Borderless Fullscreen Type
        Text borderlessFullscreenTypeText = Text.translatable("options.borderlessFullscreenType");
        FullscreenType defaultBorderlessFullscreenType = FullscreenTypes.borderless();
        FullscreenType currentBorderlessFullscreenType = FullscreenTypes.validate(config.getBorderlessFullscreenType(), defaultBorderlessFullscreenType);
        category.addEntry(entries
            .startStringDropdownMenu(borderlessFullscreenTypeText, currentBorderlessFullscreenType.id())
            .requireRestart() // This is a lie
            .setSuggestionMode(false)
            .setDefaultValue(defaultBorderlessFullscreenType.id())
            .setSelections(fullscreenTypeSelections)
            .setSaveConsumer(x -> FullscreenTypes.get(x).ifPresent(config::setBorderlessFullscreenType))
            .build());

        return builder.build();
    }

    private ClothConfigScreen() { }
}
