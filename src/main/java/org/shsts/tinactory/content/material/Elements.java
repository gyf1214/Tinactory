package org.shsts.tinactory.content.material;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Elements {
    static {
        IRON = set("iron")
                .color(0xFFC8C8C8)
                .existing("ingot", Tags.Items.INGOTS_IRON, Items.IRON_INGOT)
                .existing("nugget", Tags.Items.NUGGETS_IRON, Items.IRON_NUGGET)
                .mechanicalSet().nugget().wire()
                .buildObject();

        GOLD = set("gold")
                .color(0xFFFFE650)
                .existing("raw", Tags.Items.RAW_MATERIALS_GOLD, Items.RAW_GOLD)
                .existing("ingot", Tags.Items.INGOTS_GOLD, Items.GOLD_INGOT)
                .existing("nugget", Tags.Items.NUGGETS_GOLD, Items.GOLD_NUGGET)
                .metalSet().nugget()
//                .ore(OreVariant.DEEPSLATE)
                .buildObject();

        COPPER = set("copper")
                .color(0xFFFF6400)
                .existing("ingot", Tags.Items.INGOTS_COPPER, Items.COPPER_INGOT)
                .metalSet().wireAndPlate().pipe().foil()
                .buildObject();

        TIN = set("tin")
                .color(0xFFDCDCDC)
                .mechanicalSet().wire()
                .ore(OreVariant.STONE)
                .buildObject();

        SULFUR = set("sulfur")
                .color(0xFFC8C800)
                .dust()
                .buildObject();

        NICKEL = set("nickel")
                .color(0xFFC8C8FA)
                .metalSet()
                .buildObject();

        ALUMINIUM = set("aluminium")
                .color(0xFF80C8F0)
                .mechanicalSet()
                .buildObject();
    }

    public static void init() {}
}
