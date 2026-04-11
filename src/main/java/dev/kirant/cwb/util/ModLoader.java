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

    static {
        String branding = net.minecraft.client.ClientBrandRetriever.getClientModName();
        if (branding.contains("fabric")) {
            INSTANCE = new FabricModLoader();
        } else {
            INSTANCE = new ModLoader() { };
        }
    }

    private ModLoader() { }
}
