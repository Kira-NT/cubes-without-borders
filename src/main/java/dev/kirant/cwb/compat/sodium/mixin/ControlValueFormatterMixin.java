//? if sodium && sodium: <0.8.0 {
/*package dev.kirant.cwb.compat.sodium.mixin;

//? if sodium: >=0.6.0 {
import com.mojang.blaze3d.platform.*;
import dev.kirant.cwb.util.*;
import net.caffeinemc.mods.sodium.client.gui.options.control.*;
import net.minecraft.network.chat.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value = ControlValueFormatter.class, remap = false)
interface ControlValueFormatterMixin {
    @Inject(method = "lambda$resolution$1", at = @At("RETURN"), cancellable = true)
    private static void getResolution(int v, CallbackInfoReturnable<Component> cir) {
        if (OS.isWindows()) {
            return;
        }

        Monitor monitor = MinecraftWindow.getInstance().findBestMonitor();
        Component description = monitor == null
            ? Components.translatable("options.fullscreen.unavailable") : v <= 0
            ? Components.translatable("options.fullscreen.current")
            : Components.literal(monitor.getMode(v - 1).toString().replace(" (24bit)", ""));

        cir.setReturnValue(description);
    }
}
//?} else {
/^@org.spongepowered.asm.mixin.Mixin(value = net.caffeinemc.mods.sodium.client.gui.SodiumGameOptionPages.class, remap = false)
abstract class ControlValueFormatterMixin { }
^///?}
*///?}
