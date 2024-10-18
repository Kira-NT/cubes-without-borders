package dev.kir.cubeswithoutborders.client.util.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

@Environment(EnvType.CLIENT)
public final class ModLoaderUtilImpl {
    public static final String MOD_ID = "cubes-without-borders";

    public static String getModId() {
        return MOD_ID;
    }

    public static Path getConfigFolder() {
        return FabricLoader.getInstance()
            .getConfigDir()
            .toAbsolutePath()
            .normalize();
    }

    private ModLoaderUtilImpl() { }
}
