package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.common.Tags;
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
    public static MaterialSet COBALT;
    public static MaterialSet CADMIUM;
    public static MaterialSet NICKEL;
    public static MaterialSet MAGNESIUM;
    public static MaterialSet THORIUM;
    public static MaterialSet CHROME;
    public static MaterialSet ANTIMONY;
    public static MaterialSet SILVER;
    public static MaterialSet VANADIUM;
    public static MaterialSet ALUMINIUM;
    public static MaterialSet LEAD;
    public static MaterialSet ZINC;
    public static MaterialSet GALLIUM;
    public static MaterialSet CARBON;
    public static MaterialSet MANGANESE;

    // First Degree
    public static MaterialSet WROUGHT_IRON;
    public static MaterialSet BRONZE;
    public static MaterialSet COBALTITE;
    public static MaterialSet INVAR;
    public static MaterialSet CUPRONICKEL;
    public static MaterialSet STEEL;
    public static MaterialSet RED_ALLOY;
    public static MaterialSet BATTERY_ALLOY;
    public static MaterialSet SOLDERING_ALLOY;
    public static MaterialSet RUTILE;

    // Ore
    public static MaterialSet CHALCOPYRITE;
    public static MaterialSet PYRITE;
    public static MaterialSet LIMONITE;
    public static MaterialSet BANDED_IRON;
    public static MaterialSet GARNIERITE;
    public static MaterialSet COAL;
    public static MaterialSet CASSITERITE;
    public static MaterialSet REDSTONE;
    public static MaterialSet CINNABAR;
    public static MaterialSet RUBY;
    public static MaterialSet MAGNETITE;
    public static MaterialSet GALENA;
    public static MaterialSet SPHALERITE;
    public static MaterialSet GRAPHITE;
    public static MaterialSet DIAMOND;
    public static MaterialSet BAUXITE;
    public static MaterialSet ILMENITE;

    public static MaterialSet TEST;
    public static MaterialSet STONE;
    public static MaterialSet FLINT;
    public static MaterialSet RAW_RUBBER;
    public static MaterialSet RUBBER;
    public static MaterialSet GLOWSTONE;
    public static MaterialSet RARE_EARTH;

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
                .tool(16).mortar().build()
                .buildObject();

        RAW_RUBBER = set("raw_rubber")
                .color(0xFFCCC789)
                .dust()
                .buildObject();

        RUBBER = set("rubber")
                .color(0xFF000000)
                .polymer()
                .buildObject();

        GLOWSTONE = set("glowstone")
                .color(0xFFFFFF00)
                .existing("dust", Tags.Items.DUSTS_GLOWSTONE, Items.GLOWSTONE_DUST)
                .buildObject();

        RARE_EARTH = set("rare_earth")
                .color(0xFF808064)
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
