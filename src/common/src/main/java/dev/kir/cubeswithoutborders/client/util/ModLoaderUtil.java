package dev.kir.cubeswithoutborders.client.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public final class ModLoaderUtil {
    @ExpectPlatform
    public static String getModId() {
        throw new NoSuchMethodError();
    }

    @ExpectPlatform
    public static Path getConfigFolder() {
        throw new NoSuchMethodError();
    }

    public static boolean isModLoaded(String modId) {
        return ModLoaderUtil.isModLoaded(modId, "0.0.0");
    }

    @ExpectPlatform
    public static boolean isModLoaded(String modId, String minVersion) {
        throw new NoSuchMethodError();
    }

    private ModLoaderUtil() { }
}
