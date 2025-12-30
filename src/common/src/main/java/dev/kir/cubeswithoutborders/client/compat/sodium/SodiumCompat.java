package dev.kir.cubeswithoutborders.client.compat.sodium;

import dev.kir.cubeswithoutborders.client.FullscreenManager;
import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.client.gui.FullscreenResolutionRange;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.Set;

@ConfigEntryPointForge("cubes_without_borders")
public class SodiumCompat implements ConfigEntryPoint {
    private static final Identifier SODIUM_FULLSCREEN_ID = Identifier.of("sodium", "general.fullscreen");
    private static final Identifier SODIUM_FULLSCREEN_RESOLUTION_ID = Identifier.of("sodium", "general.fullscreen_resolution");

    private static final Identifier CWB_FULLSCREEN_ID = Identifier.of("cubes-without-borders", "general.fullscreen");

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        FullscreenManager fullscreenManager = FullscreenManager.getInstance();
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();

        builder.registerOwnModOptions()
            .registerOptionOverlay(SODIUM_FULLSCREEN_RESOLUTION_ID,
                builder.createIntegerOption(SODIUM_FULLSCREEN_RESOLUTION_ID)
                    .setStorageHandler(() -> MinecraftClient.getInstance().options.write())
                    .setName(Text.translatable("options.fullscreen.resolution"))
                    .setTooltip(Text.translatable("sodium.options.fullscreen_resolution.tooltip"))
                    .setValueFormatter(value -> {
                        Monitor monitor = MinecraftClient.getInstance().getWindow().getMonitor();
                        if (monitor == null || value == 0) {
                            return Text.translatable("options.fullscreen.current");
                        }
                        return Text.literal(monitor.getVideoMode(Math.min(value - 1, monitor.getVideoModeCount() - 1)).toString().replace(" (24bit)", ""));
                    })
                    .setValidator(new FullscreenResolutionRange())
                    .setDefaultValue(0)
                    .setBinding(value -> {
                        Window window = MinecraftClient.getInstance().getWindow();
                        if (window == null) {
                            return;
                        }

                        Monitor monitor = window.getMonitor();
                        if (monitor == null) {
                            return;
                        }

                        window.setFullscreenVideoMode(value == 0 ? Optional.empty() : Optional.of(monitor.getVideoMode(value - 1)));
                        window.applyFullscreenVideoMode();
                    }, () -> {
                        Window window = MinecraftClient.getInstance().getWindow();
                        if (window == null) {
                            return 0;
                        }

                        Monitor monitor = window.getMonitor();
                        if (monitor == null) {
                            return 0;
                        }

                        Optional<VideoMode> optional = window.getFullscreenVideoMode();
                        return optional.map(v -> monitor.findClosestVideoModeIndex(v) + 1).orElse(0);
                    })
                    .setEnabledProvider(state -> {
                        // We provide a custom scaling solution for our fullscreen modes
                        // that works on any OS. Thus, always keep the option enabled.
                        Window window = MinecraftClient.getInstance().getWindow();
                        if (window == null) {
                            return false;
                        }

                        Monitor monitor = window.getMonitor();
                        return monitor != null && monitor.getVideoModeCount() > 0;
                    }, CWB_FULLSCREEN_ID)
            )
            .registerOptionReplacement(SODIUM_FULLSCREEN_ID,
                builder.createEnumOption(CWB_FULLSCREEN_ID, FullscreenMode.class)
                    .setStorageHandler(config::save)
                    .setName(Text.translatable("options.fullscreen"))
                    .setTooltip(Text.translatable("sodium.options.fullscreen.tooltip"))
                    .setDefaultValue(FullscreenMode.OFF)
                    .setBinding(fullscreenManager::setFullscreenMode, fullscreenManager::getFullscreenMode)
                    .setAllowedValuesProvider(__ -> {
                        // If the user changes both regular fullscreen and borderless
                        // to use the same underlying logic, there is absolutely no
                        // need to provide access to the "Borderless" option, as it
                        // becomes meaningless in this context.
                        if (config.getBorderlessFullscreenType() == config.getFullscreenType()) {
                            return Set.of(FullscreenMode.OFF, FullscreenMode.ON);
                        } else {
                            return Set.of(FullscreenMode.OFF, FullscreenMode.ON, FullscreenMode.BORDERLESS);
                        }
                    })
                    .setElementNameProvider(mode -> Text.translatable(mode.getTranslationKey()))
            );
    }
}
