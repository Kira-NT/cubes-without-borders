package dev.kirant.cwb.compat.clothconfig;

import dev.kirant.cwb.CWBConfig;
import dev.kirant.cwb.util.ModLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public final class ClothConfigScreen {
    //? if cloth-config: >0.0.0 {
    // So, apparently, Cloth Config uses different IDs
    // on (Neo)Forge and Fabric (and only sometimes!),
    // and there was a period of time when it's been renamed to
    // cloth-config2, yet this change has been reverted later on?
    // I don't know chief, I've completely lost the plot...
    private static final boolean IS_CLOTH_CONFIG_LOADED = (
        ModLoader.getInstance().isModLoaded("cloth-config") ||
        ModLoader.getInstance().isModLoaded("cloth-config2") ||
        ModLoader.getInstance().isModLoaded("cloth_config") ||
        ModLoader.getInstance().isModLoaded("cloth_config2")
    );

    public static boolean isSupported() {
        return ClothConfigScreen.IS_CLOTH_CONFIG_LOADED;
    }

    public static @Nullable Screen create(CWBConfig config, String modId, Screen parent) {
        return ClothConfigScreen.isSupported() ? ClothConfigScreenImpl.create(config, modId, parent) : null;
    }
    //?} else {
    /*public static boolean isSupported() {
        return false;
    }

    public static @Nullable Screen create(CWBConfig config, String modId, Screen parent) {
        return null;
    }
    *///?}

    private ClothConfigScreen() { }
}
//?}
