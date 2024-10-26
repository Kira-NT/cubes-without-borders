package dev.kir.cubeswithoutborders.client.util.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public final class ModLoaderUtilImpl {
    public static final String MOD_ID = "cubes_without_borders";

    public static String getModId() {
        return ModLoaderUtilImpl.MOD_ID;
    }

    public static Path getConfigFolder() {
        return FMLPaths.CONFIGDIR.get();
    }

    private ModLoaderUtilImpl() { }
}
