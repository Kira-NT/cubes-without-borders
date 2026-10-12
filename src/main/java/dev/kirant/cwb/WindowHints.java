package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.util.*;

public final class WindowHints {
    private static final WindowHint MACOS_RESIZABLE;

    public static final WindowHint RESIZABLE;

    public static final WindowHint DECORATED;

    public static final WindowHint AUTO_ICONIFY;

    public static final WindowHint RESTORED;

    static {
        MACOS_RESIZABLE = OS.isMacOS() ? MinecraftWindow.MacOS::setResizable : (w, v) -> { };
    }

    //? if >=26.3 {
    static {
        RESIZABLE = MACOS_RESIZABLE.and((w, v) -> org.lwjgl.sdl.SDLVideo.SDL_SetWindowResizable(w.handle(), v));
        DECORATED = (w, v) -> org.lwjgl.sdl.SDLVideo.SDL_SetWindowBordered(w.handle(), v);
        AUTO_ICONIFY = (w, v) -> org.lwjgl.sdl.SDLHints.SDL_SetHint("SDL_VIDEO_MINIMIZE_ON_FOCUS_LOSS", v ? "auto" : "0");
        RESTORED = WindowHint.of(w -> {
            org.lwjgl.sdl.SDLVideo.SDL_RaiseWindow(w.handle());
            org.lwjgl.sdl.SDLVideo.SDL_RestoreWindow(w.handle());
        });
    }
    //?} else {
    /*static {
        RESIZABLE = MACOS_RESIZABLE.and(new GlfwWindowAttrib(org.lwjgl.glfw.GLFW.GLFW_RESIZABLE));
        DECORATED = new GlfwWindowAttrib(org.lwjgl.glfw.GLFW.GLFW_DECORATED);
        AUTO_ICONIFY = new GlfwWindowAttrib(org.lwjgl.glfw.GLFW.GLFW_AUTO_ICONIFY);
        RESTORED = WindowHint.of(w -> {
            org.lwjgl.glfw.GLFW.glfwFocusWindow(w.handle());
            org.lwjgl.glfw.GLFW.glfwRestoreWindow(w.handle());
        });
    }

    private static final class GlfwWindowAttrib implements WindowHint {
        private final int attrib;

        public GlfwWindowAttrib(int attrib) {
            this.attrib = attrib;
        }

        @Override
        public void set(Window window, boolean value) {
            org.lwjgl.glfw.GLFW.glfwSetWindowAttrib(window.handle(), this.attrib, value ? 1 : 0);
        }
    }
    *///?}

    private WindowHints() { }
}
