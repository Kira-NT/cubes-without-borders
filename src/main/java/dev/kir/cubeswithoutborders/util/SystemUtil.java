package dev.kir.cubeswithoutborders.util;

public final class SystemUtil {
    public static String getSystemName() {
        return System.getProperty("os.name");
    }

    public static boolean isWindows() {
        return SystemUtil.getSystemName().contains("Windows");
    }

    public static boolean supportsWindowedFullscreen() {
        return SystemUtil.isWindows();
    }

    private SystemUtil() { }
}
