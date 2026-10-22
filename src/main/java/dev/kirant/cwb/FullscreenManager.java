package dev.kirant.cwb;

import dev.kirant.cwb.util.MinecraftWindow;

public interface FullscreenManager {
    static FullscreenManager getInstance() {
        return (FullscreenManager)(Object)MinecraftWindow.getInstance();
    }

    FullscreenMode getFullscreenMode();

    void setFullscreenMode(FullscreenMode fullscreenMode);

    default void refreshFullscreenMode() {
        FullscreenMode mode = this.getFullscreenMode();
        if (mode != FullscreenMode.OFF) {
            this.setFullscreenMode(FullscreenMode.OFF);
            this.setFullscreenMode(mode);
        }
    }
}
