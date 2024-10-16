package dev.kir.cubeswithoutborders.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum FullscreenMode {
    OFF(0, "options.off"),
    ON(1, "options.on"),
    BORDERLESS(2, "options.borderlessFullscreen");

    private final int id;

    private final String translationKey;

    FullscreenMode(int id, String translationKey) {
        this.id = id;
        this.translationKey = translationKey;
    }

    public int getId() {
        return this.id;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public static FullscreenMode get(int id) {
        id = ((id % 3) + 3) % 3;

        if (id == ON.id) {
            return ON;
        }

        if (id == BORDERLESS.id) {
            return BORDERLESS;
        }

        return OFF;
    }
}
