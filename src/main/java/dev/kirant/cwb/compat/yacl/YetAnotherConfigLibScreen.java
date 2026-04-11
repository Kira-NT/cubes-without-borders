package dev.kirant.cwb.compat.yacl;

import dev.kirant.cwb.CWBConfig;
import dev.kirant.cwb.util.ModLoader;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

public final class YetAnotherConfigLibScreen {
    //? if yacl: >0.0.0 {
    private static final boolean IS_YACL_LOADED = ModLoader.getInstance().isModLoaded("yet_another_config_lib_v3");

    public static boolean isSupported() {
        return YetAnotherConfigLibScreen.IS_YACL_LOADED;
    }

    public static @Nullable Screen create(CWBConfig config, String modId, Screen parent) {
        return YetAnotherConfigLibScreen.isSupported() ? YetAnotherConfigLibScreenImpl.create(config, modId, parent) : null;
    }
    //?} else {
    /*public static boolean isSupported() {
        return false;
    }

    public static @Nullable Screen create(CWBConfig config, String modId, Screen parent) {
        return null;
    }
    *///?}

    private YetAnotherConfigLibScreen() { }
}
