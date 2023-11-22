package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.material.IconSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    private static final List<ResourceLocation> ORE_BASE_OVERWORLD = blockModels(
            Blocks.STONE, Blocks.DEEPSLATE);

    public static final MaterialSet TEST = set("test", IconSet.DULL, 0xFFFFFFFF);
    public static final MaterialSet STONE = set("stone", IconSet.ROUGH, 0xFFCDCDCD);
    public static final MaterialSet FLINT = set("flint", IconSet.DULL, 0xFF002040);
    public static final MaterialSet IRON = set("iron", IconSet.METALLIC, 0xFFC8C8C8);
    public static final MaterialSet WROUGHT_IRON = set("wrought_iron", IconSet.METALLIC, 0xFFC8B4B4);
    public static final MaterialSet MAGNETITE = set("magnetite", IconSet.METALLIC, 0xFF1E1E1E);

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
                .smelt(200)
                .freeze();

        WROUGHT_IRON.metalSet()
                .toolSet(64)
                .toolProcess()
                .smelt(200)
                .freeze();

        MAGNETITE.ore(ORE_BASE_OVERWORLD, Tiers.IRON, 3.0f)
                .freeze();

        // tool component tags
        REGISTRATE.tag(Items.STICK, AllTags.TOOL_HANDLE);
        REGISTRATE.tag(WROUGHT_IRON.getTag("stick"), AllTags.TOOL_HANDLE);
        REGISTRATE.tag(IRON.getTag("screw"), AllTags.TOOL_SCREW);
    }

    private static List<ResourceLocation> blockModels(Block... blocks) {
        return Arrays.stream(blocks).map(block -> {
            var loc = block.getRegistryName();
            assert loc != null;
            return ModelGen.prepend(loc, "block");
        }).toList();
    }

    private static MaterialSet set(String id, IconSet icon, int color) {
        return new MaterialSet(id, icon, color);
    }

    public static void init() {}
}
