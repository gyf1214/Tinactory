package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.datagen.content.builder.MaterialBuilder;
import org.shsts.tinactory.datagen.content.model.IconSet;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.Tinactory._REGISTRATE;
import static org.shsts.tinactory.content.AllItems.RUBBER_LEAVES;
import static org.shsts.tinactory.content.AllItems.RUBBER_LOG;
import static org.shsts.tinactory.content.AllItems.RUBBER_SAPLING;
import static org.shsts.tinactory.content.AllItems.STEAM;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.ANTIMONY;
import static org.shsts.tinactory.content.AllMaterials.ARSENIC;
import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.BATTERY_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.BAUXITE;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CADMIUM;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CHROME;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COBALT;
import static org.shsts.tinactory.content.AllMaterials.COBALTITE;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.DIAMOND;
import static org.shsts.tinactory.content.AllMaterials.ELECTRUM;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.GALENA;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM_ARSENIDE;
import static org.shsts.tinactory.content.AllMaterials.GARNIERITE;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.GLOWSTONE;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.GRAPHITE;
import static org.shsts.tinactory.content.AllMaterials.ILMENITE;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.LEAD;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.MANGANESE;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.RARE_EARTH;
import static org.shsts.tinactory.content.AllMaterials.RAW_RUBBER;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.RUTILE;
import static org.shsts.tinactory.content.AllMaterials.SILICON;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.SPHALERITE;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.TEST;
import static org.shsts.tinactory.content.AllMaterials.THORIUM;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllMaterials.ZINC;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.EXTRACTOR;
import static org.shsts.tinactory.content.AllRecipes.STEAM_TURBINE;
import static org.shsts.tinactory.content.AllRecipes.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.TOOL;
import static org.shsts.tinactory.content.AllTags.TOOL_FILE;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_HANDLE;
import static org.shsts.tinactory.content.AllTags.TOOL_MORTAR;
import static org.shsts.tinactory.content.AllTags.TOOL_SAW;
import static org.shsts.tinactory.content.AllTags.TOOL_SCREW;
import static org.shsts.tinactory.content.AllTags.TOOL_SCREWDRIVER;
import static org.shsts.tinactory.content.AllTags.TOOL_SHEARS;
import static org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.datagen.DataGen._DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.cubeTint;
import static org.shsts.tinactory.datagen.content.model.IconSet.DULL;
import static org.shsts.tinactory.datagen.content.model.IconSet.METALLIC;
import static org.shsts.tinactory.datagen.content.model.IconSet.ROUGH;
import static org.shsts.tinactory.datagen.content.model.IconSet.SHINY;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Materials {
    public static void init() {
        wood();
        elements();
        firstDegrees();
        higherDegrees();
        ores();
        misc();
        tags();
    }

    private static class MaterialFactory {
        public MaterialBuilder<MaterialFactory> material(MaterialSet material, IconSet icon) {
            return (new MaterialBuilder<>(DATA_GEN, this, material)).icon(icon);
        }
    }

    private static final MaterialFactory FACTORY = new MaterialFactory();

    private static void wood() {
        // all wood recipes
        woodRecipes("oak");
        woodRecipes("spruce");
        woodRecipes("birch");
        woodRecipes("jungle");
        woodRecipes("acacia");
        woodRecipes("dark_oak");
        woodRecipes("crimson");
        woodRecipes("warped");

        // disable wooden and iron tools
        _DATA_GEN.nullRecipe(Items.WOODEN_AXE)
            .nullRecipe(Items.WOODEN_HOE)
            .nullRecipe(Items.WOODEN_PICKAXE)
            .nullRecipe(Items.WOODEN_SHOVEL)
            .nullRecipe(Items.WOODEN_SWORD)
            .nullRecipe(Items.IRON_AXE)
            .nullRecipe(Items.IRON_HOE)
            .nullRecipe(Items.IRON_PICKAXE)
            .nullRecipe(Items.IRON_SHOVEL)
            .nullRecipe(Items.IRON_SWORD);

        // stick
        TOOL_CRAFTING.recipe(_DATA_GEN, Items.STICK)
            .result(Items.STICK, 4)
            .pattern("#").pattern("#")
            .define('#', ItemTags.PLANKS)
            .toolTag(TOOL_SAW)
            .build();
        _DATA_GEN.replaceVanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(Items.STICK, 2)
            .define('#', ItemTags.PLANKS)
            .pattern("#").pattern("#")
            .unlockedBy("has_planks", has(ItemTags.PLANKS)));

        // rubber
        _DATA_GEN.block(RUBBER_LOG)
            .blockState(ctx -> ctx.provider
                .axisBlock(ctx.object, gregtech("blocks/wood/rubber/log_rubber_side"),
                    gregtech("blocks/wood/rubber/log_rubber_top")))
            .tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN)
            .itemTag(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN)
            .dropSelf()
            .dropOnState(STICKY_RESIN, RubberLogBlock.HAS_RUBBER, true)
            .build()
            .block(RUBBER_LEAVES)
            .blockState(cubeTint("wood/rubber/leaves_rubber"))
            .tag(BlockTags.LEAVES).itemTag(ItemTags.LEAVES)
            .dropSelfOnTool(TOOL_SHEARS)
            .drop(RUBBER_SAPLING, 0.05f)
            .build()
            .block(RUBBER_SAPLING)
            .blockState(ctx -> ctx.provider.simpleBlock(ctx.object, ctx.provider.models()
                .cross(ctx.id, gregtech("blocks/wood/rubber/sapling_rubber"))))
            .itemModel(Models.xBasicItem(gregtech("blocks/wood/rubber/sapling_rubber")))
            .tag(BlockTags.SAPLINGS).itemTag(ItemTags.SAPLINGS)
            .build();
    }

    private static void elements() {
        FACTORY.material(IRON, METALLIC)
            .toolProcess().smelt()
            .build()
            .material(GOLD, SHINY)
            .toolProcess().smelt()
            .oreProcess(SILVER, NICKEL, SILVER)
            .build()
            .material(COPPER, SHINY)
            .toolProcess(0.75d).smelt()
            .build()
            .material(TIN, DULL)
            .toolProcess(0.75d).smelt()
            .oreProcess()
            .build()
            .material(SULFUR, DULL).build()
            .material(CADMIUM, SHINY).build()
            .material(COBALT, METALLIC)
            .toolProcess(1.25d).smelt()
            .build()
            .material(NICKEL, METALLIC)
            .toolProcess(1.25d).smelt()
            .build()
            .material(MAGNESIUM, METALLIC)
            .machineProcess(Voltage.LV)
            .build()
            .material(THORIUM, SHINY).build()
            // TODO
            .material(CHROME, SHINY).build()
            .material(ANTIMONY, SHINY).build()
            .material(SILVER, SHINY)
            .machineProcess(Voltage.LV).smelt()
            .oreProcess(ANTIMONY, ANTIMONY, GALLIUM)
            .build()
            .material(VANADIUM, METALLIC).build()
            .material(ALUMINIUM, DULL)
            .machineProcess(Voltage.LV)
            .blast(Voltage.LV, 1500, 400)
            .build()
            .material(LEAD, DULL)
            .machineProcess(Voltage.LV).smelt()
            .build()
            .material(ZINC, METALLIC)
            .machineProcess(Voltage.LV).smelt()
            .build()
            .material(GALLIUM, SHINY)
            .machineProcess(Voltage.LV).smelt()
            .build()
            .material(CARBON, DULL)
            .machineProcess(Voltage.HV)
            .build()
            .material(MANGANESE, DULL).build()
            .material(ARSENIC, DULL).build()
            .material(SILICON, METALLIC).build();
    }

    private static void firstDegrees() {
        FACTORY.material(WROUGHT_IRON, METALLIC)
            .toolProcess().smelt()
            .build()
            .material(BRONZE, METALLIC)
            .toolProcess(0.75d).smelt()
            .alloy(Voltage.ULV, COPPER, 3, TIN, 1)
            .build()
            .material(COBALTITE, METALLIC)
            .smelt(COBALT)
            .decompose(Voltage.LV, COBALT, 1, ARSENIC, 1, SULFUR, 1)
            .build()
            .material(INVAR, METALLIC)
            .toolProcess(1.25d).smelt()
            .alloy(Voltage.ULV, IRON, 2, NICKEL, 1)
            .build()
            .material(CUPRONICKEL, METALLIC)
            .toolProcess(1.25d).smelt()
            .alloy(Voltage.ULV, COPPER, 1, NICKEL, 1)
            .build()
            .material(STEEL, METALLIC)
            .toolProcess(1.5d)
            .blast(Voltage.ULV, 1000, 800)
            .blast(Voltage.ULV, 1000, 1000, IRON)
            .build()
            .material(RED_ALLOY, DULL)
            .toolProcess(0.5d).smelt()
            .alloyOnly(Voltage.ULV, 1, COPPER, 1, REDSTONE, 4)
            .build()
            .material(BATTERY_ALLOY, DULL)
            .machineProcess(Voltage.LV).smelt()
            .alloy(Voltage.LV, LEAD, 4, ANTIMONY, 1)
            .build()
            .material(SOLDERING_ALLOY, DULL)
            .fluidAlloy(Voltage.LV, TIN, 6, LEAD, 3, ANTIMONY, 1)
            .build()
            .material(RUTILE, SHINY)
            .build()
            .material(BRASS, METALLIC)
            .toolProcess(0.75d).smelt()
            .alloy(Voltage.LV, ZINC, 1, COPPER, 3)
            .build()
            .material(GALLIUM_ARSENIDE, DULL)
            .mix(Voltage.LV, GALLIUM, 1, ARSENIC, 1)
            .build()
            .material(KANTHAL, METALLIC)
            .mix(Voltage.LV, IRON, 1, ALUMINIUM, 1, CHROME, 1)
            .machineProcess(Voltage.LV)
            .blast(Voltage.LV, 1800, 1000)
            .build()
            .material(ELECTRUM, SHINY)
            .machineProcess(Voltage.LV, 0.75d).smelt()
            .alloy(Voltage.LV, GOLD, 1, SILVER, 1)
            .build();
    }

    private static void higherDegrees() {
        FACTORY.material(COBALT_BRASS, METALLIC)
            .machineProcess(Voltage.LV, 2d).smelt()
            .mix(Voltage.LV, BRASS, 7, ALUMINIUM, 1, COBALT, 1)
            .build();
    }

    private static void ores() {
        // stone generator
        for (var variant : OreVariant.values()) {
            STONE_GENERATOR.recipe(_DATA_GEN, variant.baseItem)
                .outputItem(0, variant.baseItem, 1)
                .voltage(variant == OreVariant.STONE ? Voltage.PRIMITIVE : variant.voltage)
                .build();
        }
        // generate water
        STONE_GENERATOR
            .recipe(_DATA_GEN, Fluids.WATER)
            .outputFluid(1, Fluids.WATER, 1000)
            .voltage(Voltage.ULV)
            .build();

        FACTORY.material(CHALCOPYRITE, DULL)
            .primitiveOreProcess(SULFUR, COBALTITE, ZINC)
            .smelt(COPPER)
            .build()
            .material(PYRITE, ROUGH)
            .primitiveOreProcess(SULFUR, COPPER, CADMIUM)
            .smelt(IRON)
            .build()
            .material(LIMONITE, METALLIC)
            .oreProcess(NICKEL, COPPER, NICKEL).smelt(IRON)
            .build()
            .material(BANDED_IRON, DULL)
            .oreProcess(NICKEL, COPPER, NICKEL).smelt(IRON)
            .build()
            .material(GARNIERITE, METALLIC)
            .oreProcess(MAGNESIUM, MAGNESIUM, NICKEL)
            .smelt(NICKEL)
            .build()
            .material(COAL, DULL)
            .toolProcess()
            .oreBuilder(COAL, COAL, THORIUM)
            .amount(2).siftAndHammer().build()
            .build()
            .material(CASSITERITE, METALLIC)
            .oreProcess(2, TIN).smelt(TIN)
            .build()
            .material(REDSTONE, DULL)
            .oreProcess(5, GLOWSTONE, GLOWSTONE, RARE_EARTH)
            .decompose(Voltage.LV, PYRITE, 6, RUBY, 3, SILICON, 1)
            .build()
            .material(CINNABAR, SHINY)
            .oreProcess(RARE_EARTH, GLOWSTONE, RARE_EARTH)
            .build()
            .material(RUBY, IconSet.RUBY)
            .machineProcess(Voltage.LV, 2d)
            .oreBuilder(CHROME).siftAndHammer().build()
            .build()
            .material(MAGNETITE, METALLIC)
            .oreProcess(GOLD, VANADIUM, COPPER)
            .smelt(IRON)
            .build()
            .material(GALENA, DULL)
            .oreProcess(SULFUR, ANTIMONY, SILVER)
            .smelt(LEAD)
            .build()
            .material(SPHALERITE, DULL)
            .oreProcess(SULFUR, SILVER, GALLIUM)
            .smelt(ZINC)
            .build()
            .material(GRAPHITE, DULL)
            .oreProcess(CARBON)
            .build()
            .material(DIAMOND, IconSet.DIAMOND)
            .oreBuilder(CARBON).siftAndHammer().build()
            .machineProcess(Voltage.LV, 2d)
            .build()
            .material(BAUXITE, DULL)
            .oreProcess(ALUMINIUM, GALLIUM, RUTILE)
            .build()
            .material(ILMENITE, METALLIC)
            .oreProcess(MANGANESE, MANGANESE, RUTILE)
            .build();
    }

    private static void misc() {
        FACTORY.material(TEST, DULL).build()
            .material(STONE, ROUGH).toolProcess().build()
            .material(FLINT, DULL).toolProcess().build()
            .material(RAW_RUBBER, DULL).build()
            .material(RUBBER, SHINY)
            .toolProcess()
            .build()
            .material(GLOWSTONE, SHINY).build()
            .material(RARE_EARTH, ROUGH).build()
            .material(GLASS, SHINY).build();

        // disable vanilla nugget
        disableVanillaOres("iron");
        disableVanillaOres("gold");
        disableVanillaOres("copper");
        disableVanillaOres("coal", "");
        disableVanillaOres("diamond", "");
        disableVanillaOres("redstone", "");
        disableVanillaOres("lapis", "lazuli");

        // smelt wrought iron nugget
        _DATA_GEN.vanillaRecipe(() -> SimpleCookingRecipeBuilder
            .smelting(Ingredient.of(IRON.tag("nugget")), WROUGHT_IRON.item("nugget"), 0, 200)
            .unlockedBy("has_material", has(IRON.tag("nugget"))), "_from_iron");

        // stone -> gravel
        TOOL_CRAFTING.recipe(_DATA_GEN, Items.GRAVEL)
            .result(Items.GRAVEL, 1)
            .pattern("#").pattern("#")
            .define('#', STONE.tag("block"))
            .toolTag(TOOL_HAMMER)
            .build()
            // gravel -> flint
            .recipe(_DATA_GEN, FLINT.loc("primary"))
            .result(FLINT.entry("primary"), 1)
            .pattern("###")
            .define('#', Items.GRAVEL)
            .toolTag(TOOL_HAMMER)
            .build()
            // gravel -> sand
            .recipe(_DATA_GEN, Items.SAND)
            .result(Items.SAND, 1)
            .pattern("#")
            .define('#', Items.GRAVEL)
            .toolTag(TOOL_MORTAR)
            .build();

        // generate steam
        for (var voltage : Voltage.between(Voltage.ULV, Voltage.HV)) {
            var consume = (int) voltage.value / 8 * (14 - voltage.rank);
            STEAM_TURBINE.recipe(_DATA_GEN, voltage.id)
                .voltage(voltage)
                .inputFluid(0, STEAM, consume)
                .outputFluid(1, Fluids.WATER, (int) voltage.value / 8 * 5)
                .build();
        }

        // rubber
        TOOL_CRAFTING.recipe(_DATA_GEN, RAW_RUBBER.loc("dust"))
            .result(RAW_RUBBER.entry("dust"), 1)
            .pattern("A").define('A', STICKY_RESIN)
            .toolTag(TOOL_MORTAR)
            .build();

        EXTRACTOR.recipe(_DATA_GEN, RAW_RUBBER.loc("dust"))
            .outputItem(1, RAW_RUBBER.entry("dust"), 3)
            .inputItem(0, STICKY_RESIN, 1)
            .workTicks(160L)
            .voltage(Voltage.LV)
            .build();

        EXTRACTOR.recipe(_DATA_GEN, suffix(RAW_RUBBER.loc("dust"), "_from_log"))
            .outputItem(1, RAW_RUBBER.entry("dust"), 1)
            .inputItem(0, RUBBER_LOG, 1)
            .workTicks(320L)
            .voltage(Voltage.LV)
            .build();

        ALLOY_SMELTER.recipe(_DATA_GEN, RUBBER.loc("sheet"))
            .inputItem(0, RAW_RUBBER.entry("dust"), 3)
            .inputItem(0, SULFUR.entry("dust"), 1)
            .outputItem(1, RUBBER.entry("sheet"), 3)
            .workTicks(300)
            .voltage(Voltage.ULV)
            .build();
    }

    private static final String[] VANILLA_METHODS = new String[]{"smelting", "blasting"};

    private static void disableVanillaOres(String name, String suffix) {
        var fullName = name + (suffix.isEmpty() ? "" : "_" + suffix);

        _DATA_GEN.nullRecipe(name + "_block");

        if (suffix.equals("ingot")) {
            _DATA_GEN.nullRecipe("raw_" + name)
                .nullRecipe("raw_" + name + "_block");
            if (name.equals("copper")) {
                _DATA_GEN.nullRecipe(fullName)
                    .nullRecipe(fullName + "_from_waxed_copper_block");
            } else {
                _DATA_GEN.nullRecipe(fullName + "_from_" + name + "_block")
                    .nullRecipe(fullName + "_from_nuggets")
                    .nullRecipe(name + "_nugget");
                for (var method : VANILLA_METHODS) {
                    _DATA_GEN.nullRecipe(name + "_nugget_from_" + method);
                }
            }
        } else {
            _DATA_GEN.nullRecipe(fullName);
        }

        var ores = new ArrayList<>(List.of("", "_deepslate"));
        if (name.equals("gold")) {
            ores.add("_nether");
        }

        for (var method : VANILLA_METHODS) {
            for (var ore : ores) {
                _DATA_GEN.nullRecipe(fullName + "_from_" + method + ore + "_" + name + "_ore");
            }
            if (suffix.equals("ingot")) {
                _DATA_GEN.nullRecipe(fullName + "_from_" + method + "_raw_" + name);
            }
        }
    }

    private static void disableVanillaOres(String name) {
        disableVanillaOres(name, "ingot");
    }

    private static void tags() {
        DATA_GEN.tag(TOOL_HAMMER, TOOL)
            .tag(TOOL_MORTAR, TOOL)
            .tag(TOOL_FILE, TOOL)
            .tag(TOOL_SAW, TOOL)
            .tag(TOOL_SCREWDRIVER, TOOL)
            .tag(TOOL_WRENCH, TOOL)
            .tag(TOOL_WIRE_CUTTER, TOOL)
            .tag(() -> Items.SHEARS, TOOL_SHEARS)
            .tag(() -> Items.STICK, TOOL_HANDLE)
            .tag(WROUGHT_IRON.tag("stick"), TOOL_HANDLE)
            .tag(IRON.tag("screw"), TOOL_SCREW);
    }

    private static void woodRecipes(String prefix) {
        var nether = prefix.equals("crimson") || prefix.equals("warped");

        var planks = _REGISTRATE.itemHandler.getEntry(prefix + "_planks");
        var logTag = AllTags.item(prefix + (nether ? "_stems" : "_logs"));
        var wood = prefix + (nether ? "_hyphae" : "_wood");
        var woodStripped = "stripped_" + wood;

        // saw plank
        TOOL_CRAFTING.recipe(_DATA_GEN, planks.loc)
            .result(planks, 4)
            .pattern("X")
            .define('X', logTag)
            .toolTag(TOOL_SAW)
            .build();

        // disable wood and woodStripped recipes
        _DATA_GEN.nullRecipe(wood)
            .nullRecipe(woodStripped)
            // reduce vanilla recipe to 2 planks
            .replaceVanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(planks.get(), 2)
                .requires(logTag)
                .group("planks")
                .unlockedBy("has_logs", has(logTag)));
    }
}
