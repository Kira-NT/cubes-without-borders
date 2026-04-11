package dev.kirant.cwb;

import dev.kirant.cwb.util.MinecraftWindow;

public interface FullscreenManager {
    static FullscreenManager getInstance() {
        return (FullscreenManager)(Object)MinecraftWindow.getInstance();
    }

    FullscreenMode getFullscreenMode();

    void setFullscreenMode(FullscreenMode fullscreenMode);
}
