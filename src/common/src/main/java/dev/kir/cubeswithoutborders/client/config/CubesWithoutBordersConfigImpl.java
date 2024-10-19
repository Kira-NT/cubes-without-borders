package dev.kir.cubeswithoutborders.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.Expose;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.kir.cubeswithoutborders.client.*;
import dev.kir.cubeswithoutborders.client.util.ModLoaderUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.io.*;

@Environment(EnvType.CLIENT)
class CubesWithoutBordersConfigImpl implements CubesWithoutBordersConfig {
    public static final CubesWithoutBordersConfigImpl INSTANCE;

    private static final Gson GSON;


    private String filePath;

    @Expose
    private FullscreenMode fullscreenMode;

    @Expose
    private FullscreenMode preferredFullscreenMode;

    @Expose
    private FullscreenType fullscreenType;

    @Expose
    private FullscreenType borderlessFullscreenType;

    @Expose
    private MonitorInfo preferredMonitor;


    public CubesWithoutBordersConfigImpl() {
        this.fullscreenMode = FullscreenMode.OFF;
        this.preferredFullscreenMode = FullscreenMode.BORDERLESS;
        this.fullscreenType = FullscreenTypes.exclusive();
        this.borderlessFullscreenType = FullscreenTypes.borderless();
        this.preferredMonitor = MonitorInfo.primary();
    }

    public static CubesWithoutBordersConfigImpl loadNamed(String name) {
        String fileName = name + ".json";
        String filePath = ModLoaderUtil.getConfigFolder().resolve(fileName).toString();
        return CubesWithoutBordersConfigImpl.load(filePath);
    }

    public static CubesWithoutBordersConfigImpl load(String filePath) {
        CubesWithoutBordersConfigImpl config;
        try (Reader reader = new FileReader(filePath)) {
            config = GSON.fromJson(reader, CubesWithoutBordersConfigImpl.class);
        } catch (Throwable e) {
            config = new CubesWithoutBordersConfigImpl();
        }
        config.filePath = filePath;
        return config;
    }


    @Override
    public FullscreenMode getFullscreenMode() {
        return this.fullscreenMode;
    }

    @Override
    public void setFullscreenMode(FullscreenMode fullscreenMode) {
        this.fullscreenMode = fullscreenMode;
    }

    @Override
    public FullscreenMode getPreferredFullscreenMode() {
        return this.preferredFullscreenMode;
    }

    @Override
    public void setPreferredFullscreenMode(FullscreenMode fullscreenMode) {
        this.preferredFullscreenMode = fullscreenMode;
    }

    @Override
    public FullscreenType getFullscreenType() {
        return this.fullscreenType;
    }

    @Override
    public void setFullscreenType(FullscreenType fullscreenType) {
        this.fullscreenType = fullscreenType;
    }

    @Override
    public FullscreenType getBorderlessFullscreenType() {
        return this.borderlessFullscreenType;
    }

    @Override
    public void setBorderlessFullscreenType(FullscreenType fullscreenType) {
        this.borderlessFullscreenType = fullscreenType;
    }

    @Override
    public MonitorInfo getPreferredMonitor() {
        return this.preferredMonitor;
    }

    @Override
    public void setPreferredMonitor(MonitorInfo monitor) {
        this.preferredMonitor = monitor == null ? MonitorInfo.primary() : monitor;
    }

    @Override
    public void save() {
        if (this.filePath == null) {
            return;
        }

        try (Writer writer = new FileWriter(this.filePath)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    static {
        GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(MonitorInfo.class, new TypeAdapter<MonitorInfo>() {
                @Override
                public void write(JsonWriter out, MonitorInfo value) throws IOException {
                    out.value(value.toString());
                }

                @Override
                public MonitorInfo read(JsonReader in) throws IOException {
                    return MonitorInfo.parse(in.nextString()).orElse(MonitorInfo.primary());
                }
            })
            .registerTypeAdapter(FullscreenType.class, new TypeAdapter<FullscreenType>() {
                @Override
                public void write(JsonWriter out, FullscreenType value) throws IOException {
                    out.value(value.id());
                }

                @Override
                public FullscreenType read(JsonReader in) throws IOException {
                    boolean isBorderless = in.getPath().contains("borderless");
                    FullscreenType defaultFullscreenType = isBorderless ? FullscreenTypes.borderless() : FullscreenTypes.exclusive();
                    return FullscreenTypes.get(in.nextString()).orElse(defaultFullscreenType);
                }
            })
            .create();


        INSTANCE = CubesWithoutBordersConfigImpl.loadNamed(
            ModLoaderUtil.getModId()
        );
    }
}
