//? if modmenu: >0.0.0 {
package dev.kirant.cwb.compat.modmenu;

import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.kirant.cwb.CWB;

public final class ModMenuScreenProvider implements ModMenuApi {
    @Override
    //? if modmenu: <1.10.0 {
    /*public String getModId() {
        return CWB.MOD_ID;
    }

    @Override
    public java.util.function.Function<Screen, ? extends Screen> getConfigScreenFactory() {
    *///?} else
    public com.terraformersmc.modmenu.api.ConfigScreenFactory<?> getModConfigScreenFactory() {
        return CWB::createConfigScreen;
    }
}
//?}
