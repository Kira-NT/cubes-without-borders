package dev.kirant.cwb.util;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import com.mojang.blaze3d.platform.Window;
import com.sun.jna.Pointer;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.windows.User32;

import java.util.*;

public final class MinecraftWindow {
    public static Window getInstance() {
        //? if >=1.15 {
        return Minecraft.getInstance().getWindow();
        //?} else
        /*return Minecraft.getInstance().window;*/
    }

    public static long getHandle(Window window) {
        //? if >=1.21.9 {
        return window.handle();
        //?} else
        /*return window.getWindow();*/
    }

    public static final class Windows {
        private static final WeakHashMap<Window, Map.Entry<Long, Long>> WINDOW_STYLES = new WeakHashMap<>();

        public static void pleaseStopDiscardingFuckingFramesThankYou(Window window) {
            // Windows LOVES discarding frames provided by windowed applications when it thinks
            // it has more important things to do than render them - like downloading a new set
            // of updates, for example. Hopefully, this set of flags, similar to those you
            // would usually see on a regular fullscreen window, will give it a hint that
            // this is not the best fucking idea in this scenario.
            long style = User32.WS_VISIBLE | User32.WS_POPUP | User32.WS_CLIPSIBLINGS | User32.WS_CLIPCHILDREN;
            long exStyle = User32.WS_EX_ACCEPTFILES | User32.WS_EX_APPWINDOW;
            Windows.setStyle(window, style, exStyle);
        }

        public static void setStyle(Window window, long style, long exStyle) {
            long hWnd = GLFWNativeWin32.glfwGetWin32Window(MinecraftWindow.getHandle(window));
            long currentStyle = User32.GetWindowLongPtr(null, hWnd, User32.GWL_STYLE);
            long currentExStyle = User32.GetWindowLongPtr(null, hWnd, User32.GWL_EXSTYLE);
            WINDOW_STYLES.put(window, new AbstractMap.SimpleEntry<>(currentStyle, currentExStyle));

            int flags = User32.SWP_NOMOVE | User32.SWP_NOSIZE | User32.SWP_NOZORDER | User32.SWP_NOSENDCHANGING | User32.SWP_FRAMECHANGED;
            User32.SetWindowLongPtr(null, hWnd, User32.GWL_STYLE, style);
            User32.SetWindowLongPtr(null, hWnd, User32.GWL_EXSTYLE, exStyle);
            User32.SetWindowPos(null, hWnd, 0, 0, 0, 0, 0, flags);
        }

        public static void restoreStyle(Window window) {
            Map.Entry<Long, Long> styles = WINDOW_STYLES.get(window);
            if (styles == null) {
                return;
            }

            long hWnd = GLFWNativeWin32.glfwGetWin32Window(MinecraftWindow.getHandle(window));
            User32.SetWindowLongPtr(null, hWnd, User32.GWL_STYLE, styles.getKey());
            User32.SetWindowLongPtr(null, hWnd, User32.GWL_EXSTYLE, styles.getValue());
        }

        private Windows() { }
    }

    public static final class MacOS {
        public static void showGlobalUI() {
            final long NSApplicationPresentationDefault = 0L;

            MacOS.setPresentationOptions(NSApplicationPresentationDefault);
        }

        public static void hideGlobalUI() {
            final long NSApplicationPresentationHideDock = 1L << 1;
            final long NSApplicationPresentationHideMenuBar = 1L << 3;

            MacOS.setPresentationOptions(NSApplicationPresentationHideDock | NSApplicationPresentationHideMenuBar);
        }

        public static void setPresentationOptions(long presentationOptions) {
            Proxy sharedApplication = Client.getInstance().sendProxy("NSApplication", "sharedApplication");
            sharedApplication.send("setPresentationOptions:", presentationOptions);
        }

        public static void setHasShadow(Window window, boolean hasShadow) {
            long nsWindowPtr = GLFWNativeCocoa.glfwGetCocoaWindow(MinecraftWindow.getHandle(window));
            Proxy nsWindow = new Proxy(new Pointer(nsWindowPtr));
            nsWindow.send("setHasShadow:", hasShadow);
        }

        public static void setResizable(Window window, boolean resizable) {
            final long NSWindowStyleMaskResizable = 1L << 3;

            long nsWindowPtr = GLFWNativeCocoa.glfwGetCocoaWindow(MinecraftWindow.getHandle(window));
            Proxy nsWindow = new Proxy(new Pointer(nsWindowPtr));
            long styleMask = ((Number)nsWindow.send("styleMask")).longValue();
            long newStyleMask = (styleMask & ~NSWindowStyleMaskResizable) | (resizable ? NSWindowStyleMaskResizable : 0);
            nsWindow.send("setStyleMask:", newStyleMask);
        }

        private MacOS() { }
    }

    private MinecraftWindow() { }
}
