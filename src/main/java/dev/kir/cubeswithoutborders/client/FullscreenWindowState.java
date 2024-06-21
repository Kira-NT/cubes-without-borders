package dev.kir.cubeswithoutborders.client;

import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface FullscreenWindowState {
    FullscreenMode getFullscreenMode();

    void setFullscreenMode(FullscreenMode mode);

    default FullscreenMode getPreferredFullscreenMode() {
        return FullscreenMode.ON;
    }

    default void setPreferredFullscreenMode(FullscreenMode mode) {

    }

    default FullscreenMode toggleFullscreenMode() {
        if (this.getFullscreenMode() != FullscreenMode.OFF) {
            this.setFullscreenMode(FullscreenMode.OFF);
            return this.getFullscreenMode();
        }

        this.setFullscreenMode(this.getPreferredFullscreenMode());
        return this.getFullscreenMode();
    }
}
