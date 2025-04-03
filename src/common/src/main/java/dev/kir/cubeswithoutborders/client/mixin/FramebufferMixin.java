package dev.kir.cubeswithoutborders.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = Framebuffer.class, priority = 0)
abstract class FramebufferMixin {
    @WrapOperation(method = "*", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/textures/GpuTexture;setTextureFilter(Lcom/mojang/blaze3d/textures/FilterMode;Z)V"))
    private void setTextureFilter(GpuTexture texture, FilterMode filter, boolean useMipmaps, Operation<Void> __) {
        texture.setTextureFilter(filter, FilterMode.LINEAR, useMipmaps);
    }
}
