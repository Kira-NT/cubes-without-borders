package dev.kir.cubeswithoutborders.client.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.GL12;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(value = Framebuffer.class, priority = 0)
abstract class FramebufferMixin {
    @ModifyArg(method = { "initFbo", "setTexFilter" }, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;_texParameter(III)V"), index = 2)
    private int setTexFilter(int target, int pname, int param) {
        if (pname == GL12.GL_TEXTURE_MAG_FILTER) {
            return GL12.GL_LINEAR;
        }

        if (pname == GL12.GL_TEXTURE_WRAP_S || pname == GL12.GL_TEXTURE_WRAP_T) {
            return GL12.GL_CLAMP_TO_EDGE;
        }

        return param;
    }
}
