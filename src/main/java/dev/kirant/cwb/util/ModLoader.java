package dev.kirant.cwb.util;

import java.nio.file.Path;

public abstract class ModLoader {
    private static final ModLoader INSTANCE;

    public static ModLoader getInstance() {
        return ModLoader.INSTANCE;
    }

    public Path getConfigFolder() {
        throw new UnsupportedOperationException();
    }

    public boolean isModLoaded(String modId) {
        throw new UnsupportedOperationException();
    }

    public boolean isModLoaded(String modId, String minVersion) {
        throw new UnsupportedOperationException();
    }

    private static class FabricModLoader extends ModLoader {
        //? if fabric {
        @Override
        public Path getConfigFolder() {
            return net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().toAbsolutePath().normalize();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded(modId);
        }

        @Override
        public boolean isModLoaded(String modId, String minVersion) {
            try {
                net.fabricmc.loader.api.ModContainer mod = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(modId).orElse(null);
                return mod != null && mod.getMetadata().getVersion().compareTo(net.fabricmc.loader.api.Version.parse(minVersion)) >= 0;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        //?}
    }

    private static class NeoForgeModLoader extends ModLoader {
        //? if neoforge {
        @Override
        public Path getConfigFolder() {
            return net.neoforged.fml.loading.FMLPaths.CONFIGDIR.get();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return net.neoforged.fml.loading.FMLLoader.getCurrent().getLoadingModList().getModFileById(modId) != null;
        }

        @Override
        public boolean isModLoaded(String modId, String minVersion) {
            net.neoforged.fml.loading.moddiscovery.ModFileInfo mod = net.neoforged.fml.loading.FMLLoader.getCurrent().getLoadingModList().getModFileById(modId);
            org.apache.maven.artifact.versioning.DefaultArtifactVersion modVersion = mod == null ? null : new org.apache.maven.artifact.versioning.DefaultArtifactVersion(mod.versionString());
            org.apache.maven.artifact.versioning.DefaultArtifactVersion targetVersion = new org.apache.maven.artifact.versioning.DefaultArtifactVersion(minVersion);
            return modVersion != null && modVersion.compareTo(targetVersion) >= 0;
        }
        //?}
    }

    private static class ForgeModLoader extends ModLoader {
        //? if forge {
        @Override
        public Path getConfigFolder() {
            return net.minecraftforge.fml.loading.FMLPaths.CONFIGDIR.get();
        }

        @Override
        public boolean isModLoaded(String modId) {
            return net.minecraftforge.fml.ModList.getModFileById(modId) != null;
        }

        @Override
        public boolean isModLoaded(String modId, String minVersion) {
            net.minecraftforge.forgespi.language.IModFileInfo mod = net.minecraftforge.fml.ModList.getModFileById(modId);
            org.apache.maven.artifact.versioning.DefaultArtifactVersion modVersion = mod == null ? null : new org.apache.maven.artifact.versioning.DefaultArtifactVersion(mod.versionString());
            org.apache.maven.artifact.versioning.DefaultArtifactVersion targetVersion = new org.apache.maven.artifact.versioning.DefaultArtifactVersion(minVersion);
            return modVersion != null && modVersion.compareTo(targetVersion) >= 0;
        }
        //?}
    }

    static {
        String branding = net.minecraft.client.ClientBrandRetriever.getClientModName();
        if (branding.contains("fabric")) {
            INSTANCE = new FabricModLoader();
        } else if (branding.contains("neoforge")) {
            INSTANCE = new NeoForgeModLoader();
        } else if (branding.contains("forge")) {
            INSTANCE = new ForgeModLoader();
        } else {
            INSTANCE = new ModLoader() { };
        }
    }

    private ModLoader() { }
}
