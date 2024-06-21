package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import org.shsts.tinactory.content.material.Elements;
import org.shsts.tinactory.content.material.FirstDegrees;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.Ores;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    // Element
    public static MaterialSet IRON;
    public static MaterialSet GOLD;
    public static MaterialSet COPPER;
    public static MaterialSet TIN;
    public static MaterialSet SULFUR;
    public static MaterialSet NICKEL;
    public static MaterialSet ALUMINIUM;

    // First Degree
    public static MaterialSet WROUGHT_IRON;
    public static MaterialSet BRONZE;
    public static MaterialSet INVAR;
    public static MaterialSet CUPRONICKEL;
    public static MaterialSet STEEL;

    // Ore
    public static MaterialSet CHALCOPYRITE;
    public static MaterialSet PYRITE;
    public static MaterialSet LIMONITE;
    public static MaterialSet BANDED_IRON;
    public static MaterialSet COAL;
    public static MaterialSet CASSITERITE;
    public static MaterialSet REDSTONE;
    public static MaterialSet CINNABAR;
    public static MaterialSet RUBY;
    public static MaterialSet MAGNETITE;

    public static MaterialSet TEST;
    public static MaterialSet STONE;
    public static MaterialSet FLINT;
    public static MaterialSet RAW_RUBBER;

    static {
        SET = new HashMap<>();
        Elements.init();
        FirstDegrees.init();
        Ores.init();

        TEST = set("test")
                .toolSet(12800000, Tiers.NETHERITE)
                .buildObject();

        STONE = set("stone")
                .color(0xFFCDCDCD)
                .existing("block", Items.COBBLESTONE)
                .existing("tool/pickaxe", Items.STONE_PICKAXE)
                .existing("tool/shovel", Items.STONE_SHOVEL)
                .existing("tool/hoe", Items.STONE_HOE)
                .existing("tool/axe", Items.STONE_AXE)
                .existing("tool/sword", Items.STONE_SWORD)
                .alias("primary", "block")
                .dust()
                .tool(16).hammer().build()
                .buildObject();

        FLINT = set("flint")
                .color(0xFF002040)
                .existing("primary", Items.FLINT)
                .dust()
                .tool(16).mortar().build()
                .buildObject();

        RAW_RUBBER = set("raw_rubber")
                .color(0xFFCCC789)
                .dust()
                .buildObject();
    }

    public static final Map<String, MaterialSet> SET;

    public static MaterialSet.Builder<?> set(String id) {
        return (new MaterialSet.Builder<>(Unit.INSTANCE, id))
                .onCreateObject(mat -> SET.put(mat.name, mat));
    }

    public static TagKey<Item> tag(String sub) {
        return MaterialSet.Builder.prefixTag(sub);
    }

    public static void init() {}
}
