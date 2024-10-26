package dev.kir.cubeswithoutborders.client.neoforge;

import dev.kir.cubeswithoutborders.client.compat.cloth.ClothConfigScreen;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import dev.kir.cubeswithoutborders.client.util.neoforge.ModLoaderUtilImpl;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@OnlyIn(Dist.CLIENT)
@Mod(ModLoaderUtilImpl.MOD_ID)
public final class CubesWithoutBordersImpl {
    public CubesWithoutBordersImpl() {
        CubesWithoutBordersImpl.registerConfigScreen();
    }

    private static void registerConfigScreen() {
        if (!ModList.get().isLoaded("cloth_config")) {
            return;
        }

        ModLoadingContext.get().registerExtensionPoint(
            IConfigScreenFactory.class,
            () -> (__, parent) -> ClothConfigScreen.create(
                CubesWithoutBordersConfig.getInstance(),
                ModLoaderUtilImpl.MOD_ID.replace("_", "-"),
                parent
            )
        );
    }
}
