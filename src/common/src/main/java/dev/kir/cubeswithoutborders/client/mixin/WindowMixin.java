package dev.kir.cubeswithoutborders.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.*;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import dev.kir.cubeswithoutborders.client.util.SystemUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.VideoMode;
import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(Window.class)
abstract class WindowMixin implements FullscreenManager {
    @Shadow
    private @Final MonitorTracker monitorTracker;

    @Shadow
    private Optional<VideoMode> fullscreenVideoMode;

    @Shadow
    private int windowedX;

    @Shadow
    private int windowedY;

    @Shadow
    private int windowedWidth;

    @Shadow
    private int windowedHeight;

    @Shadow
    private boolean fullscreen;

    @Shadow
    private boolean currentFullscreen;

    private boolean borderless;

    private FullscreenType previousFullscreenType;

    private FullscreenType currentFullscreenType;


    @Override
    public FullscreenMode getFullscreenMode() {
        return this.fullscreen
            ? this.borderless
                ? FullscreenMode.BORDERLESS
                : FullscreenMode.ON
            : FullscreenMode.OFF;
    }

    @Override
    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        FullscreenMode currentFullscreenMode = this.getFullscreenMode();
        this.fullscreen = fullscreenMode != FullscreenMode.OFF;
        this.borderless = this.fullscreen ? fullscreenMode == FullscreenMode.BORDERLESS : this.borderless;
        this.currentFullscreen = (currentFullscreenMode == fullscreenMode) == this.fullscreen;

        // Sync the global `fullscreen` option with the current window state.
        MinecraftClient.getInstance().options.getFullscreen().setValue(this.fullscreen);
    }


    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/MonitorTracker;getMonitor(J)Lnet/minecraft/client/util/Monitor;"))
    private Monitor fixupMonitor(MonitorTracker monitorTracker, long pointer, Operation<Monitor> getMonitor) {
        // Do not create a fullscreen window right now, as it will steal
        // the user's focus. We'll handle the transition later.
        this.fullscreen = this.currentFullscreen = false;

        // Use preferred monitor if possible.
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        Monitor monitor = getMonitor.call(monitorTracker, pointer);
        Optional<Monitor> preferredMonitor = MonitorLookup.findMonitor(monitorTracker, config.getPreferredMonitor());
        return SystemUtil.isWindows() ? preferredMonitor.orElse(monitor) : monitor;
    }

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V"))
    private void init(long handle, Operation<Void> glfwMakeContextCurrent) {
        Window window = (Window)(Object)this;
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        this.fullscreen = this.currentFullscreen = config.getFullscreenMode() != FullscreenMode.OFF;
        this.borderless = config.getPreferredFullscreenMode() == FullscreenMode.BORDERLESS;
        this.borderless = this.borderless || config.getFullscreenMode() == FullscreenMode.BORDERLESS;
        this.previousFullscreenType = this.currentFullscreenType = null;

        // Jump to the preferred monitor if possible.
        // Unfortunately, this feature only works on Windows at the moment.
        // Wayland people have been unable to agree on how windows should
        // specify their own coordinates for the past ten years.
        // And macOS is just a little bit broken on its own.
        Monitor currentMonitor = this.monitorTracker.getMonitor(window);
        Monitor preferredMonitor = MonitorLookup.findMonitor(this.monitorTracker, config.getPreferredMonitor()).orElse(currentMonitor);
        if (preferredMonitor != currentMonitor && SystemUtil.isWindows()) {
            VideoMode videoMode = preferredMonitor.getCurrentVideoMode();
            this.windowedX = window.x = preferredMonitor.getViewportX() + (videoMode.getWidth() - window.width) / 2;
            this.windowedY = window.y = preferredMonitor.getViewportY() + (videoMode.getHeight() - window.height) / 2;
            GLFW.glfwSetWindowMonitor(handle, 0, window.x, window.y, window.width, window.height, -1);
        }

        // Honestly, this method should have been an `@Inject`, however
        // unpatched mixins don't allow injecting into constructors at all.
        // Thanks to LlamaLad7 for saving the day!
        glfwMakeContextCurrent.call(handle);
    }

    @Inject(method = "updateWindowRegion", at = @At("HEAD"), cancellable = true)
    private void enableFullscreen(CallbackInfo ci) {
        Window window = (Window)(Object)this;
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();

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

        Monitor monitor = this.monitorTracker.getMonitor(window);
        if (monitor == null) {
            // We couldn't detect a monitor to attach this window to.
            // Let the original method deal with this problem.
            this.currentFullscreenType = null;
            return;
        }

        if (this.currentFullscreenType == FullscreenTypes.DEFAULT) {
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
            ResizableGameRenderer.getInstance().disable();
        }

        VideoMode videoMode = monitor.findClosestVideoMode(this.fullscreenVideoMode);
        this.currentFullscreenType.enable(window, monitor, videoMode);

        // If the current fullscreen type could not switch
        // the video mode, fall back to software scaling.
        int targetWidth = videoMode.getWidth();
        int targetHeight = videoMode.getHeight();
        int deltaWidth = Math.abs(window.width - targetWidth);
        int deltaHeight = Math.abs(window.height - targetHeight);
        if (deltaWidth > 1 || deltaHeight > 1) {
            float targetScale = Math.min((float)targetWidth / window.width, (float)targetHeight / window.height);
            int scaledWidth = Math.round(window.width * targetScale);
            int scaledHeight = Math.round(window.height * targetScale);
            ResizableGameRenderer.getInstance().resize(scaledWidth, scaledHeight);
        }

        ci.cancel();
    }

    @Inject(method = "updateWindowRegion", at = {
        @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowMonitor(JJIIIII)V", ordinal = 0),
        @At(value = "FIELD", target = "Lnet/minecraft/client/util/Window;windowedX:I", ordinal = 1),
    })
    private void disableFullscreen(CallbackInfo ci) {
        if (this.previousFullscreenType == null) {
            return;
        }

        this.previousFullscreenType.disable((Window)(Object)this);
        ResizableGameRenderer.getInstance().disable();
    }

    @WrapOperation(method = "updateWindowRegion", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwGetWindowMonitor(J)J", ordinal = 0))
    private long getWindowMonitorIfWindowed(long handle, Operation<Long> getWindowMonitor) {
        if (this.previousFullscreenType != null) {
            return -1;
        }

        return getWindowMonitor.call(handle);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void save(CallbackInfo ci) {
        Window window = (Window)(Object)this;
        Monitor monitor = this.monitorTracker.getMonitor(window);

        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        config.setFullscreenMode(this.getFullscreenMode());
        config.setPreferredFullscreenMode(this.borderless ? FullscreenMode.BORDERLESS : FullscreenMode.ON);
        config.setPreferredMonitor(monitor == null ? MonitorInfo.primary() : MonitorInfo.of(monitor));
        config.save();

        GameOptions options = MinecraftClient.getInstance().options;
        options.getFullscreen().setValue(this.fullscreen);
        options.write();
    }
}
