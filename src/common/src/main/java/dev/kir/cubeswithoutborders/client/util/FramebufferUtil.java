package dev.kir.cubeswithoutborders.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.BufferManager;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBackend;
import net.minecraft.client.gl.GlCommandEncoder;
import net.minecraft.client.texture.GlTexture;
import org.lwjgl.opengl.GL12;

@Environment(EnvType.CLIENT)
public final class FramebufferUtil {
    public static void beginWrite(Framebuffer framebuffer, boolean setViewport) {
        // NOP
    }

    public static void draw(Framebuffer source, Framebuffer window) {
        GlTexture srcColor = (GlTexture)source.getColorAttachment();
        GlTexture dstColor = (GlTexture)window.getColorAttachment();

        int srcColorAttachment = srcColor.getGlId();
        int srcDepthAttachment = 0;
        int dstColorAttachment = dstColor.getGlId();
        int dstDepthAttachment = 0;

        GlBackend backend = (GlBackend)RenderSystem.getDevice();
        GlCommandEncoder resourceManager = (GlCommandEncoder)backend.createCommandEncoder();
        BufferManager framebufferManager = backend.getBufferManager();
        framebufferManager.setupFramebuffer(resourceManager.temporaryFb1, srcColorAttachment, srcDepthAttachment, 0, 0);
        framebufferManager.setupFramebuffer(resourceManager.temporaryFb2, dstColorAttachment, dstDepthAttachment, 0, 0);
        framebufferManager.setupBlitFramebuffer(resourceManager.temporaryFb1, resourceManager.temporaryFb2, 0, 0, source.textureWidth, source.textureHeight, 0, 0, window.textureWidth, window.textureHeight, GL12.GL_COLOR_BUFFER_BIT, GL12.GL_NEAREST);
    }

    public static void resize(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null || framebuffer.textureWidth == width && framebuffer.textureHeight == height) {
            return;
        }

        framebuffer.resize(width, height);
    }

    private FramebufferUtil() { }
}
