package dev.kirant.cwb.util;

import net.minecraft.network.chat.Component;

public final class Components {
    public static Component translatable(String key) {
        //? if <1.19 {
        /*return new net.minecraft.network.chat.TranslatableComponent(key);
        *///?} else
        return Component.translatable(key);
    }

    public static Component literal(String value) {
        //? if <1.19 {
        /*return new net.minecraft.network.chat.TextComponent(value);
         *///?} else
        return Component.literal(value);
    }

    private Components() { }
}
