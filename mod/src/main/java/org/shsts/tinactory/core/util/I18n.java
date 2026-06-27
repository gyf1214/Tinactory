package org.shsts.tinactory.core.util;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class I18n {
    public static Component tr(String key, Object... args) {
        return Component.translatable(key, args);
    }

    public static Component tr(ResourceLocation id, Object... args) {
        var key = id.getNamespace() + "." + id.getPath().replace('/', '.');
        return tr(key, args);
    }

    public static Component name(Item item) {
        return tr(item.getDescriptionId());
    }

    public static Component name(Block block) {
        return tr(block.getDescriptionId());
    }

    public static Component raw(String format, Object... args) {
        return Component.literal(format.formatted(args));
    }
}
