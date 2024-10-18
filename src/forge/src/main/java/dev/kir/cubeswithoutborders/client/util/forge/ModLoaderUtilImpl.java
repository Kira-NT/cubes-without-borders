package dev.kir.cubeswithoutborders.client.util.forge;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;

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
