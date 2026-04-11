package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;

public interface FullscreenType {
    String getId();

    boolean isSupported();

    void enable(Window window, Monitor monitor, VideoMode videoMode);

    void disable(Window window);
}
