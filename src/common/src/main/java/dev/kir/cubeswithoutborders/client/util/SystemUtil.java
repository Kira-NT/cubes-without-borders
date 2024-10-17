package dev.kir.cubeswithoutborders.client.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SystemUtil {
    public static boolean isWindows() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("Windows");
    }

    public static boolean isLinux() {
        String osName = System.getProperty("os.name");
        return (
            osName.startsWith("Linux") ||
            osName.startsWith("FreeBSD") ||
            osName.startsWith("SunOS") ||
            osName.startsWith("Unix")
        );
    }

    public static boolean isMacOS() {
        String osName = System.getProperty("os.name");
        return (
            osName.startsWith("Mac OS X") ||
            osName.startsWith("Darwin")
        );
    }

    private SystemUtil() { }
}
