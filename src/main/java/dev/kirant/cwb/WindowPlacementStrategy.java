package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;

@FunctionalInterface
public interface WindowPlacementStrategy {
    void apply(Window window, Monitor monitor, VideoMode videoMode);
}
