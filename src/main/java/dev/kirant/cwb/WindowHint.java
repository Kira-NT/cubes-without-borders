package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;

import java.util.function.Consumer;

@FunctionalInterface
public interface WindowHint {
    void set(Window window, boolean value);

    default WindowHint and(WindowHint other) {
        return (w, v) -> { this.set(w, v); other.set(w, v); };
    }

    static WindowHint of(Consumer<Window> enable, Consumer<Window> disable) {
        return (window, value) -> (value ? enable : disable).accept(window);
    }

    static WindowHint of(Consumer<Window> enable) {
        return WindowHint.of(enable, _ -> { });
    }
}
