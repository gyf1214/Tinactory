package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final MaterialSet TEST = set("test", IconSet.DULL, 0xFFFFFF);
    public static final MaterialSet STONE = set("stone", IconSet.ROUGH, 0xCDCDCD);
    public static final MaterialSet FLINT = set("flint", IconSet.DULL, 0x002040);
    public static final MaterialSet IRON = set("iron", IconSet.METALLIC, 0xC8C8C8);
    public static final MaterialSet WROUGHT_IRON = set("wrought_iron", IconSet.METALLIC, 0xC8B4B4);

    static {
        TEST.toolSet(12800000).freeze();

        STONE.existing("block", ItemTags.STONE_CRAFTING_MATERIALS, Items.COBBLESTONE)
                .existing("tool/pickaxe", Items.STONE_PICKAXE)
                .existing("tool/shovel", Items.STONE_SHOVEL)
                .existing("tool/hoe", Items.STONE_HOE)
                .existing("tool/axe", Items.STONE_AXE)
                .existing("tool/sword", Items.STONE_SWORD)
                .alias("primary", "block")
                .hammer(16)
                .dust()
                .toolProcess()
                .freeze();

        FLINT.existing("primary", Items.FLINT)
                .dust()
                .mortar(16)
                .toolProcess()
                .freeze();

        IRON.existing("ingot", Tags.Items.INGOTS_IRON, Items.IRON_INGOT)
                .existing("nugget", Tags.Items.NUGGETS_IRON, Items.IRON_NUGGET)
                .mechanicalSet()
                .toolProcess()
                .freeze();

        WROUGHT_IRON.metalSet()
                .toolSet(64)
                .toolProcess()
                .freeze();

        // tool component tags
        REGISTRATE.itemTag(Items.STICK, AllTags.TOOL_HANDLE);
        REGISTRATE.itemTag(WROUGHT_IRON.getTag("stick"), AllTags.TOOL_HANDLE);
        REGISTRATE.itemTag(IRON.getTag("screw"), AllTags.TOOL_SCREW);
    }

    private static MaterialSet set(String id, IconSet icon, int color) {
        return new MaterialSet(id, icon, color);
    }

    public static void init() {}
}
