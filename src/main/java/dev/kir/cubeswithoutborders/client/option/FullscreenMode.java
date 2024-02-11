package dev.kir.cubeswithoutborders.client.option;

import dev.kir.cubeswithoutborders.client.BorderlessWindowState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;
import net.minecraft.util.TranslatableOption;

@Environment(EnvType.CLIENT)
public enum FullscreenMode implements TranslatableOption {
    OFF(0, "options.off"),
    ON(1, "options.on"),
    BORDERLESS(2, "options.fullscreen.borderless");

    private final int id;

    private final String name;

    FullscreenMode(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return this.id;
    }

    public String getTranslationKey() {
        return this.name;
    }

    public static FullscreenMode get(int id) {
        id = (id + 3) % 3;

        if (id == ON.id) {
            return ON;
        }

        if (id == BORDERLESS.id) {
            return BORDERLESS;
        }

        return OFF;
    }

    public static FullscreenMode of(Window window) {
        if (window.isFullscreen()) {
            return FullscreenMode.ON;
        }

        if (((BorderlessWindowState)(Object)window).isBorderless()) {
            return FullscreenMode.BORDERLESS;
        }

        return FullscreenMode.OFF;
    }
}
