package dev.kir.cubeswithoutborders.client.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.kir.cubeswithoutborders.client.compat.cloth.ClothConfigScreen;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import dev.kir.cubeswithoutborders.client.util.ModLoaderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class ModMenuCompat implements ModMenuApi {
    private static final boolean IS_CLOTH_LOADED = FabricLoader.getInstance().isModLoaded("cloth-config");

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!IS_CLOTH_LOADED) {
            return parent -> null;
        }

        return parent -> ClothConfigScreen.create(
            CubesWithoutBordersConfig.getInstance(),
            ModLoaderUtil.getModId(),
            parent
        );
    }
}
