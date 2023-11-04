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
    public static final TagKey<Item> TOOL = modItem("tool");
    public static final TagKey<Item> TOOL_WRENCH = extend(TOOL, "wrench");
    public static final TagKey<Item> TOOL_CUTTER = extend(TOOL, "cutter");
    public static final TagKey<Item> TOOL_SAW = extend(TOOL, "saw");
    public static final TagKey<Item> TOOL_HAMMER = extend(TOOL, "hammer");

    public static TagKey<Item> item(ResourceLocation loc) {
        return TagKey.create(Registry.ITEM_REGISTRY, loc);
    }

    public static TagKey<Item> item(String id) {
        return item(new ResourceLocation(id));
    }

    public static TagKey<Item> modItem(String id) {
        return item(ModelGen.modLoc(id));
    }

    public static <T> TagKey<T> extend(TagKey<T> tag, String suffix) {
        var loc = tag.location();
        var loc1 = new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + suffix);
        return TagKey.create(tag.registry(), loc1);
    }
}
