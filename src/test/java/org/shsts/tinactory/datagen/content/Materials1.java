package org.shsts.tinactory.datagen.content;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.content.AllItems.RUBBER_LEAVES;
import static org.shsts.tinactory.content.AllItems.RUBBER_LOG;
import static org.shsts.tinactory.content.AllItems.RUBBER_SAPLING;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllMaterials.CALCIUM_CARBONATE;
import static org.shsts.tinactory.content.AllMaterials.DIESEL;
import static org.shsts.tinactory.content.AllMaterials.ETHANOL;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.LPG;
import static org.shsts.tinactory.content.AllMaterials.METHANE;
import static org.shsts.tinactory.content.AllMaterials.NATURAL_GAS;
import static org.shsts.tinactory.content.AllMaterials.RAW_RUBBER;
import static org.shsts.tinactory.content.AllMaterials.REFINERY_GAS;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.SILICON_DIOXIDE;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.SULFUR;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllMaterials.WROUGHT_IRON;
import static org.shsts.tinactory.content.AllRecipes.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllRecipes.CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.COMBUSTION_GENERATOR;
import static org.shsts.tinactory.content.AllRecipes.EXTRACTOR;
import static org.shsts.tinactory.content.AllRecipes.GAS_TURBINE;
import static org.shsts.tinactory.content.AllRecipes.MACERATOR;
import static org.shsts.tinactory.content.AllRecipes.SIFTER;
import static org.shsts.tinactory.content.AllRecipes.STEAM_TURBINE;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.FLUID_STORAGE_CELL;
import static org.shsts.tinactory.content.AllTags.ITEM_STORAGE_CELL;
import static org.shsts.tinactory.content.AllTags.STORAGE_CELL;
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
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.cubeTint;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Materials1 {
    public static void init() {
        wood();
        misc();
        tags();
        exportMaterial();
    }

    private static void wood() {
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
    }

    private static void misc() {
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
            .tag(IRON.tag("screw"), TOOL_SCREW)
            .tag(ITEM_STORAGE_CELL, STORAGE_CELL)
            .tag(FLUID_STORAGE_CELL, STORAGE_CELL);
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

    private static String formatColor(int color) {
        return "0x%08X".formatted(color);
    }

    private static void exportMaterial() {
        var gson = new GsonBuilder().setPrettyPrinting().create();
        for (var mat : AllMaterials.SET.values()) {
            var jo = new JsonObject();

            jo.addProperty("color", formatColor(mat.color));

            var items = new JsonArray();
            var aliases = new JsonObject();
            var fluids = new JsonObject();

            for (var sub : mat.itemSubs()) {
                if (mat.isAlias(sub)) {
                    var target = LocHelper.name(mat.tag(sub).location().getPath(), -2);
                    aliases.addProperty(sub, target);
                } else if (!sub.startsWith("tool/")) {
                    items.add(sub);
                }
            }

            for (var sub : mat.fluidSubs()) {
                if (sub.equals("fluid")) {
                    if (mat.fluid().get() != Fluids.WATER) {
                        var target = LocHelper.name(mat.fluidLoc().getPath(), -2);
                        aliases.addProperty(sub, target);
                    } else {
                        aliases.addProperty(sub, "liquid");
                    }
                } else {
                    var jo1 = new JsonObject();
                    jo1.addProperty("baseAmount", mat.fluidAmount(sub, 1f));

                    var fluid = mat.fluid(sub).get();
                    if (fluid instanceof SimpleFluid simpleFluid) {
                        var attributes = fluid.getAttributes();
                        jo1.addProperty("texture", attributes.getStillTexture().toString());
                        if (attributes.getColor() != mat.color) {
                            jo1.addProperty("textureColor", formatColor(attributes.getColor()));
                        }
                        if (simpleFluid.displayColor != mat.color) {
                            jo1.addProperty("displayColor", formatColor(simpleFluid.displayColor));
                        }
                    } else {
                        var loc = mat.fluidLoc(sub);
                        jo1.addProperty("existing", loc.toString());
                    }
                    fluids.add(sub, jo1);
                }
            }

            if (mat.hasBlock("ore")) {
                jo.addProperty("ore", mat.oreVariant().getName());
            }

            jo.add("items", items);
            jo.add("fluids", fluids);
            jo.add("aliases", aliases);

            var path = Paths.get("materials", mat.name + ".json");
            try (var is = Files.newOutputStream(path);
                var writer = new OutputStreamWriter(is)) {
                gson.toJson(jo, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
