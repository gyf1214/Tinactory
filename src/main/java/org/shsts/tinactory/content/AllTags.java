package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllTags {
    public static final TagKey<Item> TOOL = modItem("tool");
    public static final TagKey<Item> TOOL_HAMMER = extend(TOOL, "hammer");
    public static final TagKey<Item> TOOL_MORTAR = extend(TOOL, "mortar");
    public static final TagKey<Item> TOOL_FILE = extend(TOOL, "file");
    public static final TagKey<Item> TOOL_SAW = extend(TOOL, "saw");
    public static final TagKey<Item> TOOL_SCREWDRIVER = extend(TOOL, "screwdriver");
    public static final TagKey<Item> TOOL_WRENCH = extend(TOOL, "wrench");
    public static final TagKey<Item> TOOL_WIRE_CUTTER = extend(TOOL, "wire_cutter");

    public static final TagKey<Item> TOOL_HANDLE = modItem("tool_handle");
    public static final TagKey<Item> TOOL_SCREW = modItem("tool_screw");

    public static final TagKey<Block> MINEABLE_WITH_WRENCH = modBlock("mineable/wrench");
    public static final TagKey<Block> MINEABLE_WITH_CUTTER = modBlock("mineable/cutter");

    public static TagKey<Item>
    processingMachine(RecipeTypeEntry<? extends ProcessingRecipe<?>, ?> recipeType) {
        return item(new ResourceLocation(recipeType.modid, "machine/" + recipeType.id));
    }

    public static TagKey<Item> item(ResourceLocation loc) {
        return TagKey.create(Registry.ITEM_REGISTRY, loc);
    }

    public static TagKey<Item> item(String id) {
        return item(new ResourceLocation(id));
    }

    public static TagKey<Item> modItem(String id) {
        return item(ModelGen.modLoc(id));
    }

    public static TagKey<Block> block(ResourceLocation loc) {
        return TagKey.create(Registry.BLOCK_REGISTRY, loc);
    }

    public static TagKey<Block> modBlock(String id) {
        return block(ModelGen.modLoc(id));
    }

    public static <T> TagKey<T> extend(TagKey<T> tag, String suffix) {
        var loc = tag.location();
        var loc1 = new ResourceLocation(loc.getNamespace(), loc.getPath() + "/" + suffix);
        var tag1 = TagKey.create(tag.registry(), loc1);
        REGISTRATE.tag(tag1, tag);
        return tag1;
    }
}
