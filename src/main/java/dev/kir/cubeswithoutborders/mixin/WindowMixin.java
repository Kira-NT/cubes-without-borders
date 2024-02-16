package dev.kir.cubeswithoutborders.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.kir.cubeswithoutborders.client.BorderlessWindowState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.*;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = Window.class)
abstract class WindowMixin implements BorderlessWindowState {
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

    private boolean currentBorderless;

    @Shadow
    private @Final MonitorTracker monitorTracker;

    @Shadow
    protected abstract void updateWindowRegion();

    @Override
    public boolean isBorderless() {
        return this.borderless;
    }

    @Override
    public void setBorderless(boolean borderless) {
        this.borderless = borderless;
        this.fullscreen = this.fullscreen & !borderless;
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

        RenderSystem.assertInInitPhase();
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

        // Do NOT move this line.
        // This call triggers the `onWindowSizeChanged` callback,
        // which resets values of `width` and `height`.
        GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_FALSE);

        this.x = 0;
        this.y = 0;
        this.width = videoMode.getWidth();
        this.height = videoMode.getHeight();
        GLFW.glfwSetWindowMonitor(this.handle, 0L, this.x, this.y, this.width, this.height, -1);

        this.currentBorderless = true;
        ci.cancel();
    }

    @Inject(method = "updateWindowRegion", at = @At(value = "FIELD", target = "Lnet/minecraft/client/util/Window;windowedX:I", ordinal = 1, shift = At.Shift.BEFORE))
    private void restoreWindowDecorations(CallbackInfo ci) {
        GLFW.glfwSetWindowAttrib(this.handle, GLFW.GLFW_DECORATED, GLFW.GLFW_TRUE);
    }

    @Inject(method = "updateWindowRegion", at = @At("RETURN"))
    private void updateBorderlessStatus(CallbackInfo ci) {
        this.currentBorderless = this.borderless;
    }

    @WrapOperation(method = "updateWindowRegion", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetWindowMonitor(J)J", ordinal = 0))
    private long getWindowMonitorIfNotBorderless(long handle, Operation<Long> getWindowMonitor) {
        if (this.currentBorderless) {
            return -1;
        }

        return getWindowMonitor.call(handle);
    }
}
