package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.util.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.*;

public final class FullscreenTypes {
    private static final FullscreenType DEFAULT = new DefaultFullscreen();

    private static final Map<String, FullscreenType> REGISTRY = Stream.of(
        new LinuxBorderlessFullscreen(),
        new MacOSBorderlessFullscreen(),
        new WindowedFullscreen(),
        new WindowsWindowedFullscreen(),
        new HybridFullscreen(),
        FullscreenTypes.DEFAULT
    ).collect(Collectors.toMap(FullscreenType::getId, x -> x, (x, _) -> x, LinkedHashMap::new));

    public static FullscreenType validate(FullscreenType fullscreenType) {
        return FullscreenTypes.validate(fullscreenType, FullscreenTypes.DEFAULT);
    }

    public static FullscreenType validate(FullscreenType fullscreenType, FullscreenType defaultFullscreenType) {
        if (fullscreenType == null || !fullscreenType.isSupported()) {
            return FullscreenTypes.validate(defaultFullscreenType, FullscreenTypes.DEFAULT);
        }
        return fullscreenType;
    }

    public static Optional<FullscreenType> get(String id) {
        String normalizedId = id.trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(FullscreenTypes.REGISTRY.get(normalizedId));
    }

    public static Stream<FullscreenType> stream() {
        return REGISTRY.values().stream().filter(FullscreenType::isSupported);
    }

    public static FullscreenType exclusive() {
        return FullscreenTypes.DEFAULT;
    }

    public static FullscreenType borderless() {
        // We also target Java 8, where `orElseThrow` does not exist.
        //noinspection OptionalGetWithoutIsPresent
        return FullscreenTypes.stream().findFirst().get();
    }

    // This fullscreen type has a special meaning that
    // should be recognized by the rest of the codebase:
    //
    // It indicates that the mod should disable itself and
    // allow Minecraft to handle things as it normally does.
    private static class DefaultFullscreen implements FullscreenType {
        @Override
        public String getId() {
            return "minecraft:default";
        }

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // nop
        }

        @Override
        public void disable(Window window) {
            // nop
        }
    }

    private static class WindowedFullscreen implements FullscreenType {
        @Override
        public String getId() {
            return "minecraft:windowed";
        }

        @Override
        public boolean isSupported() {
            return true;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.x();
            window.y = monitor.y();
            window.width = monitor.currentMode().getWidth();
            window.height = monitor.currentMode().getHeight();
            GLFW.glfwSetWindowMonitor(window.handle(), 0, window.x, window.y, window.width, window.height, -1);
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
    }

    private static class HybridFullscreen extends WindowedFullscreen {
        @Override
        public String getId() {
            return "minecraft:hybrid";
        }

        //? if >=26.1 {
        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            GLFW.glfwWindowHint(GLFW.GLFW_SOFT_FULLSCREEN, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);

            window.x = monitor.x();
            window.y = monitor.y();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight();
            int refreshRate = videoMode.getRefreshRate();
            GLFW.glfwSetWindowMonitor(window.handle(), monitor.monitor(), window.x, window.y, window.width, window.height, refreshRate);
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwWindowHint(GLFW.GLFW_SOFT_FULLSCREEN, GLFW.GLFW_FALSE);
        }
        //?}
    }

    private static class LinuxBorderlessFullscreen implements FullscreenType {
        @Override
        public String getId() {
            return "linux:borderless";
        }

        @Override
        public boolean isSupported() {
            return OS.isUnix() && !OS.isMacOS();
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.x();
            window.y = monitor.y();
            window.width = monitor.currentMode().getWidth();
            window.height = monitor.currentMode().getHeight();
            int refreshRate = monitor.currentMode().getRefreshRate();
            GLFW.glfwSetWindowMonitor(window.handle(), monitor.monitor(), window.x, window.y, window.width, window.height, refreshRate);
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
    }

    private static class MacOSBorderlessFullscreen implements FullscreenType {
        @Override
        public String getId() {
            return "macos:borderless";
        }

        @Override
        public boolean isSupported() {
            return OS.isMacOS();
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);
            MinecraftWindow.MacOS.setHasShadow(window, false);
            MinecraftWindow.MacOS.hideGlobalUI();

            window.x = monitor.x();
            window.y = monitor.y();
            window.width = monitor.currentMode().getWidth();
            window.height = monitor.currentMode().getHeight();
            GLFW.glfwSetWindowMonitor(window.handle(), 0, window.x, window.y, window.width, window.height, -1);

            // GLFW ignores the GLFW_RESIZABLE flag for undecorated windows because such windows are
            // always meant to be non-resizable. However, there was a brief bug where it failed
            // to enforce this behavior. In turn, this GLFW bug exposed a peculiar macOS issue:
            // on macOS 10.15 (Catalina) and earlier, if the resizable bit in NSWindowStyleMask
            // is not cleared in fullscreen mode, a window affected by this may end up being
            // automatically minimized when the user clicks anywhere in the content area.
            MinecraftWindow.MacOS.setResizable(window, false);
        }

        @Override
        public void disable(Window window) {
            MinecraftWindow.MacOS.registerWindowWillReturnFieldEditorStub(window);
            MinecraftWindow.MacOS.showGlobalUI();
            MinecraftWindow.MacOS.setHasShadow(window, true);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);

            // macOS 26.3+ (Tahoe) seems to leave a re-decorated window in a weird state
            // where it stops receiving keyboard and mouse inputs.
            // Thankfully, manually re-focusing it via `[window makeKeyAndOrderFront:nil]` helps,
            // which is exactly what `glfwFocusWindow` does under the hood.
            GLFW.glfwFocusWindow(window.handle());
        }
    }

    // Windows 8 introduced a feature called DirectFlip, which detects applications running
    // in windowed fullscreen mode and automatically switches them to exclusive fullscreen.
    // This would be a rather useful optimization... if Microsoft had implemented it properly.
    // Instead, it can cause noticeable flickering whenever an affected window gains or loses
    // focus, due to changes in the compositing mode.
    //
    // Because of this long-standing bug, some users have sought ways to bypass the feature.
    // Since there are no official or documented methods to opt-out of this "nicety",
    // developers around the world have come up with a really nasty solution: extending
    // the window by 1-2 pixels beyond the monitor's boundaries. As a result, the window's
    // dimensions no longer match those of the display, preventing Windows from applying
    // its DirectFlip and forcing the application to remain in windowed mode.
    //
    // However, don't be fooled, the problems are not gonna stop here: Windows has
    // essentially zero respect for windowed applications and may simply choose to
    // discard a significant portion of their frames instead of rendering them.
    //
    // In practice, this leaves you with a rather unpleasant choice: a performant,
    // if somewhat flickery, experience, or a non-flickering one with random and
    // unstable drops in the frame output.
    private static class WindowsWindowedFullscreen implements FullscreenType {
        @Override
        public String getId() {
            return "windows:windowed";
        }

        @Override
        public boolean isSupported() {
            return OS.isWindows();
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.x();
            window.y = monitor.y();
            window.width = monitor.currentMode().getWidth();
            window.height = monitor.currentMode().getHeight() + 1;
            GLFW.glfwSetWindowMonitor(window.handle(), 0, window.x, window.y, window.width, window.height, -1);
            MinecraftWindow.Windows.pleaseStopDiscardingFuckingFramesThankYou(window);
        }

        @Override
        public void disable(Window window) {
            MinecraftWindow.Windows.restoreStyle(window);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.handle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
    }

    private FullscreenTypes() { }
}
