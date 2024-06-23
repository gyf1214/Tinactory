package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.datagen.content.builder.MaterialBuilder;
import org.shsts.tinactory.datagen.content.model.IconSet;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.content.AllItems.RUBBER_LEAVES;
import static org.shsts.tinactory.content.AllItems.RUBBER_LOG;
import static org.shsts.tinactory.content.AllItems.RUBBER_SAPLING;
import static org.shsts.tinactory.content.AllItems.STEAM;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BANDED_IRON;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CASSITERITE;
import static org.shsts.tinactory.content.AllMaterials.CHALCOPYRITE;
import static org.shsts.tinactory.content.AllMaterials.CINNABAR;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.LIMONITE;
import static org.shsts.tinactory.content.AllMaterials.MAGNETITE;
import static org.shsts.tinactory.content.AllMaterials.NICKEL;
import static org.shsts.tinactory.content.AllMaterials.PYRITE;
import static org.shsts.tinactory.content.AllMaterials.RAW_RUBBER;
import static org.shsts.tinactory.content.AllMaterials.REDSTONE;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.TEST;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
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
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.cubeTint;
import static org.shsts.tinactory.datagen.content.model.IconSet.DULL;
import static org.shsts.tinactory.datagen.content.model.IconSet.METALLIC;
import static org.shsts.tinactory.datagen.content.model.IconSet.ROUGH;
import static org.shsts.tinactory.datagen.content.model.IconSet.SHINY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Materials {
    public static void init() {
        wood();
        elements();
        firstDegrees();
        ores();
        misc();
        tags();
    }

    private static class MaterialFactory {
        public MaterialBuilder<MaterialFactory>
        material(MaterialSet material, IconSet icon) {
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
        DATA_GEN.nullRecipe(Items.WOODEN_AXE)
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
        TOOL_CRAFTING.recipe(DATA_GEN, Items.STICK)
                .result(Items.STICK, 4)
                .pattern("#").pattern("#")
                .define('#', ItemTags.PLANKS)
                .toolTag(TOOL_SAW)
                .build();
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(Items.STICK, 2)
                .define('#', ItemTags.PLANKS)
                .pattern("#").pattern("#")
                .unlockedBy("has_planks", has(ItemTags.PLANKS)));

        // rubber
        DATA_GEN.block(RUBBER_LOG)
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
                .itemModel(basicItem(gregtech("blocks/wood/rubber/sapling_rubber")))
                .tag(BlockTags.SAPLINGS).itemTag(ItemTags.SAPLINGS)
                .build();
    }

    private static void elements() {
        FACTORY.material(IRON, METALLIC)
                .toolProcess().smelt()
                .build()
                .material(GOLD, SHINY)
                .toolProcess().smelt()
                // TODO: ?
                .oreProcess()
                .build()
                .material(COPPER, SHINY)
                .toolProcess().smelt()
                .build()
                .material(TIN, DULL)
                .toolProcess().smelt()
                // TODO: tin, zinc
                .oreProcess()
                .build()
                .material(SULFUR, DULL)
                .build()
                .material(NICKEL, METALLIC)
                .toolProcess().smelt()
                .build()
                .material(ALUMINIUM, DULL)
                .build();
    }

    private static void firstDegrees() {
        FACTORY.material(WROUGHT_IRON, METALLIC)
                .toolProcess().smelt()
                .build()
                .material(BRONZE, METALLIC)
                .toolProcess().smelt()
                .alloy(Voltage.ULV, COPPER, 3, TIN, 1)
                .build()
                .material(INVAR, METALLIC)
                .toolProcess().smelt()
                .alloy(Voltage.ULV, IRON, 2, NICKEL, 1)
                .build()
                .material(CUPRONICKEL, METALLIC)
                .toolProcess().smelt()
                .alloy(Voltage.ULV, COPPER, 1, NICKEL, 1)
                .build()
                .material(STEEL, METALLIC)
                .toolProcess()
                .build();
    }

    private static void ores() {
        FACTORY.material(CHALCOPYRITE, DULL)
                // TODO: sulfur, cobaltite
                .primitiveOreProcess(SULFUR).smelt(COPPER)
                .build()
                .material(PYRITE, ROUGH)
                // TODO: sulfur, cadmium
                .primitiveOreProcess(SULFUR).smelt(IRON)
                .build()
                .material(LIMONITE, METALLIC)
                // TODO: nickel
                .oreProcess(NICKEL).smelt(IRON)
                .build()
                .material(BANDED_IRON, DULL)
                // TODO: magnetite, magnesium?
                .oreProcess(MAGNETITE).smelt(IRON)
                .build()
                .material(COAL, DULL)
                // TODO: coal, thorium
                .oreProcess(2)
                .build()
                .material(CASSITERITE, METALLIC)
                // TODO: tin, zinc
                .oreProcess(TIN).smelt(TIN)
                .build()
                .material(REDSTONE, DULL)
                // TODO: cinnabar, glowstone, rare earth?
                .oreProcess(5)
                .build()
                .material(CINNABAR, SHINY)
                // TODO: glowstone, rare earth?
                .oreProcess()
                .build()
                .material(RUBY, IconSet.RUBY)
                // TODO: cinnabar, chromium, sifting?
                .oreProcess(CINNABAR)
                .build()
                .material(MAGNETITE, METALLIC)
                // TODO: ?
                .oreProcess(GOLD)
                .smelt(IRON)
                .build();
    }

    private static void misc() {
        FACTORY.material(TEST, DULL).build()
                .material(STONE, ROUGH).toolProcess().build()
                .material(FLINT, DULL).toolProcess().build()
                .material(RAW_RUBBER, DULL).build()
                .material(RUBBER, SHINY)
                .toolProcess()
                .build();

        // smelt wrought iron nugget
        DATA_GEN.vanillaRecipe(() -> SimpleCookingRecipeBuilder
                .smelting(Ingredient.of(IRON.tag("nugget")), WROUGHT_IRON.item("nugget"), 0, 200)
                .unlockedBy("has_material", has(IRON.tag("nugget"))), "_from_iron");

        // generate cobblestone
        STONE_GENERATOR.recipe(DATA_GEN, Items.COBBLESTONE)
                .outputItem(0, Items.COBBLESTONE, 1)
                .primitive()
                .build()
                // generate water
                .recipe(DATA_GEN, Fluids.WATER)
                .outputFluid(1, Fluids.WATER, 1000)
                .voltage(Voltage.ULV)
                .build();

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
                .recipe(DATA_GEN, Items.SAND)
                .result(Items.SAND, 1)
                .pattern("#")
                .define('#', Items.GRAVEL)
                .toolTag(TOOL_MORTAR)
                .build();

        // generate steam
        for (var voltage : Voltage.between(Voltage.ULV, Voltage.HV)) {
            var consume = (int) voltage.value / 8 * (14 - voltage.rank);
            STEAM_TURBINE.recipe(DATA_GEN, voltage.id)
                    .voltage(voltage)
                    .inputFluid(0, STEAM, consume)
                    .outputFluid(1, Fluids.WATER, (int) voltage.value / 8 * 5)
                    .build();
        }

        // rubber
        TOOL_CRAFTING.recipe(DATA_GEN, RAW_RUBBER.loc("dust"))
                .result(RAW_RUBBER.entry("dust"), 1)
                .pattern("A").define('A', STICKY_RESIN)
                .toolTag(TOOL_MORTAR)
                .build();

        ALLOY_SMELTER.recipe(DATA_GEN, RUBBER.loc("bar"))
                .inputItem(0, RAW_RUBBER.entry("dust"), 3)
                .inputItem(0, SULFUR.entry("dust"), 1)
                .outputItem(1, RUBBER.entry("bar"), 3)
                .build();

        // TEST
        BLAST_FURNACE.recipe(DATA_GEN, STEEL.loc("ingot"))
                .inputItem(0, IRON.tag("dust"), 1)
                .outputItem(2, STEEL.entry("ingot"), 1)
                .voltage(Voltage.LV)
                .power(32)
                .workTicks(200)
                .build();
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

        var planks = Tinactory.REGISTRATE.itemHandler.getEntry(prefix + "_planks");
        var logTag = AllTags.item(prefix + (nether ? "_stems" : "_logs"));
        var wood = prefix + (nether ? "_hyphae" : "_wood");
        var woodStripped = "stripped_" + wood;

        // saw plank
        TOOL_CRAFTING.recipe(DATA_GEN, planks.loc)
                .result(planks, 4)
                .pattern("X")
                .define('X', logTag)
                .toolTag(TOOL_SAW)
                .build();
        // disable wood and woodStripped recipes
        DATA_GEN.nullRecipe(wood)
                .nullRecipe(woodStripped)
                // reduce vanilla recipe to 2 planks
                .vanillaRecipe(() -> ShapelessRecipeBuilder
                        .shapeless(planks.get(), 2)
                        .requires(logTag)
                        .group("planks")
                        .unlockedBy("has_logs", has(logTag)));
    }
}
