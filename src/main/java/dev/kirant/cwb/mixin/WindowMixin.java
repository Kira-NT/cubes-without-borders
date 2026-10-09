package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.IntBuffer;
import java.util.Optional;

@Mixin(value = Window.class, priority = 0)
abstract class WindowMixin implements FullscreenManager {
    private static @Shadow @Final Logger LOGGER;

    private @Shadow @Final MonitorManager monitorManager;

    private @Shadow Optional<VideoMode> preferredFullscreenVideoMode;

    private @Shadow int windowedX;

    private @Shadow int windowedY;

    private @Shadow int windowedWidth;

    private @Shadow int windowedHeight;

    private @Shadow boolean fullscreen;

    private @Shadow boolean actuallyFullscreen;

    private FullscreenMode preferredFullscreenMode;

    private FullscreenType previousFullscreenType;

    private FullscreenType currentFullscreenType;

    @Override
    public FullscreenMode getFullscreenMode() {
        return this.fullscreen ? this.preferredFullscreenMode : FullscreenMode.OFF;
    }

    @Override
    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        FullscreenMode currentFullscreenMode = this.getFullscreenMode();
        this.fullscreen = fullscreenMode != FullscreenMode.OFF;
        this.preferredFullscreenMode = this.fullscreen ? fullscreenMode : this.preferredFullscreenMode;
        this.actuallyFullscreen = (currentFullscreenMode == fullscreenMode) == this.fullscreen;
        Minecraft.getInstance().options.fullscreen/*? >=1.19 {*/().set/*?} else {*//*=*//*?}*/(this.fullscreen);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/MonitorManager;getMonitor(J)Lcom/mojang/blaze3d/platform/Monitor;"))
    private Monitor patchInitialFullscreenMode(MonitorManager screenManager, long monitor, Operation<Monitor> getMonitor) {
        // Do not create a fullscreen window right away, as it would steal the user's focus.
        // We will handle the transition later.
        this.fullscreen = this.actuallyFullscreen = false;

        //? if >=26.1 {
        // Ensure that the default fullscreen mode is always exclusive.
        org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_SOFT_FULLSCREEN, 0);
        Minecraft.getInstance().options.exclusiveFullscreen().set(true);
        //?}

        // Hide Minecraft from broken Nvidia drivers on Windows.
        if (OS.isWindows()) {
            MinecraftWindow.Windows.concealCommandLine();
        }

        org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, CWB.CONFIG.getUseScaledFramebuffer() ? 1 : 0);
        return getMonitor.call(screenManager, monitor);
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V"))
    private void init(Window window, Operation<Void> setMode) {
        if (OS.isWindows()) {
            MinecraftWindow.Windows.restoreCommandLine();
        }

        CWBConfig config = CWB.CONFIG;
        this.fullscreen = this.actuallyFullscreen = !config.getUseDelayedFullscreen() && config.getFullscreenMode() != FullscreenMode.OFF;
        this.preferredFullscreenMode = config.getFullscreenMode() == FullscreenMode.OFF ? config.getPreferredFullscreenMode() : config.getFullscreenMode();
        this.previousFullscreenType = this.currentFullscreenType = null;
        Minecraft.getInstance().options.fullscreen/*? >=1.19 {*/().set/*?} else {*//*=*//*?}*/(this.fullscreen);

        setMode.call(window);

        // Make sure the window dimensions are up to date, as they may differ from those passed to glfwSetWindowMonitor.
        // This is mainly relevant on platforms where glfwGetVideoMode may return an invalid value, like on macOS.
        org.lwjgl.glfw.GLFW.glfwPollEvents();
        this.refreshWindowSize();
    }

    @Inject(method = { "updateFullscreenIfChanged", "changeFullscreenVideoMode" }, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V", shift = At.Shift.AFTER))
    private void onChangeFullscreenVideoMode(CallbackInfo ci) {
        // Make sure that window dimensions are up to date before
        // resizing the GUI based on potentially stale values.
        this.refreshWindowSize();
    }

    @Inject(method = "setMode", at = @At("HEAD"), cancellable = true)
    private void enableFullscreen(CallbackInfo ci) {
        CWBConfig config = CWB.CONFIG;
        Window window = (Window)(Object)this;
        Monitor monitor = null;

        this.previousFullscreenType = this.currentFullscreenType;
        this.currentFullscreenType = null;

        boolean useCustomFullscreenType = this.fullscreen;
        useCustomFullscreenType = useCustomFullscreenType && (monitor = this.monitorManager.findBestMonitor(window)) != null;
        useCustomFullscreenType = useCustomFullscreenType && (this.currentFullscreenType = config.getPreferredFullscreenType(this.preferredFullscreenMode)) != FullscreenTypes.exclusive();
        if (!useCustomFullscreenType) {
            return;
        }

        if (this.previousFullscreenType == null) {
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

    @WrapOperation(method = "setMode", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", ordinal = 0))
    private void enableExclusiveFullscreenWithBorderlessFallback(long window, long monitor, int xpos, int ypos, int width, int height, int refreshRate, Operation<Void> glfwSetWindowMonitor) {
        try {
            glfwSetWindowMonitor.call(window, monitor, xpos, ypos, width, height, refreshRate);
        } catch (Throwable e) {
            LOGGER.error("Failed to enable exclusive fullscreen mode. There may be an issue with your graphics drivers. Falling back to borderless mode.", e);
            this.setFullscreenMode(FullscreenMode.BORDERLESS);
        }
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
        config.setPreferredFullscreenMode(this.preferredFullscreenMode);
        config.save();
    }

    private void refreshWindowSize() {
        Window window = (Window)(Object)this;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer size = stack.mallocInt(2);
            org.lwjgl.glfw.GLFW.glfwGetWindowSize(window.handle(), size, (IntBuffer)size.duplicate().position(1));
            window.width = size.get(0) > 0 ? size.get(0) : 1;
            window.height = size.get(1) > 0 ? size.get(1) : 1;
        }
    }
}
