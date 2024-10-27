package dev.kir.cubeswithoutborders.client.compat.sodium.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.caffeinemc.mods.sodium.client.compatibility.environment.OsUtils;
import net.caffeinemc.mods.sodium.client.gui.options.control.ControlValueFormatter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ControlValueFormatter.class, remap = false)
interface ControlValueFormatterMixin {
    @WrapOperation(method = "lambda$resolution$1", at = @At(value = "INVOKE", target = "Lnet/caffeinemc/mods/sodium/client/compatibility/environment/OsUtils;getOs()Lnet/caffeinemc/mods/sodium/client/compatibility/environment/OsUtils$OperatingSystem;", ordinal = 0), remap = false)
    private static OsUtils.OperatingSystem getOs(Operation<OsUtils.OperatingSystem> _getOs) {
        // Sodium only allows resolution changes on Windows because GLFW
        // does not support Wayland (Linux), and there seem to be some
        // problems with macOS as well. However, we provide a custom
        // software-based scaling solution that works across all platforms.
        // Therefore, let's just lie to Sodium to get the option back on track.
        return OsUtils.OperatingSystem.WIN;
    }
}
