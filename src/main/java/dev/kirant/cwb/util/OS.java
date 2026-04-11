package dev.kirant.cwb.util;

import java.io.File;

public final class OS {
    public static boolean isWindows() {
        return File.separatorChar == '\\';
    }

    public static boolean isUnix() {
        return File.separatorChar == '/';
    }

    public static boolean isMacOS() {
        return OS.isUnix() && System.getProperty("os.name").contains("Mac");
    }

    private OS() { }
}
