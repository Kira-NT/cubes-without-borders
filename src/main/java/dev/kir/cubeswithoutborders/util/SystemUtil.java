package dev.kir.cubeswithoutborders.util;

import java.util.Locale;

public final class SystemUtil {
    public static String getSystemName() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    public static boolean isWindows() {
        return SystemUtil.getSystemName().contains("win");
    }

    private SystemUtil() { }
}
