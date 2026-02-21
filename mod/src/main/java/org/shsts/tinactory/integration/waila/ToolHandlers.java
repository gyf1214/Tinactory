package org.shsts.tinactory.integration.waila;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.AllTags;
import snownee.jade.addon.harvest.HarvestToolProvider;

import static org.shsts.tinactory.AllRegistries.ITEMS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ToolHandlers {
    private static void register(String name, TagKey<Block> tag, String id) {
        HarvestToolProvider.registerHandler(new TagToolHandler(name, tag, ITEMS.getEntry(id)));
    }

    static {
        register("wrench", AllTags.MINEABLE_WITH_WRENCH, "tool/wrench/test");
        register("wire_cutter", AllTags.MINEABLE_WITH_WIRE_CUTTER, "tool/wire_cutter/test");
    }

    public static void init() {}
}
