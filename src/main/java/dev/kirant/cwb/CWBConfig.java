package dev.kirant.cwb;

import com.google.gson.*;
import com.google.gson.annotations.*;
import dev.kirant.cwb.util.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.function.*;

public class CWBConfig {
    private String filePath;

    private boolean isDirty;

    private @Expose FullscreenMode fullscreenMode;

    private @Expose FullscreenMode preferredFullscreenMode;

    //? if !forge || >=1.16
    @SerializedName(value = "exclusiveFullscreenType", alternate = "fullscreenType")
    private @Expose String exclusiveFullscreenType;

    private @Expose String borderlessFullscreenType;

    private @Expose String windowedFullscreenType;

    private @Expose Boolean useDelayedFullscreen;

    private @Expose Boolean useScaledFramebuffer;

    private @Expose Boolean autoHideDock;

    private @Expose Boolean autoHideMenuBar;

    private @Expose Boolean pauseOnLostFocusDuringMultiplayer;

    private @Expose @Nullable CWBConfig linux;

    private @Expose @Nullable CWBConfig macos;

    private @Expose @Nullable CWBConfig windows;

    private CWBConfig() { }

    public static CWBConfig load(String filePath) {
        CWBConfig config;
        try (Reader reader = new FileReader(filePath)) {
            config = GSON.fromJson(reader, CWBConfig.class);
        } catch (Throwable e) {
            config = null;
        }

        if (config == null) {
            config = new CWBConfig();
            config.setFullscreenMode(config.getFullscreenMode());
            config.setPreferredFullscreenMode(config.getPreferredFullscreenMode());
            config.setExclusiveFullscreenType(config.getExclusiveFullscreenType());
            config.setBorderlessFullscreenType(config.getBorderlessFullscreenType());
            config.setUseDelayedFullscreen(config.getUseDelayedFullscreen());
            config.setUseScaledFramebuffer(config.getUseScaledFramebuffer());
            config.setAutoHideDock(config.getAutoHideDock());
            config.setAutoHideMenuBar(config.getAutoHideMenuBar());
            config.setPauseOnLostFocusDuringMultiplayer(config.getPauseOnLostFocusDuringMultiplayer());
        }

        config.filePath = filePath;
        return config;
    }

    public static CWBConfig loadById(String id) {
        String fileName = id + ".json";
        String filePath = ModLoader.getInstance().getConfigFolder().resolve(fileName).toString();
        return CWBConfig.load(filePath);
    }

