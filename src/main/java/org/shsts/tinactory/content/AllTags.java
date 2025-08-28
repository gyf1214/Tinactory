package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.electric.CircuitComponentTier;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

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

    public static final TagKey<Item> TOOL_SHEARS = modItem("tool_shears");
    public static final TagKey<Item> TOOL_HANDLE = modItem("tool_handle");
    public static final TagKey<Item> TOOL_SCREW = modItem("tool_screw");

    public static final TagKey<Block> MINEABLE_WITH_WRENCH = modBlock("mineable/wrench");
    public static final TagKey<Block> MINEABLE_WITH_WIRE_CUTTER = modBlock("mineable/wire_cutter");

    public static final TagKey<Item> MACHINE = modItem("machine");
    public static final TagKey<Item> ELECTRIC_FURNACE = extend(MACHINE, "electric_furnace");
    public static final TagKey<Block> COIL = modBlock("multiblock/coil");
    public static final TagKey<Block> CLEANROOM_WALL = modBlock("multiblock/cleanroom_wall");
    public static final TagKey<Block> CLEANROOM_CONNECTOR = modBlock("multiblock/cleanroom_connector");
    public static final TagKey<Block> CLEANROOM_DOOR = modBlock("multiblock/cleanroom_door");

    public static final TagKey<Item> STORAGE_CELL = modItem("storage_cell");
    public static final TagKey<Item> ITEM_STORAGE_CELL = extend(STORAGE_CELL, "item");
    public static final TagKey<Item> FLUID_STORAGE_CELL = extend(STORAGE_CELL, "fluid");

    public static <T> TagKey<T> extend(TagKey<T> tag, String suffix) {
        return TagKey.create(tag.registry(), LocHelper.extend(tag.location(), suffix));
    }

    public static TagKey<Item> item(ResourceLocation loc) {
        return TagKey.create(Registry.ITEM_REGISTRY, loc);
    }

    public static TagKey<Item> modItem(String id) {
        return item(modLoc(id));
    }

    public static TagKey<Block> block(ResourceLocation loc) {
        return TagKey.create(Registry.BLOCK_REGISTRY, loc);
    }

    public static TagKey<Block> modBlock(String id) {
        return block(modLoc(id));
    }

    public static TagKey<Item> material(String sub) {
        return modItem(sub.startsWith("tool/") ? sub : "materials/" + sub);
    }

    public static TagKey<Item> circuit(Voltage v) {
        return modItem("circuit/" + v.id);
    }

    public static TagKey<Item> battery(Voltage v) {
        return modItem("battery/" + v.id);
    }

    public static TagKey<Item> circuitComponent(String component, CircuitComponentTier tier) {
        return modItem(tier.getName(component));
    }

    public static TagKey<Item> machine(String id) {
        return extend(MACHINE, id);
    }

    public static TagKey<Item> machine(IRecipeType<?> recipeType) {
        return machine(recipeType.id());
    }
}
