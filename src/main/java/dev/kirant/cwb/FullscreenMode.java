package dev.kirant.cwb;

public enum FullscreenMode {
    OFF(CWB.MOD_ID + ".fullscreenMode.off"),
    EXCLUSIVE(CWB.MOD_ID + ".fullscreenMode.exclusive"),
    BORDERLESS(CWB.MOD_ID + ".fullscreenMode.borderless"),
    WINDOWED(CWB.MOD_ID + ".fullscreenMode.windowed");

    private final String translationKey;

    FullscreenMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return this.translationKey;
    }

    public static FullscreenMode valueOf(int ordinal) {
        switch (ordinal & 3) {
            case 1:
                return FullscreenMode.EXCLUSIVE;

            case 2:
                return FullscreenMode.BORDERLESS;

            case 3:
                return FullscreenMode.WINDOWED;

            default:
                return FullscreenMode.OFF;
        }
    }
}
