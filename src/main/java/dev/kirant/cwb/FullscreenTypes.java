package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class FullscreenTypes {
    private static final FullscreenType[] VALUES;

    static {
        int length = 3;
        VALUES = new FullscreenType[length];
        VALUES[length - 1] = new FullscreenType("minecraft:default");

        FullscreenType windowedFullscreen = new FullscreenType("minecraft:windowed", WindowPlacementStrategies.FILL_MONITOR, WindowHints.DECORATED, WindowHints.AUTO_ICONIFY);
        if (OS.isWindows()) {
            VALUES[1] = new FullscreenType("windows:windowed", WindowPlacementStrategies.OVERSCAN, WindowHints.DECORATED, WindowHints.AUTO_ICONIFY);
            VALUES[0] = windowedFullscreen;
        } else if (OS.isMacOS()) {
            Consumer<Window> enableDockAndMenuBar = w -> MinecraftWindow.MacOS.setHasDockAndMenuBar(true, true);
            Consumer<Window> disableDockAndMenuBar = w -> MinecraftWindow.MacOS.setHasDockAndMenuBar(false, false);
            WindowHint hasDockAndMenuBar = WindowHint.of(enableDockAndMenuBar, disableDockAndMenuBar);
            WindowHint hasShadow = MinecraftWindow.MacOS::setHasShadow;
            WindowHint stub = WindowHint.of(MinecraftWindow.MacOS::registerWindowWillReturnFieldEditorStub);
            VALUES[1] = windowedFullscreen;
            VALUES[0] = new FullscreenType("macos:borderless", WindowPlacementStrategies.FILL_MONITOR, new WindowHint[] { WindowHints.DECORATED, WindowHints.AUTO_ICONIFY, hasShadow, hasDockAndMenuBar, stub }, WindowHints.RESIZABLE);
        } else {
            VALUES[1] = windowedFullscreen;
            VALUES[0] = new FullscreenType("linux:borderless", WindowPlacementStrategies.BIND_TO_MONITOR, WindowHints.AUTO_ICONIFY);
        }
    }

    public static FullscreenType exclusive() {
        return FullscreenTypes.VALUES[FullscreenTypes.VALUES.length - 1];
    }

    public static FullscreenType borderless() {
        return FullscreenTypes.VALUES[0];
    }

    public static FullscreenType[] values() {
        return Arrays.copyOf(FullscreenTypes.VALUES, FullscreenTypes.VALUES.length);
    }

    public static Stream<FullscreenType> stream() {
        return Arrays.stream(FullscreenTypes.VALUES);
    }

    public static Optional<FullscreenType> get(String id) {
        for (FullscreenType value : FullscreenTypes.VALUES) {
            if (value.id().equalsIgnoreCase(id)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }

    private FullscreenTypes() { }
}
