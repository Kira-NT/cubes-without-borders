package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

@Environment(EnvType.CLIENT)
public final class FramebufferUtil {
    public static void resize(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null || framebuffer.textureWidth == width && framebuffer.textureHeight == height) {
            return;
        }

        framebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
    }

    private FramebufferUtil() { }
}
