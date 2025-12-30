package dev.kir.cubeswithoutborders.client.mixin;

import com.mojang.blaze3d.textures.FilterMode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(value = Framebuffer.class, priority = 0)
abstract class FramebufferMixin {
    @ModifyArg(method = "drawBlit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/SamplerCache;get(Lcom/mojang/blaze3d/textures/FilterMode;)Lnet/minecraft/client/gl/GpuSampler;"))
    private FilterMode setTextureFilter(FilterMode filterMode) {
        return FilterMode.LINEAR;
    }
}
