package dev.kirant.cwb;

import com.mojang.blaze3d.platform.*;

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

    public static WindowPlacementStrategy OVERSCAN = (window, monitor, _) -> {
        window.x = monitor.x();
        window.y = monitor.y();
        window.width = monitor.currentMode().getWidth();
        window.height = monitor.currentMode().getHeight() + 1;
        WindowPlacementStrategies.moveAndResize(window);
    };

    private static void bindToMonitor(Window window, Monitor monitor, VideoMode videoMode) {
        org.lwjgl.glfw.GLFW.glfwSetWindowMonitor(window.handle(), monitor.monitor(), window.x, window.y, window.width, window.height, videoMode.getRefreshRate());
    }

    private static void moveAndResize(Window window) {
        org.lwjgl.glfw.GLFW.glfwSetWindowMonitor(window.handle(), 0, window.x, window.y, window.width, window.height, -1);
    }

    private WindowPlacementStrategies() { }
}
