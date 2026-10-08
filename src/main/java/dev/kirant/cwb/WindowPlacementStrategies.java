package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public final class WindowPlacementStrategies {
    public static WindowPlacementStrategy BIND_TO_MONITOR = (window, monitor, videoMode) -> {
        window.x = monitor.x();
        window.y = monitor.y();
        window.width = videoMode.getWidth();
        window.height = videoMode.getHeight();
        WindowPlacementStrategies.bindToMonitor(window, monitor, videoMode);
    };

    public static WindowPlacementStrategy FILL_MONITOR = (window, monitor, _) -> {
        window.x = monitor.x();
        window.y = monitor.y();
        window.width = monitor.currentMode().getWidth();
        window.height = monitor.currentMode().getHeight();
        WindowPlacementStrategies.moveAndResize(window);
    };

    public static WindowPlacementStrategy FILL_MONITOR_SAFE_AREA = (window, monitor, _) -> {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer area = stack.mallocInt(4);
            org.lwjgl.glfw.GLFW.glfwGetMonitorWorkarea(monitor.monitor(), area, (IntBuffer)area.duplicate().position(1), (IntBuffer)area.duplicate().position(2), (IntBuffer)area.duplicate().position(3));

            window.x = area.get(0);
            window.y = area.get(1);
            window.width = area.get(2);
            window.height = area.get(3);
        }
        WindowPlacementStrategies.maximize(window);
    };

    public static WindowPlacementStrategy OVERSCAN = (window, monitor, _) -> {
        window.x = monitor.x();
        window.y = monitor.y();
        window.width = monitor.currentMode().getWidth();
        window.height = monitor.currentMode().getHeight() + 1;
        WindowPlacementStrategies.moveAndResize(window);
    };

    public static WindowPlacementStrategy MAXIMIZE = (window, _, _) -> WindowPlacementStrategies.maximize(window);

    private static void bindToMonitor(Window window, Monitor monitor, VideoMode videoMode) {
        org.lwjgl.glfw.GLFW.glfwSetWindowMonitor(window.handle(), monitor.monitor(), window.x, window.y, window.width, window.height, videoMode.getRefreshRate());
    }

    private static void moveAndResize(Window window) {
        org.lwjgl.glfw.GLFW.glfwSetWindowMonitor(window.handle(), 0, window.x, window.y, window.width, window.height, -1);
    }

    private static void maximize(Window window) {
        org.lwjgl.glfw.GLFW.glfwMaximizeWindow(window.handle());
    }

    private WindowPlacementStrategies() { }
}
