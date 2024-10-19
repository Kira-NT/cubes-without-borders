package dev.kir.cubeswithoutborders.client.config;

import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.FullscreenType;
import dev.kir.cubeswithoutborders.client.MonitorInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface CubesWithoutBordersConfig {
    static CubesWithoutBordersConfig getInstance() {
        return CubesWithoutBordersConfigImpl.INSTANCE;
    }


    FullscreenMode getFullscreenMode();

    void setFullscreenMode(FullscreenMode fullscreenMode);


    FullscreenMode getPreferredFullscreenMode();

    void setPreferredFullscreenMode(FullscreenMode fullscreenMode);


    FullscreenType getFullscreenType();

    void setFullscreenType(FullscreenType fullscreenType);


    FullscreenType getBorderlessFullscreenType();

    void setBorderlessFullscreenType(FullscreenType fullscreenType);


    MonitorInfo getPreferredMonitor();

    void setPreferredMonitor(MonitorInfo monitor);


    void save();
}
