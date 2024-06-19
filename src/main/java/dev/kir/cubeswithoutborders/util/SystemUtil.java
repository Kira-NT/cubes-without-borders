package dev.kir.cubeswithoutborders.util;

public final class SystemUtil {
    public static String getSystemName() {
        return System.getProperty("os.name");
    }

    public static boolean isWindows() {
        return SystemUtil.getSystemName().contains("Windows");
    }

    private SystemUtil() { }
}
