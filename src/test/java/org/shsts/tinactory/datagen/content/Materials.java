package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.datagen.content.builder.MaterialBuilder;
import org.shsts.tinactory.datagen.content.model.IconSet;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllItems.FERTILIZER;
import static org.shsts.tinactory.content.AllItems.RUBBER_LEAVES;
import static org.shsts.tinactory.content.AllItems.RUBBER_LOG;
import static org.shsts.tinactory.content.AllItems.RUBBER_SAPLING;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM_OXIDE;
import static org.shsts.tinactory.content.AllMaterials.AMMONIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.ANNEALED_COPPER;
import static org.shsts.tinactory.content.AllMaterials.ANTIMONY;
import static org.shsts.tinactory.content.AllMaterials.ARSENIC;
import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.BATTERY_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.BAUXITE;
import static org.shsts.tinactory.content.AllMaterials.BERYLLIUM;
import static org.shsts.tinactory.content.AllMaterials.BIOMASS;
import static org.shsts.tinactory.content.AllMaterials.BLUE_TOPAZ;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CADMIUM;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.CARBON;
import static org.shsts.tinactory.content.AllMaterials.CARBON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CHARCOAL;
import static org.shsts.tinactory.content.AllMaterials.CHROME;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COBALT;
import static org.shsts.tinactory.content.AllMaterials.COBALTITE;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.COKE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.DIAMOND;
import static org.shsts.tinactory.content.AllMaterials.DIESEL;
import static org.shsts.tinactory.content.AllMaterials.ELECTRUM;
import static org.shsts.tinactory.content.AllMaterials.EMERALD;
import static org.shsts.tinactory.content.AllMaterials.ETHANE;
import static org.shsts.tinactory.content.AllMaterials.ETHANOL;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.GALENA;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM_ARSENIDE;
import static org.shsts.tinactory.content.AllMaterials.GARNIERITE;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.GLOWSTONE;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.GRAPHITE;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_FUEL;
import static org.shsts.tinactory.content.AllMaterials.HEAVY_OIL;
import static org.shsts.tinactory.content.AllMaterials.ILMENITE;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.LEAD;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_FUEL;
import static org.shsts.tinactory.content.AllMaterials.LIGHT_OIL;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.LPG;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM;
import static org.shsts.tinactory.content.AllMaterials.MAGNESIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.MANGANESE;
import static org.shsts.tinactory.content.AllMaterials.METHANE;
import static org.shsts.tinactory.content.AllMaterials.NATURAL_GAS;
import static org.shsts.tinactory.content.AllMaterials.NEODYMIUM;
import static org.shsts.tinactory.content.AllMaterials.NICHROME;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.NICKEL_ZINC_FERRITE;
import static org.shsts.tinactory.content.AllMaterials.NITROGEN;
import static org.shsts.tinactory.content.AllMaterials.OXYGEN;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.POTASSIUM_NITRATE;
import static org.shsts.tinactory.content.AllMaterials.PROPANE;
import static org.shsts.tinactory.content.AllMaterials.PTFE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.RARE_EARTH;
import static org.shsts.tinactory.content.AllMaterials.RAW_RUBBER;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.RUTILE;
import static org.shsts.tinactory.content.AllMaterials.SALT_WATER;
import static org.shsts.tinactory.content.AllMaterials.SAPPHIRE;
import static org.shsts.tinactory.content.AllMaterials.SILICON;
import static org.shsts.tinactory.content.AllMaterials.SILICON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SODIUM;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_SULFATE;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.SPHALERITE;
import static org.shsts.tinactory.content.AllMaterials.STAINLESS_STEEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.SULFURIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.TEST;
import static org.shsts.tinactory.content.AllMaterials.THORIUM;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM;
import static org.shsts.tinactory.content.AllMaterials.TOPAZ;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM_STEEL;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllMaterials.ZINC;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.AUTOFARM;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.COMBUSTION_GENERATOR;
import static org.shsts.tinactory.content.AllRecipes.CUTTER;
import static org.shsts.tinactory.content.AllRecipes.EXTRACTOR;
import static org.shsts.tinactory.content.AllRecipes.GAS_TURBINE;
import static org.shsts.tinactory.content.AllRecipes.LATHE;
import static org.shsts.tinactory.content.AllRecipes.MACERATOR;
import static org.shsts.tinactory.content.AllRecipes.SIFTER;
import static org.shsts.tinactory.content.AllRecipes.STEAM_TURBINE;
import static org.shsts.tinactory.content.AllRecipes.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllRegistries.ITEMS;
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
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.cubeTint;
import static org.shsts.tinactory.datagen.content.model.IconSet.BRIGHT;
import static org.shsts.tinactory.datagen.content.model.IconSet.DULL;
import static org.shsts.tinactory.datagen.content.model.IconSet.FINE;
import static org.shsts.tinactory.datagen.content.model.IconSet.GEM_HORIZONTAL;
import static org.shsts.tinactory.datagen.content.model.IconSet.GEM_VERTICAL;
import static org.shsts.tinactory.datagen.content.model.IconSet.LIGNITE;
import static org.shsts.tinactory.datagen.content.model.IconSet.METALLIC;
import static org.shsts.tinactory.datagen.content.model.IconSet.QUARTZ;
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
        crops();
        misc();
        tags();
    }

    private static class MaterialFactory {
        public MaterialBuilder<MaterialFactory> material(MaterialSet material, IconSet icon) {
            return MaterialBuilder.factory(DATA_GEN, this, material).icon(icon);
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
        woodFarmRecipes(RUBBER_SAPLING.loc(), RUBBER_SAPLING, RUBBER_LOG, RUBBER_LEAVES);

        // disable wooden and iron tools
        DATA_GEN.nullRecipe(Items.WOODEN_AXE)
            .nullRecipe(Items.WOODEN_HOE)
            .nullRecipe(Items.WOODEN_PICKAXE)
            .nullRecipe(Items.WOODEN_SHOVEL)
            .nullRecipe(Items.WOODEN_SWORD)
            .nullRecipe(Items.IRON_AXE)
            .nullRecipe(Items.IRON_HOE)
            .nullRecipe(Items.IRON_PICKAXE)
            .nullRecipe(Items.IRON_SHOVEL)
            .nullRecipe(Items.IRON_SWORD)
            .nullRecipe(Items.COMPOSTER);

        // stick
        TOOL_CRAFTING.recipe(DATA_GEN, Items.STICK)
            .result(Items.STICK, 4)
            .pattern("#").pattern("#")
            .define('#', ItemTags.PLANKS)
            .toolTag(TOOL_SAW)
            .build();
        DATA_GEN.replaceVanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(Items.STICK, 2)
            .define('#', ItemTags.PLANKS)
            .pattern("#").pattern("#")
            .unlockedBy("has_planks", has(ItemTags.PLANKS)));
        LATHE.recipe(DATA_GEN, Items.STICK)
            .inputItem(ItemTags.PLANKS, 1)
            .outputItem(() -> Items.STICK, 1)
            .voltage(Voltage.LV)
            .workTicks(32)
            .build();

        // rubber
        DATA_GEN.block(RUBBER_LOG)
            .blockState(ctx -> ctx.provider()
                .axisBlock(ctx.object(),
                    gregtech("blocks/wood/rubber/log_rubber_side"),
                    gregtech("blocks/wood/rubber/log_rubber_top")))
            .tag(List.of(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN))
            .itemTag(List.of(ItemTags.LOGS, ItemTags.LOGS_THAT_BURN))
            .dropSelf()
            .dropOnState(STICKY_RESIN, RubberLogBlock.HAS_RUBBER, true)
            .build()
            .block(RUBBER_LEAVES)
            .blockState(cubeTint("wood/rubber/leaves_rubber"))
            .tag(BlockTags.LEAVES).itemTag(ItemTags.LEAVES)
            .dropSelfOnTool(TOOL_SHEARS)
            .drop(RUBBER_SAPLING, 0.075f)
            .build()
            .block(RUBBER_SAPLING)
            .blockState(ctx -> ctx.provider()
                .simpleBlock(ctx.object(), ctx.provider().models()
                    .cross(ctx.id(), gregtech("blocks/wood/rubber/sapling_rubber"))))
            .itemModel(basicItem(gregtech("blocks/wood/rubber/sapling_rubber")))
            .tag(BlockTags.SAPLINGS).itemTag(ItemTags.SAPLINGS)
            .build();

        // biomass
        EXTRACTOR.recipe(DATA_GEN, ItemTags.LEAVES.location())
            .inputItem(ItemTags.LEAVES, 16)
            .outputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(0.3f))
            .workTicks(128)
            .voltage(Voltage.LV)
            .build()
            .recipe(DATA_GEN, ItemTags.SAPLINGS.location())
            .inputItem(ItemTags.SAPLINGS, 16)
            .outputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(0.1f))
            .workTicks(64)
            .voltage(Voltage.LV)
            .build();

        // nameplate
        ASSEMBLER.recipe(DATA_GEN, Items.NAME_TAG)
            .outputItem(() -> Items.NAME_TAG, 1)
            .inputItem(IRON.tag("plate"), 1)
            .inputItem(TOOL_HANDLE, 1)
            .voltage(Voltage.LV)
            .workTicks(64)
            .requireTech(Technologies.SOLDERING)
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
            .material(CHROME, SHINY)
            .machineProcess(Voltage.MV, 1.5d)
            .blast(Voltage.MV, 2200, 1024, NITROGEN)
            .build()
            .material(ANTIMONY, SHINY)
            .machineProcess(Voltage.LV).smelt()
            .build()
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
            .material(SILICON, METALLIC).build()
            .material(BERYLLIUM, METALLIC)
            .machineProcess(Voltage.LV, 0.6d)
            .smelt()
            .build()
            .material(SODIUM, METALLIC).build()
            .material(POTASSIUM, METALLIC).build()
            .material(CALCIUM, METALLIC).build()
            .material(LITHIUM, DULL).build()
            .material(TITANIUM, METALLIC)
            .machineProcess(Voltage.HV, 1.25d)
            .blast(Voltage.HV, 2000, 960, NITROGEN)
            .build()
            .material(NEODYMIUM, METALLIC)
            .machineProcess(Voltage.HV)
            .blast(Voltage.MV, 1300, 2400)
            .build();
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
            .centrifuge(Voltage.LV, COBALT, 1, ARSENIC, 1, SULFUR, 1)
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
            .blastFrom(Voltage.ULV, 1000, 1000, IRON)
            .blastFrom(Voltage.MV, 1000, 96, WROUGHT_IRON, OXYGEN)
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
            .alloy(Voltage.LV, COPPER, 3, ZINC, 1)
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
            .build()
            .material(STAINLESS_STEEL, SHINY)
            .machineProcess(Voltage.MV)
            .blast(Voltage.MV, 1700, 1100)
            .mix(Voltage.MV, IRON, 6, NICKEL, 1, MANGANESE, 1, CHROME, 1)
            .build()
            .material(SODIUM_CHLORIDE, FINE).build()
            .material(POTASSIUM_CHLORIDE, FINE).build()
            .material(MAGNESIUM_CHLORIDE, DULL).build()
            .material(CALCIUM_CHLORIDE, FINE).build()
            .material(LITHIUM_CHLORIDE, FINE).build()
            .material(AMMONIUM_CHLORIDE, DULL).build()
            .material(SODIUM_CARBONATE, DULL).build()
            .material(POTASSIUM_CARBONATE, DULL).build()
            .material(CALCIUM_CARBONATE, DULL).build()
            .material(LITHIUM_CARBONATE, DULL).build()
            .material(SODIUM_SULFATE, DULL).build()
            .material(POTASSIUM_NITRATE, FINE).build()
            .material(SODIUM_HYDROXIDE, DULL).build()
            .material(CALCIUM_HYDROXIDE, DULL).build()
            .material(SULFURIC_ACID, DULL)
            .fluidMix(Voltage.MV, "dilute", SULFURIC_ACID, 1, WATER, 1)
            .build()
            .material(NICKEL_ZINC_FERRITE, METALLIC)
            .machineProcess(Voltage.MV, 1.25d)
            .blast(Voltage.MV, 1500, 400, OXYGEN, 2f)
            .mix(Voltage.MV, IRON, 4, NICKEL, 1, ZINC, 1)
            .build()
            .material(COKE, LIGNITE)
            .toolProcess()
            .build()
            .material(CHARCOAL, FINE)
            .toolProcess()
            .build()
            .material(SILICON_DIOXIDE, QUARTZ)
            .smelt(GLASS, "primary")
            .build()
            .material(NICHROME, METALLIC)
            .machineProcess(Voltage.MV, 1.25d)
            .blast(Voltage.HV, 2700, 880, NITROGEN)
            .mix(Voltage.MV, NICKEL, 4, CHROME, 1)
            .build()
            .material(ALUMINIUM_OXIDE, FINE).build()
            .material(ANNEALED_COPPER, BRIGHT)
            .machineProcess(Voltage.MV, 0.8d)
            .build();
    }

    private static void higherDegrees() {
        FACTORY.material(COBALT_BRASS, METALLIC)
            .machineProcess(Voltage.LV, 2d).smelt()
            .mix(Voltage.LV, BRASS, 7, ALUMINIUM, 1, COBALT, 1)
            .build()
            .material(SALT_WATER, DULL)
            .fluidMix(Voltage.MV, SODIUM_CHLORIDE, 1, WATER, 1)
            .build()
            .material(PE, DULL)
            .machineProcess(Voltage.LV, 0.5d)
            .build()
            .material(PVC, DULL)
            .machineProcess(Voltage.LV, 0.5d)
            .build()
            .material(LPG, DULL)
            .fluidMix(Voltage.MV, ETHANE, 1, PROPANE, 1)
            .build()
            .material(DIESEL, DULL)
            .fluidMix(Voltage.MV, LIGHT_FUEL, 5, HEAVY_FUEL, 1)
            .build()
            .material(VANADIUM_STEEL, METALLIC)
            .machineProcess(Voltage.MV, 1.5d)
            .blast(Voltage.MV, 2500, 1280, NITROGEN)
            .build()
            .material(PTFE, DULL)
            .machineProcess(Voltage.LV, 0.75d)
            .build();
    }

    private static void ores() {
        // stone generator
        for (var variant : OreVariant.values()) {
            STONE_GENERATOR.recipe(DATA_GEN, variant.baseItem)
                .outputItem(() -> variant.baseItem, 1)
                .voltage(variant == OreVariant.STONE ? Voltage.PRIMITIVE : variant.voltage)
                .build();
        }
        // generate water
        STONE_GENERATOR.recipe(DATA_GEN, WATER.fluidLoc())
            .outputFluid(WATER.fluid(), WATER.fluidAmount(1f))
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
            .oreProcess(MAGNESIUM_CHLORIDE, MAGNESIUM_CHLORIDE, NICKEL)
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
            .centrifuge(Voltage.LV, PYRITE, 6, RUBY, 3, SILICON, 1)
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
            .material(DIAMOND, SHINY)
            .oreBuilder(CARBON).siftAndHammer().build()
            .machineProcess(Voltage.LV, 2d)
            .build()
            .material(BAUXITE, DULL)
            .oreProcess(ALUMINIUM, GALLIUM, RUTILE)
            .build()
            .material(ILMENITE, METALLIC)
            .oreProcess(MANGANESE, MANGANESE, RUTILE)
            .build()
            .material(NATURAL_GAS, DULL).oilOre(192).build()
            .material(LIGHT_OIL, DULL).oilOre(240).build()
            .material(HEAVY_OIL, DULL).oilOre(512).build()
            .material(EMERALD, SHINY)
            .oreBuilder(ALUMINIUM, BERYLLIUM, THORIUM).siftAndHammer().build()
            .machineProcess(Voltage.LV, 2d)
            .build()
            .material(SAPPHIRE, GEM_VERTICAL)
            .oreBuilder(ALUMINIUM, RUTILE, RUTILE).siftAndHammer().build()
            .machineProcess(Voltage.LV, 2d)
            .build()
            .material(TOPAZ, GEM_HORIZONTAL)
            .oreBuilder(BLUE_TOPAZ, ALUMINIUM, BLUE_TOPAZ).build()
            .build()
            .material(BLUE_TOPAZ, GEM_HORIZONTAL)
            .oreBuilder(TOPAZ, ALUMINIUM, TOPAZ).build()
            .build();

        // blast ores
        BLAST_FURNACE.recipe(DATA_GEN, CHALCOPYRITE.loc("dust"))
            .inputItem(CHALCOPYRITE.tag("dust"), 2)
            .inputFluid(OXYGEN.fluid(), OXYGEN.fluidAmount(9f))
            .outputItem(IRON.entry("ingot"), 3)
            .outputItem(COPPER.entry("ingot"), 3)
            .outputFluid(SULFURIC_ACID.fluid("gas"), SULFURIC_ACID.fluidAmount("gas", 6f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(400)
            .build()
            .recipe(DATA_GEN, PYRITE.loc("dust"))
            .inputItem(PYRITE.tag("dust"), 2)
            .inputFluid(OXYGEN.fluid(), OXYGEN.fluidAmount(4.5f))
            .outputItem(IRON.entry("ingot"), 3)
            .outputFluid(SULFURIC_ACID.fluid("gas"), SULFURIC_ACID.fluidAmount("gas", 3f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(400)
            .build()
            .recipe(DATA_GEN, LIMONITE.loc("dust"))
            .inputItem(LIMONITE.tag("dust"), 8)
            .inputItem(CARBON.tag("dust"), 9)
            .outputItem(IRON.entry("ingot"), 12)
            .outputFluid(CARBON_DIOXIDE.fluid(), CARBON_DIOXIDE.fluidAmount(9f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(1600)
            .build()
            .recipe(DATA_GEN, BANDED_IRON.loc("dust"))
            .inputItem(BANDED_IRON.tag("dust"), 8)
            .inputItem(CARBON.tag("dust"), 9)
            .outputItem(IRON.entry("ingot"), 12)
            .outputFluid(CARBON_DIOXIDE.fluid(), CARBON_DIOXIDE.fluidAmount(9f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(1600)
            .build()
            .recipe(DATA_GEN, GARNIERITE.loc("dust"))
            .inputItem(GARNIERITE.tag("dust"), 4)
            .inputItem(CARBON.tag("dust"), 3)
            .outputItem(NICKEL.entry("ingot"), 6)
            .outputFluid(CARBON_DIOXIDE.fluid(), CARBON_DIOXIDE.fluidAmount(3f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(800)
            .build()
            .recipe(DATA_GEN, CASSITERITE.loc("dust"))
            .inputItem(CASSITERITE.tag("dust"), 2)
            .inputItem(CARBON.tag("dust"), 3)
            .outputItem(TIN.entry("ingot"), 3)
            .outputFluid(CARBON_DIOXIDE.fluid(), CARBON_DIOXIDE.fluidAmount(3f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(400)
            .build()
            .recipe(DATA_GEN, GALENA.loc("dust"))
            .inputItem(GALENA.tag("dust"), 2)
            .inputFluid(OXYGEN.fluid(), OXYGEN.fluidAmount(4.5f))
            .outputItem(LEAD.entry("ingot"), 3)
            .outputItem(ANTIMONY.entry("ingot"), 1)
            .outputFluid(SULFURIC_ACID.fluid("gas"), SULFURIC_ACID.fluidAmount("gas", 3f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(400)
            .build()
            .recipe(DATA_GEN, SPHALERITE.loc("dust"))
            .inputItem(SPHALERITE.tag("dust"), 2)
            .inputFluid(OXYGEN.fluid(), OXYGEN.fluidAmount(4.5f))
            .outputItem(ZINC.entry("ingot"), 3)
            .outputItem(SILVER.entry("ingot"), 1)
            .outputFluid(SULFURIC_ACID.fluid("gas"), SULFURIC_ACID.fluidAmount("gas", 3f))
            .voltage(Voltage.LV)
            .temperature(2000)
            .workTicks(400)
            .build();
    }

    private static void crops() {
        crop(Items.WHEAT, Items.WHEAT_SEEDS, true);
        crop(Items.BEETROOT, Items.BEETROOT_SEEDS, true);
        crop(Items.PUMPKIN, Items.PUMPKIN_SEEDS, false);
        crop(Items.MELON, Items.MELON_SEEDS, false);

        crop(Items.CARROT);
        crop(Items.POTATO);
        crop(Items.COCOA_BEANS);
        crop(Items.SUGAR_CANE);
        crop(Items.SWEET_BERRIES);
        crop(Items.CACTUS);
        crop(Items.KELP);
        crop(Items.SEA_PICKLE);
        crop(Items.NETHER_WART);
        crop(Items.CRIMSON_FUNGUS);
        crop(Items.WARPED_FUNGUS);
        crop(Items.GLOW_BERRIES);
        crop(Items.BROWN_MUSHROOM);
        crop(Items.RED_MUSHROOM);

        // cut melon slice
        CUTTER.recipe(DATA_GEN, Items.MELON_SLICE)
            .outputItem(() -> Items.MELON_SLICE, 9)
            .inputItem(() -> Items.MELON, 1)
            .workTicks(128L)
            .voltage(Voltage.LV)
            .build();

        // seed
        cropToSeed(Items.WHEAT, Items.WHEAT_SEEDS);
        cropToSeed(Items.BEETROOT, Items.BEETROOT_SEEDS);
        cropToSeed(Items.PUMPKIN, Items.PUMPKIN_SEEDS, 4, 256);
        cropToSeed(Items.MELON_SLICE, Items.MELON_SEEDS);

        // biomass
        cropToBiomass(Items.WHEAT, 1, 0.1f, 48);
        cropToBiomass(Items.BEETROOT, 1, 0.5f, 48);
        cropToBiomass(Items.CARROT, 2, 0.1f, 96);
        cropToBiomass(Items.POTATO, 2, 0.15f, 64);
        cropToBiomass(Items.MELON, 1, 0.6f, 240);
        cropToBiomass(Items.PUMPKIN, 1, 0.3f, 256);
        cropToBiomass(Items.COCOA_BEANS, 2, 0.1f, 128);
        cropToBiomass(Items.SUGAR_CANE, 1, 0.4f, 48);
        cropToBiomass(Items.SWEET_BERRIES, 1, 0.1f, 32);
        cropToBiomass(Items.CACTUS, 1, 0.1f, 128);
        cropToBiomass(Items.KELP, 2, 0.15f, 64);
        cropToBiomass(Items.SEA_PICKLE, 2, 0.1f, 96);
        cropToBiomass(Items.NETHER_WART, 4, 0.1f, 96);
        cropToBiomass(Items.CRIMSON_FUNGUS, 4, 0.1f, 128);
        cropToBiomass(Items.WARPED_FUNGUS, 4, 0.1f, 128);
        cropToBiomass(Items.GLOW_BERRIES, 2, 0.15f, 64);

        cropToBiomass(Items.WHEAT_SEEDS, 16, 0.1f, 64);
        cropToBiomass(Items.BEETROOT_SEEDS, 16, 0.1f, 64);
        cropToBiomass(Items.MELON_SEEDS, 16, 0.1f, 64);
        cropToBiomass(Items.PUMPKIN_SEEDS, 16, 0.1f, 64);

        EXTRACTOR.recipe(DATA_GEN, Tags.Items.MUSHROOMS.location())
            .inputItem(Tags.Items.MUSHROOMS, 6)
            .outputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(0.1f))
            .workTicks(96)
            .voltage(Voltage.MV)
            .build();
    }

    private static void crop(Item crop, Item seed, boolean outputSeed) {
        var loc = crop.getRegistryName();
        assert loc != null;
        AUTOFARM.recipe(DATA_GEN, loc)
            .inputItem(() -> seed, 1)
            .inputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(0.5f))
            .outputItem(() -> crop, crop == seed ? 3 : 1)
            .transform($ -> outputSeed ? $.outputItem(() -> seed, 2) : $)
            .voltage(Voltage.LV)
            .workTicks(800)
            .build()
            .recipe(DATA_GEN, suffix(loc, "_with_bone_meal"))
            .inputItem(() -> seed, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(0.5f))
            .inputItem(2, () -> Items.BONE_MEAL, 1)
            .outputItem(() -> crop, crop == seed ? 3 : 1)
            .transform($ -> outputSeed ? $.outputItem(() -> seed, 2) : $)
            .voltage(Voltage.LV)
            .workTicks(300)
            .build()
            .recipe(DATA_GEN, suffix(loc, "_with_fertilizer"))
            .inputItem(() -> seed, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(0.5f))
            .inputItem(2, FERTILIZER, 1)
            .outputItem(() -> crop, crop == seed ? 6 : 2)
            .transform($ -> outputSeed ? $.outputItem(() -> seed, 4) : $)
            .voltage(Voltage.MV)
            .workTicks(300)
            .build();
    }

    private static void crop(Item crop) {
        crop(crop, crop, false);
    }

    private static void cropToBiomass(Item crop, int amount, float fluidAmount, long workTicks) {
        EXTRACTOR.recipe(DATA_GEN, crop)
            .inputItem(() -> crop, amount)
            .outputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(fluidAmount))
            .workTicks(workTicks)
            .voltage(Voltage.LV)
            .build();
    }

    private static void cropToSeed(Item crop, Item seed) {
        cropToSeed(crop, seed, 1, 64);
    }

    private static void cropToSeed(Item crop, Item seed, int amount, long workTicks) {
        MACERATOR.recipe(DATA_GEN, crop)
            .inputItem(() -> crop, 1)
            .outputItem(() -> seed, amount)
            .voltage(Voltage.LV)
            .workTicks(workTicks)
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
        disableVanillaOres("emerald", "");

        // smelt wrought iron nugget
        DATA_GEN.vanillaRecipe(() -> SimpleCookingRecipeBuilder
            .smelting(Ingredient.of(IRON.tag("nugget")), WROUGHT_IRON.item("nugget"), 0, 200)
            .unlockedBy("has_material", has(IRON.tag("nugget"))), "_from_iron");

        // stone -> gravel
        TOOL_CRAFTING.recipe(DATA_GEN, Items.GRAVEL)
            .result(Items.GRAVEL, 1)
            .pattern("#").pattern("#")
            .define('#', STONE.tag("block"))
            .toolTag(TOOL_HAMMER)
            .build()
            // gravel -> flint
            .recipe(DATA_GEN, FLINT.loc("primary"))
            .result(FLINT.entry("primary"), 1)
            .pattern("###")
            .define('#', Items.GRAVEL)
            .toolTag(TOOL_HAMMER)
            .build()
            // gravel -> sand
            .recipe(DATA_GEN, Blocks.SAND)
            .result(() -> Blocks.SAND, 1)
            .pattern("#")
            .define('#', Items.GRAVEL)
            .toolTag(TOOL_MORTAR)
            .build();

        generatorRecipes(STEAM_TURBINE, WATER, "gas", 80d, 100);
        generatorRecipes(GAS_TURBINE, METHANE, 80d, 100);
        generatorRecipes(GAS_TURBINE, LPG, 320d, 100);
        generatorRecipes(GAS_TURBINE, REFINERY_GAS, 64d, 100);
        generatorRecipes(GAS_TURBINE, NATURAL_GAS, 40d, 100);
        generatorRecipes(COMBUSTION_GENERATOR, ETHANOL, 160d, 100);
        generatorRecipes(COMBUSTION_GENERATOR, DIESEL, 400d, 125);

        // rubber
        TOOL_CRAFTING.recipe(DATA_GEN, RAW_RUBBER.loc("dust"))
            .result(RAW_RUBBER.entry("dust"), 1)
            .pattern("A").define('A', STICKY_RESIN)
            .toolTag(TOOL_MORTAR)
            .build();

        EXTRACTOR.recipe(DATA_GEN, RAW_RUBBER.loc("dust"))
            .outputItem(RAW_RUBBER.entry("dust"), 3)
            .inputItem(STICKY_RESIN, 1)
            .workTicks(160L)
            .voltage(Voltage.LV)
            .build()
            .recipe(DATA_GEN, suffix(RAW_RUBBER.loc("dust"), "_from_log"))
            .outputItem(RAW_RUBBER.entry("dust"), 1)
            .inputItem(RUBBER_LOG, 1)
            .workTicks(320L)
            .voltage(Voltage.LV)
            .build();

        ALLOY_SMELTER.recipe(DATA_GEN, RUBBER.loc("sheet"))
            .inputItem(RAW_RUBBER.tag("dust"), 3)
            .inputItem(SULFUR.tag("dust"), 1)
            .outputItem(RUBBER.entry("sheet"), 3)
            .workTicks(300)
            .voltage(Voltage.ULV)
            .build();

        // stones
        var sandLoc = Blocks.SAND.getRegistryName();
        assert sandLoc != null;
        MACERATOR.recipe(DATA_GEN, SILICON_DIOXIDE.loc("dust"))
            .inputItem(GLASS.tag("primary"), 1)
            .outputItem(SILICON_DIOXIDE.entry("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(128L)
            .build()
            .recipe(DATA_GEN, suffix(SILICON_DIOXIDE.loc("dust"), "_from_flint"))
            .inputItem(FLINT.tag("primary"), 1)
            .outputItem(SILICON_DIOXIDE.entry("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(128L)
            .build()
            .recipe(DATA_GEN, sandLoc)
            .inputItem(() -> Blocks.GRAVEL, 1)
            .outputItem(() -> Blocks.SAND, 1)
            .voltage(Voltage.LV)
            .workTicks(64L)
            .build()
            .recipe(DATA_GEN, suffix(sandLoc, "_from_sandstone"))
            .inputItem(() -> Blocks.SANDSTONE, 1)
            .outputItem(() -> Blocks.SAND, 4)
            .voltage(Voltage.LV)
            .workTicks(240L)
            .build();

        CENTRIFUGE.recipe(DATA_GEN, sandLoc)
            .inputItem(() -> Blocks.SAND, 1)
            .outputItem(SILICON_DIOXIDE.entry("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(64L)
            .build()
            .recipe(DATA_GEN, STONE.loc("dust"))
            .inputItem(STONE.tag("dust"), 2)
            .outputItem(SILICON_DIOXIDE.entry("dust"), 1)
            .outputItem(CALCIUM_CARBONATE.entry("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(128L)
            .build()
            .recipe(DATA_GEN, STONE.loc("block"))
            .inputItem(STONE.tag("block"), 2)
            .outputItem(() -> Blocks.GRAVEL, 1)
            .outputItem(CALCIUM_CARBONATE.entry("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(240L)
            .build();

        SIFTER.recipe(DATA_GEN, Blocks.GRAVEL)
            .inputItem(() -> Blocks.GRAVEL, 1)
            .outputItem(FLINT.entry("primary"), 1, 0.8)
            .outputItem(FLINT.entry("primary"), 1, 0.35)
            .outputItem(() -> Blocks.SAND, 1, 0.65)
            .voltage(Voltage.LV)
            .workTicks(400L)
            .build();
    }

    private static final String[] VANILLA_METHODS = new String[]{"smelting", "blasting"};

    private static void disableVanillaOres(String name, String suffix) {
        var fullName = name + (suffix.isEmpty() ? "" : "_" + suffix);

        DATA_GEN.nullRecipe(name + "_block");

        if (suffix.equals("ingot")) {
            DATA_GEN.nullRecipe("raw_" + name)
                .nullRecipe("raw_" + name + "_block");
            if (name.equals("copper")) {
                DATA_GEN.nullRecipe(fullName)
                    .nullRecipe(fullName + "_from_waxed_copper_block");
            } else {
                DATA_GEN.nullRecipe(fullName + "_from_" + name + "_block")
                    .nullRecipe(fullName + "_from_nuggets")
                    .nullRecipe(name + "_nugget");
                for (var method : VANILLA_METHODS) {
                    DATA_GEN.nullRecipe(name + "_nugget_from_" + method);
                }
            }
        } else {
            DATA_GEN.nullRecipe(fullName);
        }

        var ores = new ArrayList<>(List.of("", "_deepslate"));
        if (name.equals("gold")) {
            ores.add("_nether");
        }

        for (var method : VANILLA_METHODS) {
            for (var ore : ores) {
                DATA_GEN.nullRecipe(fullName + "_from_" + method + ore + "_" + name + "_ore");
            }
            if (suffix.equals("ingot")) {
                DATA_GEN.nullRecipe(fullName + "_from_" + method + "_raw_" + name);
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

        var planks = ITEMS.getEntry(mcLoc(prefix + "_planks"));
        var logs = mcLoc(prefix + (nether ? "_stems" : "_logs"));
        var logsTag = AllTags.item(logs);
        var wood = prefix + (nether ? "_hyphae" : "_wood");
        var woodStripped = "stripped_" + wood;

        // saw plank
        TOOL_CRAFTING.recipe(DATA_GEN, planks)
            .result(planks, 4)
            .pattern("X")
            .define('X', logsTag)
            .toolTag(TOOL_SAW)
            .build();

        // disable wood and woodStripped recipes
        DATA_GEN.nullRecipe(wood)
            .nullRecipe(woodStripped)
            // reduce vanilla recipe to 2 planks
            .replaceVanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(planks.get(), 2)
                .requires(logsTag)
                .group("planks")
                .unlockedBy("has_logs", has(logsTag)));

        // wood components
        var sign = ITEMS.getEntry(mcLoc(prefix + "_sign"));
        var pressurePlate = ITEMS.getEntry(mcLoc(prefix + "_pressure_plate"));
        var button = ITEMS.getEntry(mcLoc(prefix + "_button"));
        var slab = ITEMS.getEntry(mcLoc(prefix + "_slab"));

        DATA_GEN.nullRecipe(sign.loc())
            .nullRecipe(pressurePlate.loc())
            .nullRecipe(button.loc())
            .nullRecipe(slab.loc());

        TOOL_CRAFTING.recipe(DATA_GEN, slab)
            .result(slab, 1)
            .pattern("#")
            .define('#', planks)
            .toolTag(TOOL_SAW)
            .build()
            .recipe(DATA_GEN, button)
            .result(button, 4)
            .pattern("#")
            .define('#', pressurePlate)
            .toolTag(TOOL_SAW)
            .build();

        CUTTER.recipe(DATA_GEN, planks)
            .outputItem(planks, 6)
            .inputItem(logsTag, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(0.6f))
            .voltage(Voltage.LV)
            .workTicks(240)
            .build()
            .recipe(DATA_GEN, slab)
            .outputItem(slab, 2)
            .inputItem(planks, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(0.1f))
            .voltage(Voltage.LV)
            .workTicks(80)
            .build()
            .recipe(DATA_GEN, button)
            .outputItem(button, 8)
            .inputItem(pressurePlate, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(0.05f))
            .voltage(Voltage.LV)
            .workTicks(64)
            .build();

        ASSEMBLER.recipe(DATA_GEN, sign)
            .outputItem(sign, 1)
            .inputItem(planks, 1)
            .inputItem(TOOL_HANDLE, 1)
            .voltage(Voltage.ULV)
            .workTicks(64)
            .requireTech(Technologies.SOLDERING)
            .build()
            .recipe(DATA_GEN, pressurePlate)
            .outputItem(pressurePlate, 1)
            .inputItem(slab, 1)
            .inputItem(IRON.tag("ring"), 1)
            .inputItem(REDSTONE.tag("dust"), 1)
            .voltage(Voltage.ULV)
            .workTicks(128)
            .requireTech(Technologies.SOLDERING)
            .build();

        // farm
        if (!nether) {
            var sapling = mcLoc(prefix + "_sapling");
            var saplingItem = ITEMS.getEntry(sapling);
            var logItem = ITEMS.getEntry(mcLoc(prefix + "_log"));
            var leavesItem = ITEMS.getEntry(mcLoc(prefix + "_leaves"));
            woodFarmRecipes(sapling, saplingItem, logItem, leavesItem);
        }
    }

    private static void woodFarmRecipes(ResourceLocation loc,
        Supplier<? extends ItemLike> saplingItem,
        Supplier<? extends ItemLike> logItem,
        Supplier<? extends ItemLike> leavesItem) {

        var isRubber = loc.equals(RUBBER_SAPLING.loc());

        AUTOFARM.recipe(DATA_GEN, loc)
            .inputItem(saplingItem, 1)
            .inputFluid(BIOMASS.fluid(), BIOMASS.fluidAmount(1f))
            .outputItem(logItem, 6)
            .transform($ -> isRubber ? $.outputItem(STICKY_RESIN, 6) : $)
            .outputItem(saplingItem, 2)
            .voltage(Voltage.LV)
            .workTicks(1600)
            .build()
            .recipe(DATA_GEN, suffix(loc, "_with_bone_meal"))
            .inputItem(saplingItem, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(1f))
            .inputItem(2, () -> Items.BONE_MEAL, 2)
            .outputItem(logItem, 6)
            .transform($ -> isRubber ? $.outputItem(STICKY_RESIN, 6) : $)
            .outputItem(saplingItem, 2)
            .outputItem(leavesItem, 16)
            .voltage(Voltage.LV)
            .workTicks(300)
            .build()
            .recipe(DATA_GEN, suffix(loc, "_with_fertilizer"))
            .inputItem(saplingItem, 1)
            .inputFluid(WATER.fluid(), WATER.fluidAmount(1f))
            .inputItem(2, FERTILIZER, 2)
            .outputItem(logItem, 12)
            .transform($ -> isRubber ? $.outputItem(STICKY_RESIN, 12) : $)
            .outputItem(saplingItem, 4)
            .outputItem(leavesItem, 32)
            .voltage(Voltage.MV)
            .workTicks(300)
            .build();
    }

    private static void generatorRecipes(IRecipeType<ProcessingRecipe.Builder> type,
        MaterialSet material, String sub, double ratio, long ticks) {
        var start = type == STEAM_TURBINE ? Voltage.ULV : Voltage.LV;
        for (var v : Voltage.between(start, Voltage.HV)) {
            var decay = 1.4d - v.rank * 0.1d;
            var outputAmount = v.value * ticks / ratio;
            var inputAmount = (int) Math.round(outputAmount * decay);
            type.recipe(DATA_GEN, suffix(material.fluidLoc(sub), "_" + v.id))
                .inputFluid(material.fluid(sub), inputAmount)
                .transform($ -> type == STEAM_TURBINE ? $
                    .outputFluid(WATER.fluid(), (int) Math.round(outputAmount)) : $)
                .voltage(v)
                .workTicks(ticks)
                .build();
        }
    }

    private static void generatorRecipes(IRecipeType<ProcessingRecipe.Builder> type,
        MaterialSet material, double ratio, long ticks) {
        generatorRecipes(type, material, "fluid", ratio, ticks);
    }
}
