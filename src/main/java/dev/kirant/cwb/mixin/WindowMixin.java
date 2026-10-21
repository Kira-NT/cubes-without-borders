package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.*;
import dev.kirant.cwb.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.nio.IntBuffer;
import java.util.Optional;

@Mixin(value = Window.class, priority = 0)
abstract class WindowMixin implements FullscreenManager {
    private static @Shadow @Final Logger LOGGER;

    private @Shadow Optional<VideoMode> preferredFullscreenVideoMode;

    private @Shadow int windowedX;

    private @Shadow int windowedY;

    private @Shadow int windowedWidth;

    private @Shadow int windowedHeight;

    private @Shadow boolean exclusiveFullscreen;

    private @Shadow boolean fullscreenRequested;

    private @Shadow boolean fullscreen;

    private FullscreenMode preferredFullscreenMode;

    private FullscreenType previousFullscreenType;

    private FullscreenType currentFullscreenType;

    @Override
    public FullscreenMode getFullscreenMode() {
        return this.fullscreenRequested ? this.preferredFullscreenMode : FullscreenMode.OFF;
    }

    @Override
    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        FullscreenMode currentFullscreenMode = this.getFullscreenMode();
        this.fullscreenRequested = fullscreenMode != FullscreenMode.OFF;
        this.preferredFullscreenMode = this.fullscreenRequested ? fullscreenMode : this.preferredFullscreenMode;
        this.fullscreen = (currentFullscreenMode == fullscreenMode) == this.fullscreenRequested;
        Minecraft.getInstance().options.fullscreen/*? >=1.19 {*/().set/*?} else {*//*=*//*?}*/(this.fullscreenRequested);
    }

    //?if <26.3 {
    /*@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/MonitorManager;getMonitor(J)Lcom/mojang/blaze3d/platform/Monitor;"))
    private Monitor patchInitialFullscreenMode(MonitorManager monitorManager, long monitor, Operation<Monitor> getMonitor) {
    *///?} else {
    @WrapOperation(
        method = "<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/renderpearl/api/device/GpuBackend;I)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/MonitorManager;getMonitor(I)Lcom/mojang/blaze3d/platform/Monitor;")
    )
    private Monitor patchInitialFullscreenMode(MonitorManager monitorManager, int monitor, Operation<Monitor> getMonitor) {
        //?}
        // Do not create a fullscreen window right away, as it would steal the user's focus.
        // We will handle the transition later.
        this.fullscreenRequested = this.fullscreen = false;

        // Ensure that the default fullscreen mode is always exclusive.
        //? if >=26.1 <26.3
        //org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_SOFT_FULLSCREEN, 0);
        //? if >=26.1 {
        this.exclusiveFullscreen = true;
        try { Minecraft.getInstance().options.exclusiveFullscreen().set(true); } catch (Throwable t) { }
        //?}

        // Hide Minecraft from broken Nvidia drivers on Windows.
        if (OS.isWindows()) {
            MinecraftWindow.Windows.concealCommandLine();
        }

        //? if <26.3
        //org.lwjgl.glfw.GLFW.glfwWindowHint(org.lwjgl.glfw.GLFW.GLFW_COCOA_RETINA_FRAMEBUFFER, CWB.CONFIG.getUseScaledFramebuffer() ? 1 : 0);

        return getMonitor.call(monitorManager, monitor);
    }

    @WrapOperation(
        method = /*? <26.3 {*//*"<init>"*//*?} else {*/"<init>(Lcom/mojang/blaze3d/platform/WindowEventHandler;Lcom/mojang/blaze3d/platform/DisplayData;Ljava/lang/String;ZLjava/lang/String;Lcom/mojang/blaze3d/platform/MonitorManager;Lcom/mojang/renderpearl/api/device/GpuBackend;I)V"/*?}*/,
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V")
    )
    private void init(Window window, Operation<Void> setMode) {
        if (OS.isWindows()) {
            MinecraftWindow.Windows.restoreCommandLine();
        }

        CWBConfig config = CWB.CONFIG;
        this.fullscreenRequested = this.fullscreen = !config.getUseDelayedFullscreen() && config.getFullscreenMode() != FullscreenMode.OFF;
        this.preferredFullscreenMode = config.getFullscreenMode() == FullscreenMode.OFF ? config.getPreferredFullscreenMode() : config.getFullscreenMode();
        this.previousFullscreenType = this.currentFullscreenType = null;
        Minecraft.getInstance().options.fullscreen/*? >=1.19 {*/().set/*?} else {*//*=*//*?}*/(this.fullscreenRequested);

        WindowHints.AUTO_ICONIFY.set(window, true);
        setMode.call(window);

        //? if <26.3 && (forge || neoforge) {
        /*if (OS.isMacOS() && !config.getUseScaledFramebuffer()) {
            MinecraftWindow.MacOS.setWantsBestResolutionOpenGLSurface(window, false);
        }
        *///?}

        // Make sure the window dimensions are up to date, as they may differ from those passed to glfwSetWindowMonitor.
        // This is mainly relevant on platforms where glfwGetVideoMode may return an invalid value, like on macOS.
        this.refreshWindowSize();
    }

    @Inject(method = { "updateFullscreenIfChanged", "changeFullscreenVideoMode" }, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setMode()V", shift = At.Shift.AFTER))
    private void onChangeFullscreenVideoMode(CallbackInfo ci) {
        // Make sure that window dimensions are up to date before
        // resizing the GUI based on potentially stale values.
        this.refreshWindowSize();
        this.refreshFramebufferSize();
    }

    @Inject(method = "setMode", at = @At("HEAD"), cancellable = true)
    private void enableFullscreen(CallbackInfo ci) {
        CWBConfig config = CWB.CONFIG;
        Window window = (Window)(Object)this;
        Monitor monitor = null;

        this.previousFullscreenType = this.currentFullscreenType;
        this.currentFullscreenType = null;

        boolean useCustomFullscreenType = this.fullscreenRequested;
        useCustomFullscreenType = useCustomFullscreenType && (monitor = window.monitorManager.findBestMonitor(window)) != null;
        useCustomFullscreenType = useCustomFullscreenType && (this.currentFullscreenType = config.getPreferredFullscreenType(this.preferredFullscreenMode)) != FullscreenTypes.exclusive();
        if (!useCustomFullscreenType) {
            //? if >=26.3
            this.fullscreen = this.previousFullscreenType != null;
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

        this.currentFullscreenType.enable(window, monitor, monitor.getPreferredVideoMode(this.preferredFullscreenVideoMode));
        this.fullscreen = this.fullscreenRequested;
        ci.cancel();
    }

    @Inject(method = "setMode", at = {
        //? if >=26.3 {
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;applyFullscreen()Z"),
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;applyWindowed()Z"),
        //?} else {
        /*@At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", ordinal = 0),
        @At(value = "FIELD", target = "Lcom/mojang/blaze3d/platform/Window;windowedX:I", opcode = org.objectweb.asm.Opcodes.GETFIELD),
        *///?}
    })
    private void disableFullscreen(CallbackInfo ci) {
        if (this.previousFullscreenType != null) {
            this.previousFullscreenType.disable((Window)(Object)this);
        }
    }

    //?if >=26.3 {
    @ModifyArg(method = "createWindow", at = @At(value = "INVOKE", target = "Lcom/mojang/renderpearl/api/device/GpuBackend;createWindow(Ljava/lang/String;IIJ)J"), index = 3, require = 0)
    private long patchPixelDensity(long flags) {
        long flag = org.lwjgl.sdl.SDLVideo.SDL_WINDOW_HIGH_PIXEL_DENSITY;
        return CWB.CONFIG.getUseScaledFramebuffer() ? (flags | flag) : (flags & ~flag);
    }

    @Inject(method = "isWindowFullscreen", at = @At("HEAD"), cancellable = true)
    private void fixFullscreenState(CallbackInfoReturnable<Boolean> cir) {
        // Why do we even record the fullscreen state we are in if we are gonna query it dynamically each and every time anyway?
        cir.setReturnValue(this.fullscreenRequested);
    }

    @WrapOperation(method = "handleEvent", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;updateFullscreenState()V"))
    private void disableFullscreenStateUpdate(Window window, Operation<Void> updateFullscreenState) {
        // Genuinely, this code was written either by someone with a severe case of paranoia, or by a chatbot.
        // No, we don't need an event handler that overwrites the recorded fullscreen state immediately after
        // we've entered fullscreen and set that state ourselves.
        // It only complicates the handling of windowed fullscreen modes.
    }

    @WrapOperation(method = { "onMove", "onResize" }, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;isWindowFullscreen()Z", ordinal = 0))
    private boolean disableWindowedRectOverride(Window window, Operation<Void> isWindowFullscreen) {
        // We only ever need to record the window's position and dimensions when we enter fullscreen.
        // Doing so at any other time only creates opportunities for a desync to happen.
        return true;
    }

    @WrapOperation(method = "updateWindowMouseGrab", at = @At(value = "INVOKE", target = "Lorg/lwjgl/sdl/SDLVideo;SDL_SetWindowMouseGrab(JZ)Z"))
    private boolean disableWindowMouseGrab(long window, boolean grabbed, Operation<Boolean> original) {
        // The game already locks the cursor in place while you are actually playing it.
        // There's zero to none reasons to additionally confine it to the area of the window
        // while the player is paused or in a menu. Did literally anybody ask for it?
        return original.call(window, false);
    }

    @WrapOperation(method = "setMode", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;applyFullscreen()Z"))
    private boolean enableExclusiveFullscreenWithBorderlessFallback(Window window, Operation<Boolean> applyFullscreen) {
        try {
            return applyFullscreen.call(window);
        } catch (Throwable e) {
            LOGGER.error("Failed to enable exclusive fullscreen mode. There may be an issue with your graphics drivers. Falling back to borderless mode.", e);
            this.setFullscreenMode(FullscreenMode.BORDERLESS);
            return true;
        }
    }
    //?} else {
    /*@WrapOperation(method = "setMode", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", ordinal = 0))
    private void enableExclusiveFullscreenWithBorderlessFallback(long window, long monitor, int xpos, int ypos, int width, int height, int refreshRate, Operation<Void> glfwSetWindowMonitor) {
        try {
            glfwSetWindowMonitor.call(window, monitor, xpos, ypos, width, height, refreshRate);
        } catch (Throwable e) {
            LOGGER.error("Failed to enable exclusive fullscreen mode. There may be an issue with your graphics drivers. Falling back to borderless mode.", e);
            this.setFullscreenMode(FullscreenMode.BORDERLESS);
        }
    }

    @WrapOperation(method = "setMode", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetWindowMonitor(J)J", ordinal = 0))
    private long wasFullscreen(long window, Operation<Long> getWindowMonitor) {
        return this.previousFullscreenType == null ? getWindowMonitor.call(window) : -1;
    }
    *///?}

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
            //? if >=26.3 {
            org.lwjgl.sdl.SDLEvents.SDL_PumpEvents();
            org.lwjgl.sdl.SDLVideo.SDL_SyncWindow(window.handle());
            org.lwjgl.sdl.SDLVideo.SDL_GetWindowSize(window.handle(), size, size.slice(1, 1));
            //?} else {
            /*org.lwjgl.glfw.GLFW.glfwPollEvents();
            org.lwjgl.glfw.GLFW.glfwGetWindowSize(window.handle(), size, (IntBuffer)size.duplicate().position(1));
            *///?}
            window.width = size.get(0) > 0 ? size.get(0) : 1;
            window.height = size.get(1) > 0 ? size.get(1) : 1;
        }
    }

    protected @Shadow abstract void refreshFramebufferSize();
}
