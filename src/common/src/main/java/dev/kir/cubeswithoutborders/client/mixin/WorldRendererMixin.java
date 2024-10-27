package dev.kir.cubeswithoutborders.client.mixin;

import dev.kir.cubeswithoutborders.client.ResizableGameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(WorldRenderer.class)
abstract class WorldRendererMixin {
    @Inject(method = { "loadEntityOutlinePostProcessor", "onResized" }, at = @At("RETURN"))
    private void reloadResources(CallbackInfo ci) {
        ResizableGameRenderer.getInstance().reload();
    }
}
