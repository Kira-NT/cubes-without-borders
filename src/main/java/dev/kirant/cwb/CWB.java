package dev.kirant.cwb;

import dev.kirant.cwb.compat.clothconfig.ClothConfigScreen;
import dev.kirant.cwb.compat.yacl.YetAnotherConfigLibScreen;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public final class CWB {
    public static final String MOD_ID = "cwb";

    public static final CWBConfig CONFIG = CWBConfig.loadById(CWB.MOD_ID);

    public static @Nullable Screen createConfigScreen(Screen parentScreen) {
        Screen screen = YetAnotherConfigLibScreen.create(CWB.CONFIG, CWB.MOD_ID, parentScreen);
        return screen != null ? screen : ClothConfigScreen.create(CWB.CONFIG, CWB.MOD_ID, parentScreen);
    }

    public static void registerConfigScreen(Consumer<Function<Screen, Screen>> screenConsumer) {
        if (YetAnotherConfigLibScreen.isSupported() || ClothConfigScreen.isSupported()) {
            screenConsumer.accept(CWB::createConfigScreen);
        }
    }

    //? if neoforge {
    @net.neoforged.fml.common.Mod(CWB.MOD_ID)
    @net.neoforged.api.distmarker.OnlyIn(net.neoforged.api.distmarker.Dist.CLIENT)
    public static class NeoForge {
        public NeoForge(net.neoforged.fml.ModContainer mod) {
            CWB.registerConfigScreen(f -> mod.registerExtensionPoint(
                net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                (_, x) -> f.apply(x)
            ));
        }
    }
    //?}

    //? if forge {
    @net.minecraftforge.fml.common.Mod(CWB.MOD_ID)
    @net.minecraftforge.api.distmarker.OnlyIn(net.minecraftforge.api.distmarker.Dist.CLIENT)
    public static class Forge {
        @SuppressWarnings("removal")
        public Forge() {
            CWB.registerConfigScreen(f -> net.minecraftforge.fml.ModLoadingContext.get().registerExtensionPoint(
                net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory((_, x) -> f.apply(x))
            ));
        }
    }
    //?}

    private CWB() { }
}
