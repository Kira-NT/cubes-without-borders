package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.windows.User32;

@Environment(EnvType.CLIENT)
public final class WindowsUtil {
    public static boolean setWindowStyle(Window window, long hWndInsertAfter, long style, long exStyle) {
        long hWnd = GLFWNativeWin32.glfwGetWin32Window(window.getHandle());
        if (hWnd == 0) {
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
        User32.SetWindowPos(hWnd, hWndInsertAfter, x, y, width, height, flags);

        // Finally, update the style of the window (including "extended styles").
        User32.SetWindowLongPtr(hWnd, User32.GWL_STYLE, style);
        User32.SetWindowLongPtr(hWnd, User32.GWL_EXSTYLE, exStyle);
        return true;
    }

    private WindowsUtil() { }
}
