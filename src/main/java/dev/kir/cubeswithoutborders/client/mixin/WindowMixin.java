package dev.kir.cubeswithoutborders.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kir.cubeswithoutborders.client.util.*;
import dev.kir.cubeswithoutborders.client.option.FullscreenMode;
import dev.kir.cubeswithoutborders.client.option.FullscreenOptions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.*;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(value = Window.class)
abstract class WindowMixin implements FullscreenWindowState {
    @Shadow
    private static @Final Logger LOGGER;

    @Shadow
    private @Final long handle;

    @Shadow
    private int x;

    @Shadow
    private int y;

    @Shadow
    private int windowedX;

    @Shadow
    private int windowedY;

    @Shadow
    private int width;

    @Shadow
    private int height;

    @Shadow
    private int windowedWidth;

    @Shadow
    private int windowedHeight;

    @Shadow
    private boolean fullscreen;

    private boolean borderless;

    private boolean prefersBorderless;

    private boolean currentBorderless;

    @Shadow
    private @Final MonitorTracker monitorTracker;

    @Shadow
    protected abstract void updateWindowRegion();

    @Shadow
    private Optional<VideoMode> videoMode;

    @Override
    public FullscreenMode getFullscreenMode() {
        return FullscreenMode.get(this.fullscreen, this.borderless);
    }

    @Override
    public void setFullscreenMode(FullscreenMode mode) {
        this.borderless = mode == FullscreenMode.BORDERLESS;
        this.fullscreen = mode == FullscreenMode.ON;
        this.prefersBorderless = this.prefersBorderless & !this.fullscreen | this.borderless;
    }

    @Override
    public FullscreenMode getPreferredFullscreenMode() {
        return this.prefersBorderless ? FullscreenMode.BORDERLESS : FullscreenMode.ON;
    }

    @Override
    public void setPreferredFullscreenMode(FullscreenMode mode) {
        this.prefersBorderless = mode == FullscreenMode.BORDERLESS;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/MonitorTracker;getMonitor(J)Lnet/minecraft/client/util/Monitor;", ordinal = 0))
    private Monitor getMonitor(MonitorTracker monitorTracker, long pointer, Operation<Monitor> getMonitor, WindowEventHandler _arg0, MonitorTracker _arg1, WindowSettings settings) {
        Monitor defaultMonitor = getMonitor.call(monitorTracker, pointer);
        MonitorInfo monitorInfo = ((MonitorInfoContainer)settings).getMonitorInfo();
        return MonitorLookup.findMonitor(monitorTracker, monitorInfo).orElse(defaultMonitor);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", ordinal = 0))
    private long createWindow(int width, int height, CharSequence title, long monitorHandle, long share, Operation<Long> createWindow, WindowEventHandler eventHandler, MonitorTracker monitorTracker, WindowSettings settings) {
        // Minecraft tries to set its windowed dimensions (e.g., the usual 854x480)
        // as the desired fullscreen resolution. Thanks, Mojang.
        Monitor monitor = monitorHandle == 0 ? null : monitorTracker.getMonitor(monitorHandle);
        if (monitor != null) {
            boolean isBorderless = ((FullscreenWindowState)settings).getFullscreenMode() == FullscreenMode.BORDERLESS;
            VideoMode videoMode = monitor.findClosestVideoMode(isBorderless ? Optional.empty() : this.videoMode);
            this.width = width = videoMode.getWidth();
            this.height = height = videoMode.getHeight();
        }

        return createWindow.call(width, height, title, monitorHandle, share);
    }

    @Inject(method = "setWindowedSize", at = @At("HEAD"))
    private void setWindowedSize(CallbackInfo ci) {
        this.borderless = false;
    }

    @Inject(method = "toggleFullscreen", at = @At("RETURN"))
    private void toggleFullscreen(CallbackInfo ci) {
        this.borderless = this.borderless && !this.fullscreen;
    }

    @Inject(method = "swapBuffers", at = @At("RETURN"))
    private void swapBuffers(CallbackInfo ci) {
        if (this.currentBorderless != this.borderless) {
            this.updateWindowRegion();
        }
    }

    @Inject(method = "updateWindowRegion", at = @At("HEAD"), cancellable = true)
    private void updateBorderlessWindowRegion(CallbackInfo ci) {
        if (!this.borderless || this.currentBorderless) {
            return;
        }

        Monitor monitor = this.monitorTracker.getMonitor((Window)(Object)this);
        if (monitor == null) {
            LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
            this.borderless = false;
            ci.cancel();
            return;
        }

        VideoMode videoMode = monitor.getCurrentVideoMode();
        boolean isInWindowedMode = GLFW.glfwGetWindowMonitor(this.handle) == 0L;
        if (isInWindowedMode) {
            this.windowedX = this.x;
            this.windowedY = this.y;
            this.windowedWidth = this.width;
            this.windowedHeight = this.height;
        }

        long monitorHandle = monitor.getHandle();
        this.x = monitor.getViewportX();
        this.y = monitor.getViewportY();
        this.width = videoMode.getWidth();
        this.height = videoMode.getHeight();
        int refreshRate = videoMode.getRefreshRate();
        GLFW.glfwSetWindowMonitor(this.handle, monitorHandle, this.x, this.y, this.width, this.height, refreshRate);
        WindowUtil.disableExclusiveFullscreen((Window)(Object)this);

        this.currentBorderless = true;
        ci.cancel();
    }

    @Inject(method = "updateWindowRegion", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", ordinal = 0, shift = At.Shift.AFTER))
    private void restoreExclusiveFullscreen(CallbackInfo ci) {
        WindowUtil.enableExclusiveFullscreen((Window)(Object)this);
    }

    @Inject(method = "updateWindowRegion", at = @At("RETURN"))
    private void updateBorderlessStatus(CallbackInfo ci) {
        this.currentBorderless = this.borderless;
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void saveSelectedMonitor(CallbackInfo ci) {
        Monitor monitor = this.monitorTracker.getMonitor((Window)(Object)this);
        if (monitor == null) {
            return;
        }

        MonitorInfo monitorInfo = MonitorInfo.of(monitor);
        GameOptions options = MinecraftClient.getInstance().options;
        ((FullscreenOptions)options).getFullscreenMonitor().setValue(monitorInfo);
        options.write();
    }
}
