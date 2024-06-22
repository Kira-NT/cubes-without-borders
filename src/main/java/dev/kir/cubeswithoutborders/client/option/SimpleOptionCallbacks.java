package dev.kir.cubeswithoutborders.client.option;

import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public final class SimpleOptionCallbacks<T> implements SimpleOption.Callbacks<T> {
    private final Codec<T> codec;

    private final Function<T, Optional<T>> validate;

    public SimpleOptionCallbacks(Codec<T> codec) {
        this(codec, Optional::ofNullable);
    }

    public SimpleOptionCallbacks(Codec<T> codec, Function<T, Optional<T>> validate) {
        this.codec = codec;
        this.validate = validate;
    }

    @Override
    public Function<SimpleOption<T>, ClickableWidget> getButtonCreator(SimpleOption.TooltipFactory<T> tooltipFactory, GameOptions gameOptions, int x, int y, int width) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<T> validate(T value) {
        return this.validate.apply(value);
    }

    @Override
    public Codec<T> codec() {
        return this.codec;
    }
}
