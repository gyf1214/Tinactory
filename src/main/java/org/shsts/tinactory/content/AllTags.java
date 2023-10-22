package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllTags {
    public static final ResourceLocation TOOL = ModelGen.modLoc("tool");
    public static final ResourceLocation TOOL_WRENCH = ModelGen.modLoc("tool/wrench");
    public static final ResourceLocation TOOL_CUTTER = ModelGen.modLoc("tool/cutter");
    public static final ResourceLocation TOOL_SAW = ModelGen.modLoc("tool/saw");

    public static TagKey<Item> item(ResourceLocation loc) {
        return TagKey.create(Registry.ITEM_REGISTRY, loc);
    }
}
