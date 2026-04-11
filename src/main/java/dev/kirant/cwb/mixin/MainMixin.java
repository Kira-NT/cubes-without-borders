package dev.kirant.cwb.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kirant.cwb.CWB;
import dev.kirant.cwb.FullscreenMode;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
abstract class MainMixin {
    @ModifyReceiver(method = "main", at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;parse([Ljava/lang/String;)Ljoptsimple/OptionSet;"), require = 0)
    private static OptionParser allowBorderlessOption(OptionParser parser, String[] arguments) {
        parser.accepts("borderless");
        return parser;
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderThread()V", ordinal = 0), require = 0)
    private static void readBorderlessOption(CallbackInfo ci, @Local(ordinal = 0) OptionSet options) {
        if (options.has("borderless")) {
            CWB.CONFIG.setFullscreenMode(FullscreenMode.BORDERLESS);
        }
    }
}
