package dev.kir.cubeswithoutborders.client.util.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

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

    public static boolean isModLoaded(String modId, String minVersion) {
        ArtifactVersion modVersion;
        if (ModList.get() != null) {
            ModContainer mod = ModList.get().getModContainerById(modId).orElse(null);
            modVersion = mod == null ? null : mod.getModInfo().getVersion();
        } else if (LoadingModList.get() != null) {
            ModFileInfo mod = LoadingModList.get().getModFileById(modId);
            modVersion = mod == null ? null : new DefaultArtifactVersion(mod.versionString());
        } else {
            modVersion = null;
        }

        ArtifactVersion version = new DefaultArtifactVersion(minVersion);
        return modVersion != null && modVersion.compareTo(version) >= 0;
    }

    private ModLoaderUtilImpl() { }
}
