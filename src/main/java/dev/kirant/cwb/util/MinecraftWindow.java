package dev.kirant.cwb.util;

import ca.weblite.objc.Client;
import ca.weblite.objc.Proxy;
import com.mojang.blaze3d.platform.Window;
import com.sun.jna.Pointer;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWNativeCocoa;
import org.lwjgl.glfw.GLFWNativeWin32;
import org.lwjgl.system.*;
import org.lwjgl.system.windows.User32;

import java.nio.ByteBuffer;
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

        private static final String FAKE_CMD = "do.not.touch.this.process.please / net.minecraft.client.main.Main / ";

        private static String utf16Cmd;

        private static String asciiCmd;

        private static ByteBuffer utf16Buffer;

        private static ByteBuffer asciiBuffer;

        public static void concealCommandLine() {
            if (asciiBuffer != null) {
                // We've already concealed the original command line.
                return;
            }

            try (SharedLibrary kernel32 = APIUtil.apiCreateLibrary("kernel32")) {
                long utf16CmdPtr = JNI.callP(kernel32.getFunctionAddress("GetCommandLineW"));
                long asciiCmdPtr = JNI.callP(kernel32.getFunctionAddress("GetCommandLineA"));
                if (utf16CmdPtr == 0 || asciiCmdPtr == 0) {
                    // This should not be possible.
                    return;
                }

                utf16Cmd = MemoryUtil.memUTF16(utf16CmdPtr);
                asciiCmd = MemoryUtil.memASCII(asciiCmdPtr);
                if (!utf16Cmd.contains("java") || !asciiCmd.contains("java")) {
                    // Somebody has already modified the command line arguments.
                    return;
                }

                int utf16CmdLength = MemoryUtil.memLengthUTF16(utf16Cmd, true);
                int asciiCmdLength = MemoryUtil.memLengthASCII(asciiCmd, true);
                int utf16FakeCmdLength = MemoryUtil.memLengthUTF16(FAKE_CMD, true);
                int asciiFakeCmdLength = MemoryUtil.memLengthASCII(FAKE_CMD, true);
                if (utf16FakeCmdLength > utf16CmdLength || asciiFakeCmdLength > asciiCmdLength) {
                    // We don't want to write out of bounds.
                    return;
                }

                // Hide Minecraft from broken Nvidia drivers on Windows by
                // overwriting its command line arguments with some nonsense.
                utf16Buffer = MemoryUtil.memByteBuffer(utf16CmdPtr, utf16CmdLength);
                asciiBuffer = MemoryUtil.memByteBuffer(asciiCmdPtr, asciiCmdLength);
                MemoryUtil.memUTF16(FAKE_CMD, true, utf16Buffer);
                MemoryUtil.memASCII(FAKE_CMD, true, asciiBuffer);
            }
        }

        public static void restoreCommandLine() {
            if (utf16Cmd != null && utf16Buffer != null && FAKE_CMD.length() <= utf16Buffer.remaining() && FAKE_CMD.equals(MemoryUtil.memUTF16(utf16Buffer, FAKE_CMD.length()))) {
                MemoryUtil.memUTF16(utf16Cmd, true, utf16Buffer);
            }

            if (asciiCmd != null && asciiBuffer != null && FAKE_CMD.length() <= asciiBuffer.remaining() && FAKE_CMD.equals(MemoryUtil.memASCII(asciiBuffer, FAKE_CMD.length()))) {
                MemoryUtil.memASCII(asciiCmd, true, asciiBuffer);
            }

            utf16Cmd = asciiCmd = null;
            utf16Buffer = asciiBuffer = null;
        }

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
