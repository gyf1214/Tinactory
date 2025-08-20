package org.shsts.tinactory.content;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.material.Elements;
import org.shsts.tinactory.content.material.FirstDegrees;
import org.shsts.tinactory.content.material.HigherDegrees;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.Ores;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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
    public static MaterialSet ARSENIC;
    public static MaterialSet SILICON;
    public static MaterialSet OXYGEN;
    public static MaterialSet NITROGEN;
    public static MaterialSet ARGON;
    public static MaterialSet HYDROGEN;
    public static MaterialSet CHLORINE;
    public static MaterialSet BERYLLIUM;
    public static MaterialSet SODIUM;
    public static MaterialSet POTASSIUM;
    public static MaterialSet CALCIUM;
    public static MaterialSet LITHIUM;
    public static MaterialSet TITANIUM;
    public static MaterialSet NEODYMIUM;

    // First Degree
    public static MaterialSet WATER;
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
    public static MaterialSet BRASS;
    public static MaterialSet GALLIUM_ARSENIDE;
    public static MaterialSet KANTHAL;
    public static MaterialSet ELECTRUM;
    public static MaterialSet STAINLESS_STEEL;
    public static MaterialSet SODIUM_CHLORIDE;
    public static MaterialSet POTASSIUM_CHLORIDE;
    public static MaterialSet MAGNESIUM_CHLORIDE;
    public static MaterialSet CALCIUM_CHLORIDE;
    public static MaterialSet LITHIUM_CHLORIDE;
    public static MaterialSet AMMONIUM_CHLORIDE;
    public static MaterialSet SODIUM_CARBONATE;
    public static MaterialSet POTASSIUM_CARBONATE;
    public static MaterialSet CALCIUM_CARBONATE;
    public static MaterialSet LITHIUM_CARBONATE;
    public static MaterialSet SODIUM_SULFATE;
    public static MaterialSet POTASSIUM_NITRATE;
    public static MaterialSet SODIUM_HYDROXIDE;
    public static MaterialSet CALCIUM_HYDROXIDE;
    public static MaterialSet LITHIUM_BRINE;
    public static MaterialSet CARBON_DIOXIDE;
    public static MaterialSet HYDROGEN_CHLORIDE;
    public static MaterialSet HYDROGEN_SULFIDE;
    public static MaterialSet SULFURIC_ACID;
    public static MaterialSet NITRIC_ACID;
    public static MaterialSet AMMONIA;
    public static MaterialSet METHANE;
    public static MaterialSet ETHANE;
    public static MaterialSet PROPANE;
    public static MaterialSet ETHYLENE;
    public static MaterialSet PROPENE;
    public static MaterialSet ETHANOL;
    public static MaterialSet BENZENE;
    public static MaterialSet PHENOL;
    public static MaterialSet TOLUENE;
    public static MaterialSet IRON_CHLORIDE;
    public static MaterialSet VINYL_CHLORIDE;
    public static MaterialSet NICKEL_ZINC_FERRITE;
    public static MaterialSet COKE;
    public static MaterialSet CHARCOAL;
    public static MaterialSet SILICON_DIOXIDE;
    public static MaterialSet NICHROME;
    public static MaterialSet ALUMINIUM_OXIDE;
    public static MaterialSet HYDROGEN_FLUORIDE;
    public static MaterialSet CHLOROFORM;
    public static MaterialSet TETRA_FLUORO_ETHYLENE;
    public static MaterialSet TITANIUM_TETRACHLORIDE;
    public static MaterialSet ANNEALED_COPPER;

    // Higher Degree
    public static MaterialSet COBALT_BRASS;
    public static MaterialSet AIR;
    public static MaterialSet SALT_WATER;
    public static MaterialSet SEA_WATER;
    public static MaterialSet REFINERY_GAS;
    public static MaterialSet NAPHTHA;
    public static MaterialSet LIGHT_FUEL;
    public static MaterialSet HEAVY_FUEL;
    public static MaterialSet PE;
    public static MaterialSet PVC;
    public static MaterialSet BIOMASS;
    public static MaterialSet LPG;
    public static MaterialSet DIESEL;
    public static MaterialSet CREOSOTE_OIL;
    public static MaterialSet VANADIUM_STEEL;
    public static MaterialSet PTFE;

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
    public static MaterialSet NATURAL_GAS;
    public static MaterialSet LIGHT_OIL;
    public static MaterialSet HEAVY_OIL;
    public static MaterialSet EMERALD;
    public static MaterialSet SAPPHIRE;
    public static MaterialSet TOPAZ;
    public static MaterialSet BLUE_TOPAZ;

    // misc
    public static MaterialSet TEST;
    public static MaterialSet STONE;
    public static MaterialSet FLINT;
    public static MaterialSet RAW_RUBBER;
    public static MaterialSet RUBBER;
    public static MaterialSet GLOWSTONE;
    public static MaterialSet RARE_EARTH;
    public static MaterialSet GLASS;

    static {
        SET = new HashMap<>();
        Elements.init();
        FirstDegrees.init();
        HigherDegrees.init();
        Ores.init();

        TEST = set("test")
            .toolSet(12800000, Tiers.NETHERITE)
            .buildObject();

        STONE = set("stone")
            .color(0xFFCDCDCD)
            .existing("block", Items.COBBLESTONE)
            .alias("primary", "block")
            .dust()
            .tool(16).hammer().build()
            .buildObject();

        FLINT = set("flint")
            .color(0xFF002040)
            .existing("primary", Items.FLINT)
            .tool(16).mortar().build()
            .buildObject();

        RAW_RUBBER = dust("raw_rubber", 0xFFCCC789);

        RUBBER = set("rubber")
            .color(0xFF000000)
            .polymerRing()
            .buildObject();

        GLOWSTONE = set("glowstone")
            .color(0xFFFFFF00)
            .existing("dust", Tags.Items.DUSTS_GLOWSTONE, Items.GLOWSTONE_DUST)
            .buildObject();

        RARE_EARTH = dust("rare_earth", 0xFF808064);

        GLASS = set("glass")
            .color(0xFFFAFAFA)
            .existing("gem", Items.GLASS)
            .alias("primary", "gem")
            .buildObject();

        alias("pe", PE);
        alias("pvc", PVC);
        alias("ptfe", PTFE);
    }

    public static final Map<String, MaterialSet> SET;

    public static MaterialSet.Builder<?> set(String id) {
        return MaterialSet.builder(Unit.INSTANCE, id)
            .onCreateObject(mat -> SET.put(mat.name, mat));
    }

    public static MaterialSet dust(String id, int color) {
        return set(id)
            .color(color)
            .dustPrimary()
            .buildObject();
    }

    public static MaterialSet gas(String id, int color) {
        return set(id)
            .color(color)
            .gas()
            .buildObject();
    }

    public static MaterialSet liquid(String id, int color) {
        return set(id)
            .color(color)
            .liquid()
            .buildObject();
    }

    public static MaterialSet fluid(String id, int color, String sub, String tex) {
        return set(id)
            .color(color)
            .fluid(sub, tex, 1000)
            .fluidPrimary(sub)
            .buildObject();
    }

    public static MaterialSet fluid(String id, int color, String sub) {
        return fluid(id, color, sub, id);
    }

    public static TagKey<Item> tag(String sub) {
        return MaterialSet.Builder.prefixTag(sub);
    }

    public static Function<Item.Properties, Item> fuelItem(int burnTime) {
        return prop -> new Item(prop) {
            @Override
            public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
                return burnTime;
            }
        };
    }

    private static void alias(String name, MaterialSet set) {
        SET.put(name, set);
    }

    public static MaterialSet getMaterial(String name) {
        return SET.get(name);
    }

    public static void init() {}
}
