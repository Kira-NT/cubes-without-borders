package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;

import java.util.Objects;

public class FullscreenType {
    private final String id;

    private final WindowPlacementStrategy placementStrategy;

    private final WindowHint[] hintsBeforePlacement;

    private final WindowHint[] hintsAfterPlacement;

    public FullscreenType(String id, WindowHint... hints) {
        this(id, WindowPlacementStrategies.BIND_TO_MONITOR, hints);
    }

    public FullscreenType(String id, WindowPlacementStrategy placementStrategy, WindowHint... hints) {
        this(id, placementStrategy, hints, new WindowHint[0]);
    }

    public FullscreenType(String id, WindowPlacementStrategy placementStrategy, WindowHint[] hintsBeforePlacement, WindowHint... hintsAfterPlacement) {
        this.id = Objects.requireNonNull(id);
        this.placementStrategy = Objects.requireNonNull(placementStrategy);
        this.hintsBeforePlacement = Objects.requireNonNull(hintsBeforePlacement);
        this.hintsAfterPlacement = Objects.requireNonNull(hintsAfterPlacement);
    }

    public String id() {
        return this.id;
    }

    public void enable(Window window, Monitor monitor, VideoMode videoMode) {
        for (WindowHint hint : this.hintsBeforePlacement) {
            hint.set(window, false);
        }

        this.placementStrategy.apply(window, monitor, videoMode);

        for (WindowHint hint : this.hintsAfterPlacement) {
            hint.set(window, false);
        }

        WindowHints.RESTORED.set(window, false);
    }

    public void disable(Window window) {
        for (int i = this.hintsBeforePlacement.length - 1; i >= 0; i--) {
            this.hintsBeforePlacement[i].set(window, true);
        }

        for (int i = this.hintsAfterPlacement.length - 1; i >= 0; i--) {
            this.hintsAfterPlacement[i].set(window, true);
        }

        WindowHints.RESTORED.set(window, true);
    }
}
