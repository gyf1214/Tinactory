package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class I18n {
    public static Component tr(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    public static Component raw(String format, Object... args) {
        return new TextComponent(format.formatted(args));
    }
}
