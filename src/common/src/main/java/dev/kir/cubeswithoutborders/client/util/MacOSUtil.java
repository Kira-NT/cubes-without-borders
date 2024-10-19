package dev.kir.cubeswithoutborders.client.util;

import ca.weblite.objc.Client;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MacOSUtil {
    private static final long NSApplicationPresentationDefault = 0L;
    private static final long NSApplicationPresentationHideDock = 1L << 1;
    private static final long NSApplicationPresentationHideMenuBar = 1L << 3;

    public static void hideGlobalUI() {
        Client.getInstance()
            .sendProxy("NSApplication", "sharedApplication")
            .send("setPresentationOptions:", NSApplicationPresentationHideDock | NSApplicationPresentationHideMenuBar);
    }

    public static void showGlobalUI() {
        Client.getInstance()
            .sendProxy("NSApplication", "sharedApplication")
            .send("setPresentationOptions:", NSApplicationPresentationDefault);
    }

    private MacOSUtil() { }
}
