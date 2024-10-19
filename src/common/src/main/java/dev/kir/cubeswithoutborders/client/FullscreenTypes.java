package dev.kir.cubeswithoutborders.client;

import dev.kir.cubeswithoutborders.client.util.MacOSUtil;
import dev.kir.cubeswithoutborders.client.util.SystemUtil;
import dev.kir.cubeswithoutborders.client.util.WindowsUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.windows.User32;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public final class FullscreenTypes {
    private static final Map<String, FullscreenType> REGISTRY = new HashMap<>();

    public static final FullscreenType DEFAULT = FullscreenTypes.register(new DefaultFullscreen());

    public static final FullscreenType WINDOWED = FullscreenTypes.register(new WindowedFullscreen());

    public static final FullscreenType LINUX_BORDERLESS = FullscreenTypes.register(new LinuxBorderlessFullscreen());

    public static final FullscreenType MAC_OS_BORDERLESS = FullscreenTypes.register(new MacOSBorderlessFullscreen());

    public static final FullscreenType WINDOWS_EXCLUSIVE = FullscreenTypes.register(new WindowsExclusiveFullscreen());

    public static final FullscreenType WINDOWS_BORDERLESS = FullscreenTypes.register(new WindowsBorderlessFullscreen());

    public static final FullscreenType WINDOWS_WINDOWED = FullscreenTypes.register(new WindowsWindowedFullscreen());


    public static FullscreenType validate(FullscreenType fullscreenType) {
        return FullscreenTypes.validate(fullscreenType, FullscreenTypes.DEFAULT);
    }

    public static FullscreenType validate(FullscreenType fullscreenType, FullscreenType defaultFullscreenType) {
        if (fullscreenType == null || !fullscreenType.supported()) {
            return FullscreenTypes.validate(defaultFullscreenType, FullscreenTypes.DEFAULT);
        }
        return fullscreenType;
    }

    public static Optional<FullscreenType> get(String id) {
        String normalizedId = id.trim().toLowerCase(Locale.ROOT);
        return Optional.ofNullable(REGISTRY.get(normalizedId));
    }

    public static Stream<FullscreenType> stream() {
        return REGISTRY.values()
            .stream()
            .filter(FullscreenType::supported)
            .sorted((a, b) -> b.priority() - a.priority());
    }

    public static FullscreenType exclusive() {
        return FullscreenTypes.DEFAULT;
    }

    public static FullscreenType borderless() {
        return FullscreenTypes.stream().findFirst().orElseThrow();
    }

    private static FullscreenType register(FullscreenType fullscreenType) {
        String id = fullscreenType.id().toLowerCase(Locale.ROOT);
        REGISTRY.put(id, fullscreenType);
        return fullscreenType;
    }


    // This fullscreen type has a special meaning that
    // should be recognized by the rest of the codebase:
    //
    // It indicates that the mod should disable itself and
    // allow Minecraft to handle things as it normally does.
    private static class DefaultFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "minecraft:default";
        }

        @Override
        public boolean supported() {
            return true;
        }

        @Override
        public int priority() {
            // Since this mod is centered around the concept of borderless fullscreen,
            // the original Minecraft fullscreen type should, by default,
            // have a lower priority than the implementations we provide.
            return -1;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // noop
        }

        @Override
        public void disable(Window window) {
            // noop
        }
    }


    // This is a naive implementation of windowed fullscreen mode.
    //
    // It's **not** equipped to combat various quirks of different platforms,
    // so it likely won't work as expected in most cases.
    // However, since there **are** scenarios where it might still be useful,
    // I'm including it as an option for those who understand its limitations.
    private static class WindowedFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "minecraft:windowed";
        }

        @Override
        public boolean supported() {
            return true;
        }

        @Override
        public int priority() {
            // This fullscreen type is prone to various issues.
            // Thus, it should not be considered a viable option.
            return -2;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // We cannot change the video mode without attaching the window
            // to a monitor. Thus, enforce the use of the current mode instead.
            videoMode = monitor.getCurrentVideoMode();

            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.getViewportX();
            window.y = monitor.getViewportY();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight();

            GLFW.glfwSetWindowMonitor(
                window.getHandle(),
                0,
                window.x,
                window.y,
                window.width,
                window.height,
                -1
            );
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
    }


    // Provides a borderless fullscreen experience for Linux.
    //
    // Since Linux isn't a steaming pile of garbage like some... other platform,
    // and regular fullscreen is already about as good as it gets, the only thing
    // we really need to do is set `GLFW_AUTO_ICONIFY` to `GLFW_FALSE`.
    private static class LinuxBorderlessFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "linux:borderless";
        }

        @Override
        public boolean supported() {
            return SystemUtil.isLinux();
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // GLFW does not support changing the video mode on Wayland.
            // Since X11 is irrelevant in the modern world, let's assume
            // we're always on Wayland. Therefore, we need to preserve
            // the current mode to prevent the game from messing up
            // its own resolution.
            videoMode = monitor.getCurrentVideoMode();

            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.getViewportX();
            window.y = monitor.getViewportY();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight();

            GLFW.glfwSetWindowMonitor(
                window.getHandle(),
                monitor.getHandle(),
                window.x,
                window.y,
                window.width,
                window.height,
                videoMode.getRefreshRate()
            );
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
    }


    // Provides a borderless fullscreen experience for macOS.
    //
    // At least I think so. I haven't tested it since I don't have
    // macOS installed anymore. If you do, feel free to tweak it
    // to your liking and submit a PR - contributions are always welcome.
    private static class MacOSBorderlessFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "macos:borderless";
        }

        @Override
        public boolean supported() {
            return SystemUtil.isMacOS();
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // We cannot change the video mode without attaching the window
            // to a monitor. Thus, enforce the use of the current mode instead.
            videoMode = monitor.getCurrentVideoMode();

            MacOSUtil.hideGlobalUI();
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.getViewportX();
            window.y = monitor.getViewportY();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight();

            GLFW.glfwSetWindowMonitor(
                window.getHandle(),
                0,
                window.x,
                window.y,
                window.width,
                window.height,
                -1
            );
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
            MacOSUtil.showGlobalUI();
        }
    }


    // This is the same fullscreen implementation used by mods like CW/B,
    // Borderless Mining, and others. It causes the screen to flicker
    // whenever the window loses or regains focus, and it has several
    // issues when it comes to compositing.
    //
    // Technically, it's still exclusive fullscreen but with dynamic
    // context switching enabled, which is what causes the notorious
    // flickering.
    //
    // This implementation is only included because some users complain
    // about increased input lag with other fullscreen types. *Sigh*
    //
    // See:
    //  - https://github.com/Kir-Antipov/cubes-without-borders/issues/4
    //  - https://github.com/Kir-Antipov/cubes-without-borders/issues/12
    private static class WindowsExclusiveFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "windows:exclusive";
        }

        @Override
        public boolean supported() {
            return SystemUtil.isWindows();
        }

        @Override
        public int priority() {
            // The priority table for Windows fullscreen types is as follows:
            //  - Windowed    1000
            //  - Borderless  100
            //  - Exclusive   10
            //  - Default    -1
            return 10;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // We cannot change the video mode without attaching the window
            // to a monitor. Thus, enforce the use of the current mode instead.
            videoMode = monitor.getCurrentVideoMode();

            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);

            window.x = monitor.getViewportX();
            window.y = monitor.getViewportY();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight();

            GLFW.glfwSetWindowMonitor(
                window.getHandle(),
                0,
                window.x,
                window.y,
                window.width,
                window.height,
                -1
            );
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
        }
    }


    // This one offers a true borderless fullscreen experience.
    // Or at least it would if Windows wasn't so broken and actually knew how
    // to render its own windows (no pun intended) correctly.
    //
    // While this implementation avoids the infamous flickering and fully
    // supports compositing, it has several other shortcomings that lower
    // its priority in favor of windowed fullscreen.
    //
    // See:
    //  - https://github.com/Kir-Antipov/cubes-without-borders/issues/22
    //  - https://github.com/Kir-Antipov/cubes-without-borders/issues/27
    private static class WindowsBorderlessFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "windows:borderless";
        }

        @Override
        public boolean supported() {
            return SystemUtil.isWindows();
        }

        @Override
        public int priority() {
            // The priority table for Windows fullscreen types is as follows:
            //  - Windowed    1000
            //  - Borderless  100
            //  - Exclusive   10
            //  - Default    -1
            return 100;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            long hWndInsertAfter = User32.HWND_NOTOPMOST;
            long style = User32.WS_VISIBLE | User32.WS_CLIPCHILDREN | User32.WS_CLIPSIBLINGS | User32.WS_GROUP;
            long exStyle = User32.WS_EX_APPWINDOW | User32.WS_EX_ACCEPTFILES | User32.WS_EX_COMPOSITED | User32.WS_EX_LAYERED;

            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.getViewportX();
            window.y = monitor.getViewportY();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight();

            GLFW.glfwSetWindowMonitor(
                window.getHandle(),
                monitor.getHandle(),
                window.x,
                window.y,
                window.width,
                window.height,
                videoMode.getRefreshRate()
            );
            WindowsUtil.setWindowStyle(window, hWndInsertAfter, style, exStyle);
        }

        @Override
        public void disable(Window window) {
            long hWndInsertAfter = User32.HWND_TOP;
            long style = User32.WS_POPUP | User32.WS_VISIBLE | User32.WS_CLIPCHILDREN | User32.WS_CLIPSIBLINGS | User32.WS_SYSMENU | User32.WS_GROUP;
            long exStyle = User32.WS_EX_APPWINDOW | User32.WS_EX_ACCEPTFILES;

            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
            WindowsUtil.setWindowStyle(window, hWndInsertAfter, style, exStyle);
        }
    }


    // This is as close as it gets to a true borderless experience on Windows
    // without it being plagued by some pretty annoying bugs.
    //
    // If we can't get "real" fullscreen to work on Windows - whether due to
    // the OS being severely broken, driver issues, GLFW quirks, or anything
    // in between - we can force the game window to remain in windowed mode
    // and simply stretch it to cover the whole screen.
    // However, Windows has a "feature" that detects windowed fullscreen and
    // forcefully switches it to its, ahem, problematic exclusive fullscreen.
    // Fortunately, we can bypass this behavior by adjusting one of
    // the window's dimensions by just a single pixel.
    // The difference is unnoticeable to humans, and it gets the job done.
    //
    // The only known issue with this implementation is that OBS Studio
    // doesn't detect it as proper fullscreen, because someone made
    // the detection rules too strict - the window must match
    // the monitor's dimensions *exactly* for OBS Game Capture
    // to pick it up. I submitted a PR to fix the issue:
    //  - https://github.com/obsproject/obs-studio/pull/10880/
    // Unfortunately, it hasn't been merged yet.
    //
    // See:
    //  - https://github.com/Kir-Antipov/cubes-without-borders/issues/16
    private static class WindowsWindowedFullscreen implements FullscreenType {
        @Override
        public String id() {
            return "windows:windowed";
        }

        @Override
        public boolean supported() {
            return SystemUtil.isWindows();
        }

        @Override
        public int priority() {
            // The priority table for Windows fullscreen types is as follows:
            //  - Windowed    1000
            //  - Borderless  100
            //  - Exclusive   10
            //  - Default    -1
            return 1000;
        }

        @Override
        public void enable(Window window, Monitor monitor, VideoMode videoMode) {
            // We cannot change the video mode without attaching the window
            // to a monitor. Thus, enforce the use of the current mode instead.
            videoMode = monitor.getCurrentVideoMode();

            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

            window.x = monitor.getViewportX();
            window.y = monitor.getViewportY();
            window.width = videoMode.getWidth();
            window.height = videoMode.getHeight() + 1;

            GLFW.glfwSetWindowMonitor(
                window.getHandle(),
                0,
                window.x,
                window.y,
                window.width,
                window.height,
                -1
            );
        }

        @Override
        public void disable(Window window) {
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
            GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);
        }
    }

    private FullscreenTypes() { }
}
