//? if modmenu {
package dev.kirant.cwb.compat.modmenu;

import com.terraformersmc.modmenu.api.*;
import dev.kirant.cwb.CWB;
import net.minecraft.client.gui.screens.Screen;

public final class ModMenuScreenProvider implements ModMenuApi {
    @Override
    //? if modmenu: <1.10.0 {
    /*public String getModId() {
        return CWB.MOD_ID;
    }

    @Override
    public java.util.function.Function<Screen, ? extends Screen> getConfigScreenFactory() {
    *///?} else
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CWB::createConfigScreen;
    }
}
//?}
