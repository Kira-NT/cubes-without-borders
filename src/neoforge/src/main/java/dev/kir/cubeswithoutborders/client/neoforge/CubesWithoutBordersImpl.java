package dev.kir.cubeswithoutborders.client.neoforge;

import dev.kir.cubeswithoutborders.client.compat.cloth.ClothConfigScreen;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import dev.kir.cubeswithoutborders.client.util.neoforge.ModLoaderUtilImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@OnlyIn(Dist.CLIENT)
@Mod(ModLoaderUtilImpl.MOD_ID)
public final class CubesWithoutBordersImpl {
    public CubesWithoutBordersImpl(ModContainer modContainer) {
        CubesWithoutBordersImpl.registerConfigScreen(modContainer);
    }

    private static void registerConfigScreen(ModContainer modContainer) {
        if (!ModList.get().isLoaded("cloth_config")) {
            return;
        }

        modContainer.registerExtensionPoint(
            IConfigScreenFactory.class,
            (__, parent) -> ClothConfigScreen.create(
                CubesWithoutBordersConfig.getInstance(),
                ModLoaderUtilImpl.MOD_ID.replace("_", "-"),
                parent
            )
        );
    }
}
