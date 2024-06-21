package dev.kir.cubeswithoutborders.client.option;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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

    public static FullscreenMode get(boolean isFullscreen, boolean isBorderless) {
        if (isBorderless) {
            return BORDERLESS;
        }

        if (isFullscreen) {
            return ON;
        }

        return OFF;
    }

    public static FullscreenMode combine(FullscreenMode left, FullscreenMode right) {
        boolean isFullscreen = left == ON || right == ON;
        boolean isBorderless = left == BORDERLESS || right == BORDERLESS;
        return FullscreenMode.get(isFullscreen, isBorderless);
    }
}
