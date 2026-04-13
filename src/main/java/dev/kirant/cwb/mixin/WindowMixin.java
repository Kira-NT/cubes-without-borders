package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(Window.class)
abstract class WindowMixin implements FullscreenManager {
    private @Shadow @Final ScreenManager screenManager;

    private @Shadow Optional<VideoMode> preferredFullscreenVideoMode;

    private @Shadow int windowedX;

    private @Shadow int windowedY;

    private @Shadow int windowedWidth;

    private @Shadow int windowedHeight;

    private @Shadow boolean fullscreen;

    private @Shadow boolean actuallyFullscreen;

    private boolean borderless;

    private FullscreenType previousFullscreenType;

    private FullscreenType currentFullscreenType;

    @Override
    public FullscreenMode getFullscreenMode() {
        return this.fullscreen ? this.borderless ? FullscreenMode.BORDERLESS : FullscreenMode.ON : FullscreenMode.OFF;
    }

    @Override
    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        FullscreenMode currentFullscreenMode = this.getFullscreenMode();
        this.fullscreen = fullscreenMode != FullscreenMode.OFF;
        this.borderless = this.fullscreen ? fullscreenMode == FullscreenMode.BORDERLESS : this.borderless;
        this.actuallyFullscreen = (currentFullscreenMode == fullscreenMode) == this.fullscreen;
        this.syncCurrentFullscreenState();
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/ScreenManager;getMonitor(J)Lcom/mojang/blaze3d/platform/Monitor;"))
    private Monitor patchInitialFullscreenMode(ScreenManager screenManager, long monitor, Operation<Monitor> getMonitor) {
        // Do not create a fullscreen window right away, as it would steal the user's focus.
        // We will handle the transition later.
        this.fullscreen = this.actuallyFullscreen = false;

        //? if >=26.1 {
        // Ensure that the default fullscreen mode is always exclusive.
        GLFW.glfwWindowHint(GLFW.GLFW_SOFT_FULLSCREEN, GLFW.GLFW_FALSE);
        Minecraft.getInstance().options.exclusiveFullscreen().set(true);
        //?}

        // Hide Minecraft from broken Nvidia drivers on Windows.
        if (OS.isWindows()) {
            MinecraftWindow.Windows.concealCommandLine();
        }

        GLFW.glfwWindowHint(GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, CWB.CONFIG.getUseScaledFramebuffer() ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        return getMonitor.call(screenManager, monitor);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V"))
    private void init(Window window, Operation<Void> setMode) {
        if (OS.isWindows()) {
            MinecraftWindow.Windows.restoreCommandLine();
        }

        CWBConfig config = CWB.CONFIG;
        this.fullscreen = this.actuallyFullscreen = config.getFullscreenMode() != FullscreenMode.OFF;
        this.borderless = config.getPreferredFullscreenMode() == FullscreenMode.BORDERLESS;
        this.borderless = this.borderless || config.getFullscreenMode() == FullscreenMode.BORDERLESS;
        this.previousFullscreenType = this.currentFullscreenType = null;
        this.syncCurrentFullscreenState();
        setMode.call(window);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;refreshFramebufferSize()V"))
    private void fixupFramebufferSize(Window window, Operation<Void> refreshFramebufferSize) {
        GLFW.glfwPollEvents();
        refreshFramebufferSize.call(window);
    }

    @Inject(method = "setMode", at = @At("HEAD"), cancellable = true)
    private void enableFullscreen(CallbackInfo ci) {
        Window window = (Window)(Object)this;
        CWBConfig config = CWB.CONFIG;

        this.previousFullscreenType = this.currentFullscreenType;
        if (this.fullscreen) {
            FullscreenType requestedFullscreenType = this.borderless ? config.getBorderlessFullscreenType() : config.getFullscreenType();
            FullscreenType defaultFullscreenType = this.borderless ? FullscreenTypes.borderless() : FullscreenTypes.exclusive();
            this.currentFullscreenType = FullscreenTypes.validate(requestedFullscreenType, defaultFullscreenType);
        } else {
            // Let the original method deal with the windowed mode.
            this.currentFullscreenType = null;
            return;
        }

        Monitor monitor = this.screenManager.findBestMonitor(window);
        if (monitor == null) {
            // We couldn't detect a monitor to attach this window to.
            // Let the original method deal with this problem.
            this.currentFullscreenType = null;
            return;
        }

        if (this.currentFullscreenType == FullscreenTypes.exclusive()) {
            // The player requests built-in fullscreen mode.
            // Once again, let the original method deal with it.
            return;
        }

        boolean wasInWindowedMode = this.previousFullscreenType == null;
        if (wasInWindowedMode) {
            this.windowedX = window.x;
            this.windowedY = window.y;
            this.windowedWidth = window.width;
            this.windowedHeight = window.height;
        } else {
            this.previousFullscreenType.disable(window);
        }

        this.currentFullscreenType.enable(window, monitor, monitor.getPreferredVidMode(this.preferredFullscreenVideoMode));
        ci.cancel();
    }

    @Inject(method = "setMode", at = {
        @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", ordinal = 0),
        @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;windowedX:I", opcode = Opcodes.GETFIELD),
    })
    private void disableFullscreen(CallbackInfo ci) {
        if (this.previousFullscreenType != null) {
            this.previousFullscreenType.disable((Window)(Object)this);
        }
    }

    @WrapOperation(method = "setMode", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetWindowMonitor(J)J", ordinal = 0))
    private long wasFullscreen(long window, Operation<Long> getWindowMonitor) {
        return this.previousFullscreenType == null ? getWindowMonitor.call(window) : -1;
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void save(CallbackInfo ci) {
        CWBConfig config = CWB.CONFIG;
        config.setFullscreenMode(this.getFullscreenMode());
        config.setPreferredFullscreenMode(this.borderless ? FullscreenMode.BORDERLESS : FullscreenMode.ON);
        config.save();
    }

    private void syncCurrentFullscreenState() {
        //? if >=1.19 {
        Minecraft.getInstance().options.fullscreen().set(this.fullscreen);
         //?} else {
        /*Minecraft.getInstance().options.fullscreen = this.fullscreen;
        *///?}
    }
}
