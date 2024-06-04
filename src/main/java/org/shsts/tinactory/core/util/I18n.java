package org.shsts.tinactory.core.util;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class I18n {
    public static TranslatableComponent tr(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }

    public static TranslatableComponent tr(ResourceLocation id, Object... args) {
        var key = id.getNamespace() + "." + id.getPath().replace('/', '.');
        return tr(key, args);
    }

    public static TranslatableComponent name(Item item) {
        return tr(item.getDescriptionId());
    }

    public static TranslatableComponent name(Block block) {
        return tr(block.getDescriptionId());
    }

    public static TextComponent raw(String format, Object... args) {
        return new TextComponent(format.formatted(args));
    }
}
