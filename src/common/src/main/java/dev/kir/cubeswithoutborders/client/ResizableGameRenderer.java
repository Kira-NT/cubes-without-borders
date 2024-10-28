package dev.kir.cubeswithoutborders.client;

import dev.kir.cubeswithoutborders.client.util.FramebufferUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.WindowFramebuffer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.Window;

@Environment(EnvType.CLIENT)
public final class ResizableGameRenderer {
    private static final ResizableGameRenderer INSTANCE;

    private final MinecraftClient client;

    private Framebuffer framebuffer;

    private Framebuffer clientFramebuffer;

    private int framebufferWidth;

    private int framebufferHeight;

    private int windowFramebufferWidth;

    private int windowFramebufferHeight;

    private ResizableGameRenderer(MinecraftClient client) {
        this.client = client;
        this.framebuffer = null;
        this.clientFramebuffer = null;
        this.framebufferWidth = -1;
        this.framebufferHeight = -1;
    }

    public static ResizableGameRenderer getInstance() {
        return ResizableGameRenderer.INSTANCE;
    }

    public boolean isEnabled() {
        return this.framebufferWidth > 0 && this.framebufferHeight > 0;
    }

    public boolean isRendering() {
        return this.clientFramebuffer != null;
    }

    public void resize(int width, int height) {
        this.framebufferWidth = width;
        this.framebufferHeight = height;
        this.reload();
    }

    public void reload() {
        if (!this.isEnabled()) {
            return;
        }

        FramebufferUtil.resize(this.framebuffer, this.framebufferWidth, this.framebufferHeight);
        this.resizeWorldRendererFramebuffers();
    }

    public void disable() {
        this.framebufferWidth = -1;
        this.framebufferHeight = -1;

        Window window = this.client.getWindow();
        if (window != null && this.windowFramebufferWidth > 0 && this.windowFramebufferHeight > 0) {
            window.framebufferWidth = this.windowFramebufferWidth;
            window.framebufferHeight = this.windowFramebufferHeight;
        }
        this.windowFramebufferWidth = -1;
        this.windowFramebufferHeight = -1;

        if (this.clientFramebuffer != null) {
            this.client.framebuffer = this.clientFramebuffer;
            this.client.getFramebuffer().beginWrite(true);
        }
        this.clientFramebuffer = null;

        if (this.framebuffer != null) {
            this.framebuffer.delete();
            this.framebuffer = null;
        }
    }

    public void beginRender() {
        int width = this.framebufferWidth;
        int height = this.framebufferHeight;
        Window window = this.client.getWindow();
        if (!this.isEnabled() || window == null) {
            return;
        }

        if (this.framebuffer == null) {
            this.framebuffer = new WindowFramebuffer(width, height);

            // We need to manually trigger `initFbo` for
            // `FramebufferMixin` to do its thing.
            this.framebuffer.resize(width, height);
            this.resizeWorldRendererFramebuffers();
        }

        this.windowFramebufferWidth = window.framebufferWidth;
        this.windowFramebufferHeight = window.framebufferHeight;
        window.framebufferWidth = width;
        window.framebufferHeight = height;

        this.clientFramebuffer = this.client.getFramebuffer();
        this.client.framebuffer = this.framebuffer;
        this.framebuffer.beginWrite(true);
    }

    public void endRender() {
        Window window = this.client.getWindow();
        if (!this.isEnabled() || window == null) {
            return;
        }

        if (this.framebuffer == null || this.clientFramebuffer == null) {
            return;
        }

        window.framebufferWidth = this.windowFramebufferWidth;
        window.framebufferHeight = this.windowFramebufferHeight;
        this.windowFramebufferWidth = -1;
        this.windowFramebufferHeight = -1;

        this.client.framebuffer = this.clientFramebuffer;
        this.client.getFramebuffer().beginWrite(true);
        this.framebuffer.draw(window.getFramebufferWidth(), window.getFramebufferHeight());
        this.clientFramebuffer = null;
    }

    private void resizeWorldRendererFramebuffers() {
        int width = this.framebufferWidth;
        int height = this.framebufferHeight;
        Window window = this.client.getWindow();
        WorldRenderer worldRenderer = this.client.worldRenderer;
        if (window == null) {
            return;
        }

        FramebufferUtil.resize(worldRenderer.entityOutlineFramebuffer, width, height);
    }

    static {
        INSTANCE = new ResizableGameRenderer(MinecraftClient.getInstance());
    }
}
