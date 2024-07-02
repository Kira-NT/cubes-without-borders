package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.windows.User32;

@Environment(EnvType.CLIENT)
public final class WindowUtil {
    public static boolean enableExclusiveFullscreen(Window window) {
        GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_TRUE);

        if (SystemUtil.isWindows()) {
            return WindowUtil.enableExclusiveFullscreen_Windows(window);
        }
        return true;
    }

    public static boolean disableExclusiveFullscreen(Window window) {
        GLFW.glfwSetWindowAttrib(window.getHandle(), GLFW.GLFW_AUTO_ICONIFY, GLFW.GLFW_FALSE);

        if (SystemUtil.isWindows()) {
            return WindowUtil.disableExclusiveFullscreen_Windows(window);
        }
        return true;
    }

    private static boolean enableExclusiveFullscreen_Windows(Window window) {
        long hWndInsertAfter = User32.HWND_TOPMOST;
        long style = User32.WS_POPUP | User32.WS_VISIBLE | User32.WS_CLIPCHILDREN | User32.WS_CLIPSIBLINGS | User32.WS_SYSMENU | User32.WS_GROUP;
        long exStyle = User32.WS_EX_APPWINDOW | User32.WS_EX_ACCEPTFILES;
        return WindowUtil.setWindowStyle_Windows(window, hWndInsertAfter, style, exStyle);
    }

    private static boolean disableExclusiveFullscreen_Windows(Window window) {
        long hWndInsertAfter = User32.HWND_NOTOPMOST;
        long style = User32.WS_VISIBLE | User32.WS_CLIPCHILDREN | User32.WS_CLIPSIBLINGS | User32.WS_GROUP;
        long exStyle = User32.WS_EX_APPWINDOW | User32.WS_EX_ACCEPTFILES | User32.WS_EX_COMPOSITED | User32.WS_EX_LAYERED;
        return WindowUtil.setWindowStyle_Windows(window, hWndInsertAfter, style, exStyle);
    }

    private static boolean setWindowStyle_Windows(Window window, long hWndInsertAfter, long style, long exStyle) {
        long hWnd = GLFWNativeWin32.glfwGetWin32Window(window.getHandle());
        if (hWnd <= 0) {
            // This should never happen since the underlying
            // window is created in the `Window` constructor.
            return false;
        }

        // Change the Z-order of the window, leaving everything else as is.
        //
        // Technically, we don't need to set or even know the window dimensions, as
        // `SetWindowPos` will discard them anyway (because of `SWP_NOMOVE` and `SWP_NOSIZE`).
        // However, since it's Windows, I am **not** taking any chances here.
        int x = window.getX();
        int y = window.getY();
        int width = window.getWidth();
        int height = window.getHeight();
        int flags = User32.SWP_NOMOVE | User32.SWP_NOSIZE | User32.SWP_NOSENDCHANGING;
        boolean isUpdated = User32.SetWindowPos(hWnd, hWndInsertAfter, x, y, width, height, flags);
        if (isUpdated) {
            return false;
        }

        // Finally, update the style of the window (including "extended styles").
        User32.SetWindowLongPtr(hWnd, User32.GWL_STYLE, style);
        User32.SetWindowLongPtr(hWnd, User32.GWL_EXSTYLE, exStyle);
        return true;
    }

    private WindowUtil() { }
}
