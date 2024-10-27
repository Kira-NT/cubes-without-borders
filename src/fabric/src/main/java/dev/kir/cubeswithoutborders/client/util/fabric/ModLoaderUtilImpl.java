package dev.kir.cubeswithoutborders.client.util.fabric;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;

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

    public static boolean isModLoaded(String modId, String minVersion) {
        try {
            Version version = Version.parse(minVersion);
            ModContainer mod = FabricLoader.getInstance().getModContainer(modId).orElse(null);
            return mod != null && mod.getMetadata().getVersion().compareTo(version) >= 0;
        } catch (Throwable e) {
            return false;
        }
    }

    private ModLoaderUtilImpl() { }
}
