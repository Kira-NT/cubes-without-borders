package dev.kir.cubeswithoutborders.client.mixin;

import dev.kir.cubeswithoutborders.client.ResizableGameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void beginRender(CallbackInfo ci) {
        ResizableGameRenderer.getInstance().beginRender();
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void endRender(CallbackInfo ci) {
        ResizableGameRenderer.getInstance().endRender();
    }
}