    public FullscreenMode getFullscreenMode() {
        return this.getValue(c -> c.fullscreenMode, FullscreenMode.OFF);
    }

    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        this.setValue(c -> c.fullscreenMode, (c, x) -> c.fullscreenMode = x, fullscreenMode);
    }

    public FullscreenMode getPreferredFullscreenMode() {
        return this.getValue(c -> c.preferredFullscreenMode, FullscreenMode.BORDERLESS);
    }

    public void setPreferredFullscreenMode(FullscreenMode fullscreenMode) {
        this.setValue(c -> c.preferredFullscreenMode, (c, x) -> c.preferredFullscreenMode = x, fullscreenMode);
    }

    public FullscreenType getPreferredFullscreenType() {
        return this.getPreferredFullscreenType(this.getPreferredFullscreenMode());
    }

    public FullscreenType getPreferredFullscreenType(FullscreenMode preferredFullscreenMode) {
        return preferredFullscreenMode == FullscreenMode.WINDOWED
            ? this.getWindowedFullscreenType()
            : preferredFullscreenMode == FullscreenMode.EXCLUSIVE
                ? this.getExclusiveFullscreenType()
                : this.getBorderlessFullscreenType();
    }

    public FullscreenType getExclusiveFullscreenType() {
        return this.getValue(c -> c.exclusiveFullscreenType, x -> FullscreenTypes.get(x).orElse(null), FullscreenTypes.exclusive());
    }

    public void setExclusiveFullscreenType(FullscreenType fullscreenType) {
        this.setPlatformAwareValue(c -> c.exclusiveFullscreenType, (c, x) -> c.exclusiveFullscreenType = x, fullscreenType == null ? null : fullscreenType.id());
    }

    public FullscreenType getBorderlessFullscreenType() {
        return this.getValue(c -> c.borderlessFullscreenType, x -> FullscreenTypes.get(x).orElse(null), FullscreenTypes.borderless());
    }

    public void setBorderlessFullscreenType(FullscreenType fullscreenType) {
        this.setPlatformAwareValue(c -> c.borderlessFullscreenType, (c, x) -> c.borderlessFullscreenType = x, fullscreenType == null ? null : fullscreenType.id());
    }

    public FullscreenType getWindowedFullscreenType() {
        return this.getValue(c -> c.windowedFullscreenType, x -> FullscreenTypes.get(x).orElse(null), FullscreenTypes.windowed());
    }

    public void setWindowedFullscreenType(FullscreenType fullscreenType) {
        this.setPlatformAwareValue(c -> c.windowedFullscreenType, (c, x) -> c.windowedFullscreenType = x, fullscreenType == null ? null : fullscreenType.id());
    }

    public boolean getUseDelayedFullscreen() {
        return this.getValue(c -> c.useDelayedFullscreen, true);
    }

    public void setUseDelayedFullscreen(boolean useDelayedFullscreen) {
        this.setValue(c -> c.useDelayedFullscreen, (c, x) -> c.useDelayedFullscreen = x, useDelayedFullscreen);
    }

    public boolean getUseScaledFramebuffer() {
        return this.getValue(c -> c.useScaledFramebuffer, true);
    }

    public void setUseScaledFramebuffer(boolean useScaledFramebuffer) {
        this.setValue(c -> c.useScaledFramebuffer, (c, x) -> c.useScaledFramebuffer = x, useScaledFramebuffer);
    }

    public boolean getAutoHideDock() {
        return this.getValue(c -> c.autoHideDock, false);
    }

    public void setAutoHideDock(boolean autoHideDock) {
        this.setValue(c -> c.autoHideDock, (c, x) -> c.autoHideDock = x, autoHideDock);
    }

    public boolean getAutoHideMenuBar() {
        return this.getValue(c -> c.autoHideMenuBar, false);
    }

    public void setAutoHideMenuBar(boolean autoHideMenuBar) {
        this.setValue(c -> c.autoHideMenuBar, (c, x) -> c.autoHideMenuBar = x, autoHideMenuBar);
    }

    public boolean getPauseOnLostFocusDuringMultiplayer() {
        return this.getValue(c -> c.pauseOnLostFocusDuringMultiplayer, true);
    }

    public void setPauseOnLostFocusDuringMultiplayer(boolean pauseOnLostFocusDuringMultiplayer) {
        this.setValue(c -> c.pauseOnLostFocusDuringMultiplayer, (c, x) -> c.pauseOnLostFocusDuringMultiplayer = x, pauseOnLostFocusDuringMultiplayer);
    }

    public void save() {
        if (!this.isDirty || this.filePath == null) {
            return;
        }

        try (Writer writer = new FileWriter(this.filePath)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.isDirty = false;
    }

    private @Nullable CWBConfig getPlatformAwareConfig(boolean shouldCreate) {
        CWBConfig platformAwareConfig = null;
        if (OS.isWindows()) {
            platformAwareConfig = this.windows = this.windows == null && shouldCreate ? new CWBConfig() : this.windows;
        } else if (OS.isMacOS()) {
            platformAwareConfig = this.macos = this.macos == null && shouldCreate ? new CWBConfig() : this.macos;
        } else if (OS.isUnix()) {
            platformAwareConfig = this.linux = this.linux == null && shouldCreate ? new CWBConfig() : this.linux;
        }
        return platformAwareConfig;
    }

    private <T> T getValue(Function<CWBConfig, T> getter) {
        CWBConfig platformAwareConfig = this.getPlatformAwareConfig(false);
        T value = platformAwareConfig == null ? null : getter.apply(platformAwareConfig);
        return value == null ? getter.apply(this) : value;
    }

    private <T> T getValue(Function<CWBConfig, T> getter, T defaultValue) {
        T value = this.getValue(getter);
        return value == null ? defaultValue : value;
    }

    private <T, U> U getValue(Function<CWBConfig, T> getter, Function<T, U> mapper, U defaultValue) {
        T value = this.getValue(getter);
        U mappedValue = value == null ? defaultValue : mapper.apply(value);
        return mappedValue == null ? defaultValue : mappedValue;
    }

    private <T> void setValue(Function<CWBConfig, T> getter, BiConsumer<CWBConfig, T> setter, T value, boolean isPlatformAware) {
        if (this.getValue(getter) == value) {
            return;
        }

        CWBConfig platformAwareConfig = this.getPlatformAwareConfig(isPlatformAware);
        setter.accept(platformAwareConfig == null || !isPlatformAware && getter.apply(platformAwareConfig) == null ? this : platformAwareConfig, value);
        this.isDirty = true;
    }

    private <T> void setValue(Function<CWBConfig, T> getter, BiConsumer<CWBConfig, T> setter, T value) {
        this.setValue(getter, setter, value, false);
    }

    private <T> void setPlatformAwareValue(Function<CWBConfig, T> getter, BiConsumer<CWBConfig, T> setter, T value) {
        this.setValue(getter, setter, value, true);
    }

    @SuppressWarnings("deprecation")
    private static final Gson GSON = new GsonBuilder()
        //? if !forge || >=1.16
        .setLenient()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .create();
}
