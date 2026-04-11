//? if yacl: >0.0.0 {
package dev.kirant.cwb.compat.yacl;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.impl.controller.*;
import dev.kirant.cwb.*;
import dev.kirant.cwb.util.Components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

final class YetAnotherConfigLibScreenImpl {
    public static Screen create(CWBConfig config, String modId, Screen parent) {
        Minecraft client = Minecraft.getInstance();
        return YetAnotherConfigLib.createBuilder()
            .title(Components.translatable("modmenu.nameTranslation." + modId))
            .category(ConfigCategory.createBuilder()
                .name(Components.translatable("modmenu.nameTranslation." + modId))
                .option(Option.<String>createBuilder()
                    .name(Components.translatable(modId + ".options.fullscreenType"))
                    .description(id -> YetAnotherConfigLibScreenImpl.getFullscreenTypeOptionDescription(modId, "fullscreenType", id))
                    .binding(FullscreenTypes.exclusive().getId(), () -> config.getFullscreenType().getId(), x -> FullscreenTypes.get(x).ifPresent(config::setFullscreenType))
                    .controller(x -> new DropdownStringControllerBuilderImpl(x).values(FullscreenTypes.stream().map(FullscreenType::getId).toArray(String[]::new)))
                    .flag(OptionFlag.GAME_RESTART)
                    .build())
                .option(Option.<String>createBuilder()
                    .name(Components.translatable(modId + ".options.borderlessFullscreenType"))
                    .description(id -> YetAnotherConfigLibScreenImpl.getFullscreenTypeOptionDescription(modId, "borderlessFullscreenType", id))
                    .binding(FullscreenTypes.borderless().getId(), () -> config.getBorderlessFullscreenType().getId(), x -> FullscreenTypes.get(x).ifPresent(config::setBorderlessFullscreenType))
                    .controller(x -> new DropdownStringControllerBuilderImpl(x).values(FullscreenTypes.stream().map(FullscreenType::getId).toArray(String[]::new)))
                    .flag(OptionFlag.GAME_RESTART)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Components.translatable(modId + ".options.useScaledFramebuffer"))
                    .description(OptionDescription.of(Components.translatable(modId + ".options.useScaledFramebuffer.tooltip")))
                    .binding(true, config::getUseScaledFramebuffer, config::setUseScaledFramebuffer)
                    .controller(TickBoxControllerBuilderImpl::new)
                    .flag(OptionFlag.GAME_RESTART)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Components.translatable(modId + ".options.pauseOnLostFocus"))
                    .description(OptionDescription.of(Components.translatable(modId + ".options.pauseOnLostFocus.tooltip")))
                    .binding(true, () -> client.options.pauseOnLostFocus, x -> client.options.pauseOnLostFocus = x)
                    .controller(TickBoxControllerBuilderImpl::new)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Components.translatable(modId + ".options.pauseOnLostFocusDuringMultiplayer"))
                    .description(OptionDescription.of(Components.translatable(modId + ".options.pauseOnLostFocusDuringMultiplayer.tooltip")))
                    .binding(true, config::getPauseOnLostFocusDuringMultiplayer, config::setPauseOnLostFocusDuringMultiplayer)
                    .controller(TickBoxControllerBuilderImpl::new)
                    .build())
                .build())
            .save(() -> { config.save(); client.options.save(); })
            .build().generateScreen(parent);
    }

    private static OptionDescription getFullscreenTypeOptionDescription(String modId, String optionId, String fullscreenTypeId) {
        Component emptyLine = Components.literal("");
        return OptionDescription.createBuilder().text(
            Components.translatable(modId + ".options." + optionId + ".tooltip"),
            emptyLine,
            emptyLine,
            Components.literal("§l" + fullscreenTypeId),
            emptyLine,
            Components.translatable(modId + ".fullscreenTypes." + fullscreenTypeId + ".tooltip")
        ).build();
    }

    private YetAnotherConfigLibScreenImpl() { }
}
//?}
