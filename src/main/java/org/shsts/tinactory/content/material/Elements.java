package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.model.ModelGen;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllMaterials.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Elements {
    static {
        IRON = set("iron")
                .color(0xFFC8C8C8).icon(IconSet.METALLIC)
                .existing("ingot", Tags.Items.INGOTS_IRON, Items.IRON_INGOT)
                .existing("nugget", Tags.Items.NUGGETS_IRON, Items.IRON_NUGGET)
                .existing("wire", ModelGen.modLoc("network/cable/ulv"))
                .mechanicalSet()
                .toolProcess().smelt()
                .buildObject();

        GOLD = set("gold")
                .color(0xFFFFE650).icon(IconSet.SHINY)
                .existing("ore", Blocks.DEEPSLATE_GOLD_ORE)
                .existing("raw", Tags.Items.RAW_MATERIALS_GOLD, Items.RAW_GOLD)
                .existing("ingot", Tags.Items.INGOTS_GOLD, Items.GOLD_INGOT)
                .existing("nugget", Tags.Items.NUGGETS_GOLD, Items.GOLD_NUGGET)
                .metalSet()
                .toolProcess().smelt()
                .ore(OreVariant.DEEPSLATE)
                // TODO: byproduct
                .build()
                .buildObject();

        COPPER = set("copper")
                .color(0xFFFF6400).icon(IconSet.SHINY)
                .existing("ingot", Tags.Items.INGOTS_COPPER, Items.COPPER_INGOT)
                .metalSet()
                .toolProcess().smelt()
                .buildObject();

        TIN = set("tin")
                .color(0xFFDCDCDC).icon(IconSet.DULL)
                .existing("cable", ModelGen.modLoc("network/cable/lv"))
                .metalSet()
                .toolProcess().smelt()
                .ore(OreVariant.STONE)
                // TODO: byproduct tin, zinc
                .build()
                .buildObject();
    }

    public static void init() {}
}
