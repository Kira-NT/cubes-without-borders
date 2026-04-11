package dev.kirant.cwb;

import com.google.gson.*;
import com.google.gson.annotations.*;
import com.google.gson.stream.*;
import dev.kirant.cwb.util.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.function.*;

public class CWBConfig {
    private String filePath;

    private boolean isDirty;

    private @Expose FullscreenMode fullscreenMode;

    private @Expose FullscreenMode preferredFullscreenMode;

    private @Expose FullscreenType fullscreenType;

    private @Expose FullscreenType borderlessFullscreenType;

    private @Expose Boolean useScaledFramebuffer;

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
            config.isDirty = true;
        }

        config.validate();
        config.filePath = filePath;
        return config;
    }

    public static CWBConfig loadById(String id) {
        String fileName = id + ".json";
        String filePath = ModLoader.getInstance().getConfigFolder().resolve(fileName).toString();
        return CWBConfig.load(filePath);
    }

    public FullscreenMode getFullscreenMode() {
        return this.getValue(c -> c.fullscreenMode);
    }

    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        this.setValue(c -> c.fullscreenMode, (c, x) -> c.fullscreenMode = x, fullscreenMode);
    }

    public FullscreenMode getPreferredFullscreenMode() {
        return this.getValue(c -> c.preferredFullscreenMode);
    }

    public void setPreferredFullscreenMode(FullscreenMode fullscreenMode) {
        this.setValue(c -> c.preferredFullscreenMode, (c, x) -> c.preferredFullscreenMode = x, fullscreenMode);
    }

    public FullscreenType getFullscreenType() {
        return this.getValue(c -> c.fullscreenType);
    }

    public void setFullscreenType(FullscreenType fullscreenType) {
        this.setValue(c -> c.fullscreenType, (c, x) -> c.fullscreenType = x, fullscreenType);
    }

    public FullscreenType getBorderlessFullscreenType() {
        return this.getValue(c -> c.borderlessFullscreenType);
    }

    public void setBorderlessFullscreenType(FullscreenType fullscreenType) {
        this.setValue(c -> c.borderlessFullscreenType, (c, x) -> c.borderlessFullscreenType = x, fullscreenType);
    }

    public boolean getUseScaledFramebuffer() {
        return this.getValue(c -> c.useScaledFramebuffer);
    }

    public void setUseScaledFramebuffer(boolean useScaledFramebuffer) {
        this.setValue(c -> c.useScaledFramebuffer, (c, x) -> c.useScaledFramebuffer = x, useScaledFramebuffer);
    }

    public boolean getPauseOnLostFocusDuringMultiplayer() {
        return this.getValue(c -> c.pauseOnLostFocusDuringMultiplayer);
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

    private @Nullable CWBConfig getPlatformAwareConfig(boolean isReadOnly) {
        boolean shouldCreate = !isReadOnly & (this.linux != null | this.macos != null | this.windows != null);
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
        CWBConfig platformAwareConfig = this.getPlatformAwareConfig(true);
        T value = platformAwareConfig == null ? null : getter.apply(platformAwareConfig);
        return value == null ? getter.apply(this) : value;
    }

    private <T> void setValue(Function<CWBConfig, T> getter, BiConsumer<CWBConfig, T> setter, T value) {
        if (this.getValue(getter) == value) {
            return;
        }

        CWBConfig platformAwareConfig = this.getPlatformAwareConfig(false);
        setter.accept(platformAwareConfig == null ? this : platformAwareConfig, value);

        this.validate();
        this.isDirty = true;
    }

    private void validate() {
        this.fullscreenMode = this.fullscreenMode != null ? this.fullscreenMode : FullscreenMode.OFF;
        this.preferredFullscreenMode = this.preferredFullscreenMode != null ? this.preferredFullscreenMode : FullscreenMode.BORDERLESS;
        this.fullscreenType = this.fullscreenType != null ? this.fullscreenType : FullscreenTypes.exclusive();
        this.borderlessFullscreenType = this.borderlessFullscreenType != null ? this.borderlessFullscreenType : FullscreenTypes.borderless();
        this.useScaledFramebuffer = this.useScaledFramebuffer != null ? this.useScaledFramebuffer : true;
        this.pauseOnLostFocusDuringMultiplayer = this.pauseOnLostFocusDuringMultiplayer != null ? this.pauseOnLostFocusDuringMultiplayer : true;
    }

    @SuppressWarnings("deprecation")
    private static final Gson GSON = new GsonBuilder()
        .setLenient()
        .setPrettyPrinting()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(FullscreenType.class, new TypeAdapter<FullscreenType>() {
            @Override
            public void write(JsonWriter out, FullscreenType value) throws IOException {
                out.value(value == null ? null : value.getId());
            }

            @Override
            public FullscreenType read(JsonReader in) throws IOException {
                return FullscreenTypes.get(in.nextString()).orElse(null);
            }
        })
        .create();
}
