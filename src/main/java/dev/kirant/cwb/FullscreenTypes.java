package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.util.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class FullscreenTypes {
    private static final FullscreenType[] VALUES;

    static {
        int length = 4 + (OS.isMacOS() ? 1 : 0);
        VALUES = new FullscreenType[length];
        VALUES[length - 1] = new FullscreenType("minecraft:default");

        FullscreenType windowedFullscreen = new FullscreenType("minecraft:windowed", WindowPlacementStrategies.FILL_MONITOR, WindowHints.DECORATED, WindowHints.AUTO_ICONIFY);
        FullscreenType maximizedFullscreen = new FullscreenType("minecraft:maximized", WindowPlacementStrategies.MAXIMIZE, WindowHints.DECORATED, WindowHints.AUTO_ICONIFY);
        if (OS.isWindows()) {
            VALUES[2] = maximizedFullscreen;
            VALUES[1] = new FullscreenType("windows:windowed", WindowPlacementStrategies.OVERSCAN, WindowHints.DECORATED, WindowHints.AUTO_ICONIFY);
            VALUES[0] = windowedFullscreen;
        } else if (OS.isMacOS()) {
            Consumer<Window> enableDockAndMenuBar = w -> MinecraftWindow.MacOS.setHasDockAndMenuBar(true, true);
            Consumer<Window> disableDockAndMenuBar = w -> MinecraftWindow.MacOS.setHasDockAndMenuBar(false, false);
            Consumer<Window> disableDock = w -> MinecraftWindow.MacOS.setHasDockAndMenuBar(false, true);
            WindowHint hasDockAndMenuBar = WindowHint.of(enableDockAndMenuBar, disableDockAndMenuBar);
            WindowHint hasDock = WindowHint.of(enableDockAndMenuBar, disableDock);
            WindowHint hasShadow = MinecraftWindow.MacOS::setHasShadow;
            WindowHint stub = WindowHint.of(MinecraftWindow.MacOS::registerWindowWillReturnFieldEditorStub);
            VALUES[3] = windowedFullscreen;
            VALUES[2] = maximizedFullscreen;
            VALUES[1] = new FullscreenType("macos:windowed", WindowPlacementStrategies.FILL_MONITOR_SAFE_AREA, new WindowHint[] { WindowHints.DECORATED, WindowHints.AUTO_ICONIFY, hasShadow, hasDock, stub }, WindowHints.RESIZABLE);
            VALUES[0] = new FullscreenType("macos:borderless", WindowPlacementStrategies.FILL_MONITOR_SAFE_AREA, new WindowHint[] { WindowHints.DECORATED, WindowHints.AUTO_ICONIFY, hasShadow, hasDockAndMenuBar, stub }, WindowHints.RESIZABLE);
        } else {
            VALUES[2] = windowedFullscreen;
            VALUES[1] = maximizedFullscreen;
            VALUES[0] = new FullscreenType("linux:borderless", WindowPlacementStrategies.BIND_TO_MONITOR, WindowHints.AUTO_ICONIFY);
        }
    }

    public static FullscreenType exclusive() {
        return FullscreenTypes.VALUES[FullscreenTypes.VALUES.length - 1];
    }

    public static FullscreenType borderless() {
        return FullscreenTypes.VALUES[0];
    }

    public static FullscreenType windowed() {
        return FullscreenTypes.VALUES[1];
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
        if ("@default".equalsIgnoreCase(id) || "@exclusive".equalsIgnoreCase(id)) {
            return Optional.of(FullscreenTypes.exclusive());
        }
        if ("@borderless".equalsIgnoreCase(id)) {
            return Optional.of(FullscreenTypes.borderless());
        }
        if ("@windowed".equalsIgnoreCase(id)) {
            return Optional.of(FullscreenTypes.windowed());
        }
        return Optional.empty();
    }

    private FullscreenTypes() { }
}
