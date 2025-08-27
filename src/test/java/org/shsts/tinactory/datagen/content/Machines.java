package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder1;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.Map;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllBlockEntities.ARC_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.BATTERY_BOX;
import static org.shsts.tinactory.content.AllBlockEntities.BENDER;
import static org.shsts.tinactory.content.AllBlockEntities.CENTRIFUGE;
import static org.shsts.tinactory.content.AllBlockEntities.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllBlockEntities.CIRCUIT_ASSEMBLER;
import static org.shsts.tinactory.content.AllBlockEntities.COMBUSTION_GENERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.CUTTER;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_TANK;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTROLYZER;
import static org.shsts.tinactory.content.AllBlockEntities.EXTRACTOR;
import static org.shsts.tinactory.content.AllBlockEntities.EXTRUDER;
import static org.shsts.tinactory.content.AllBlockEntities.FLUID_SOLIDIFIER;
import static org.shsts.tinactory.content.AllBlockEntities.GAS_TURBINE;
import static org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.LASER_ENGRAVER;
import static org.shsts.tinactory.content.AllBlockEntities.LATHE;
import static org.shsts.tinactory.content.AllBlockEntities.LOGISTIC_WORKER;
import static org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.MACERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.MIXER;
import static org.shsts.tinactory.content.AllBlockEntities.MULTIBLOCK_INTERFACE;
import static org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER;
import static org.shsts.tinactory.content.AllBlockEntities.ORE_ANALYZER;
import static org.shsts.tinactory.content.AllBlockEntities.ORE_WASHER;
import static org.shsts.tinactory.content.AllBlockEntities.POLARIZER;
import static org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_ANALYZER;
import static org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_WASHER;
import static org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_STONE_GENERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.RESEARCH_BENCH;
import static org.shsts.tinactory.content.AllBlockEntities.STEAM_TURBINE;
import static org.shsts.tinactory.content.AllBlockEntities.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.THERMAL_CENTRIFUGE;
import static org.shsts.tinactory.content.AllBlockEntities.WIREMILL;
import static org.shsts.tinactory.content.AllBlockEntities.WORKBENCH;
import static org.shsts.tinactory.content.AllItems.BUZZSAW;
import static org.shsts.tinactory.content.AllItems.CABLE;
import static org.shsts.tinactory.content.AllItems.CHIPS;
import static org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_BUFFER;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP;
import static org.shsts.tinactory.content.AllItems.EMITTER;
import static org.shsts.tinactory.content.AllItems.GRINDER;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.ROBOT_ARM;
import static org.shsts.tinactory.content.AllItems.SENSOR;
import static org.shsts.tinactory.content.AllItems.TRANSFORMER;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.GRAPHITE;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.STAINLESS_STEEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMultiblocks.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllMultiblocks.HEATPROOF_CASING;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;
import static org.shsts.tinactory.content.AllTags.circuit;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Machines {
    private static final long MACHINE_TICKS = 200;

    public static void init() {
        primitiveRecipes();
        ulvRecipes();
        basicRecipes();
        miscRecipes();
    }

    private static void primitiveRecipes() {
        // workbench
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(WORKBENCH.get())
                .pattern("WSW")
                .pattern("SCS")
                .pattern("WSW")
                .define('S', STONE.tag("block"))
                .define('W', Items.STICK)
                .define('C', Blocks.CRAFTING_TABLE)
                .unlockedBy("has_cobblestone", has(STONE.tag("block"))))
            // primitive stone generator
            .vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(PRIMITIVE_STONE_GENERATOR.get())
                .pattern("WLW")
                .pattern("L L")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS)))
            // primitive ore analyzer
            .vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(PRIMITIVE_ORE_ANALYZER.get())
                .pattern("WLW")
                .pattern("LFL")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .define('F', FLINT.tag("primary"))
                .unlockedBy("has_flint", has(FLINT.tag("primary"))))
            // primitive ore washer
            .vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(PRIMITIVE_ORE_WASHER.get())
                .pattern("WLW")
                .pattern("LFL")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .define('F', Items.WATER_BUCKET)
                .unlockedBy("has_water_bucket", has(Items.WATER_BUCKET)));
    }

    private static void ulvRecipes() {
        ulvFromPrimitive(STONE_GENERATOR, PRIMITIVE_STONE_GENERATOR);
        ulvFromPrimitive(ORE_ANALYZER, PRIMITIVE_ORE_ANALYZER);
        ulvFromPrimitive(ORE_WASHER, PRIMITIVE_ORE_WASHER);
        ulvMachine(RESEARCH_BENCH.entry(Voltage.ULV), () -> Blocks.CRAFTING_TABLE);
        ulvMachine(AllBlockEntities.ASSEMBLER.entry(Voltage.ULV), WORKBENCH);
        ulvMachine(ELECTRIC_FURNACE.entry(Voltage.ULV), () -> Blocks.FURNACE);
        ulvMachine(ELECTRIC_CHEST.entry(Voltage.ULV), () -> Blocks.CHEST);
        ulvMachine(ELECTRIC_TANK.entry(Voltage.ULV), GLASS.tag("primary"));
        ulvMachine(LOGISTIC_WORKER.entry(Voltage.ULV), () -> Blocks.HOPPER);
        ulvMachine(ELECTRIC_BUFFER.get(Voltage.ULV), CABLE.get(Voltage.ULV));

        TOOL_CRAFTING.recipe(DATA_GEN, NETWORK_CONTROLLER)
            .result(NETWORK_CONTROLLER, 1)
            .pattern("VWV").pattern("VHV").pattern("WVW")
            .define('W', CABLE.get(Voltage.ULV))
            .define('H', MACHINE_HULL.get(Voltage.ULV))
            .define('V', circuit(Voltage.ULV))
            .toolTag(TOOL_WRENCH)
            .build()
            .recipe(DATA_GEN, STEAM_TURBINE.entry(Voltage.ULV))
            .result(STEAM_TURBINE.entry(Voltage.ULV), 1)
            .pattern("PVP").pattern("RHR").pattern("WVW")
            .define('P', COPPER.tag("pipe"))
            .define('R', IRON.tag("rotor"))
            .define('W', CABLE.get(Voltage.ULV))
            .define('H', MACHINE_HULL.get(Voltage.ULV))
            .define('V', circuit(Voltage.ULV))
            .toolTag(TOOL_WRENCH)
            .build();

        ASSEMBLER.recipe(DATA_GEN, ALLOY_SMELTER.entry(Voltage.ULV))
            .outputItem(ALLOY_SMELTER.entry(Voltage.ULV), 1)
            .inputItem(ELECTRIC_FURNACE.entry(Voltage.ULV), 1)
            .inputItem(circuit(Voltage.ULV), 2)
            .inputItem(CABLE.get(Voltage.ULV), 4)
            .requireTech(Technologies.ALLOY_SMELTING)
            .voltage(Voltage.ULV)
            .workTicks(MACHINE_TICKS)
            .build()
            .recipe(DATA_GEN, BLAST_FURNACE)
            .outputItem(BLAST_FURNACE, 1)
            .inputItem(HEATPROOF_CASING, 1)
            .inputItem(ELECTRIC_FURNACE.entry(Voltage.ULV), 3)
            .inputItem(circuit(Voltage.ULV), 3)
            .inputItem(CABLE.get(Voltage.ULV), 2)
            .requireTech(Technologies.STEEL)
            .voltage(Voltage.ULV)
            .workTicks(MACHINE_TICKS)
            .build()
            .recipe(DATA_GEN, MULTIBLOCK_INTERFACE.get(Voltage.ULV))
            .outputItem(MULTIBLOCK_INTERFACE.get(Voltage.ULV), 1)
            .inputItem(MACHINE_HULL.get(Voltage.ULV), 1)
            .inputItem(circuit(Voltage.ULV), 2)
            .inputItem(CABLE.get(Voltage.ULV), 2)
            .inputItem(() -> Blocks.CHEST, 1)
            .inputItem(GLASS.tag("primary"), 1)
            .voltage(Voltage.ULV)
            .workTicks(MACHINE_TICKS)
            .requireTech(Technologies.STEEL)
            .build();
    }

    private static void basicRecipes() {
        machineRecipe(Voltage.LV, STEEL, COPPER, TIN, BRONZE, TIN);
        machineRecipe(Voltage.MV, ALUMINIUM, CUPRONICKEL, COPPER, BRASS, BRONZE);
        machineRecipe(Voltage.HV, STAINLESS_STEEL, KANTHAL, SILVER, STAINLESS_STEEL, STEEL);
    }

    private static void miscRecipes() {
        TOOL_CRAFTING.recipe(DATA_GEN, LOW_PRESSURE_BOILER)
            .result(LOW_PRESSURE_BOILER, 1)
            .pattern("PPP").pattern("PWP").pattern("VFV")
            .define('P', IRON.tag("plate"))
            .define('W', CABLE.get(Voltage.ULV))
            .define('V', circuit(Voltage.ULV))
            .define('F', Blocks.FURNACE.asItem())
            .toolTag(TOOL_WRENCH)
            .build();

        ASSEMBLER.recipe(DATA_GEN, HIGH_PRESSURE_BOILER)
            .outputItem(HIGH_PRESSURE_BOILER, 1)
            .inputItem(MACHINE_HULL.get(Voltage.MV), 1)
            .inputItem(() -> Blocks.FURNACE, 1)
            .inputItem(BRASS.tag("pipe"), 2)
            .inputItem(IRON.tag("plate"), 4)
            .voltage(Voltage.LV)
            .workTicks(MACHINE_TICKS)
            .requireTech(Technologies.SOLDERING, Technologies.STEEL)
            .build();

        // disable vanilla recipes
        DATA_GEN.nullRecipe(Items.BLAST_FURNACE)
            .nullRecipe(Items.SMOKER)
            .nullRecipe(Items.STONECUTTER)
            .nullRecipe(Items.FLETCHING_TABLE)
            .nullRecipe(Items.CARTOGRAPHY_TABLE)
            .nullRecipe(Items.GRINDSTONE)
            .nullRecipe(Items.CAMPFIRE)
            .nullRecipe(Items.SOUL_CAMPFIRE)
            .nullRecipe(Items.ENCHANTING_TABLE)
            .nullRecipe(Items.ANVIL)
            .nullRecipe(Items.SMITHING_TABLE)
            .nullRecipe(Items.TARGET)
            .nullRecipe(Items.NOTE_BLOCK)
            .nullRecipe(Items.JUKEBOX)
            .nullRecipe(Items.CAULDRON)
            .nullRecipe(Items.RESPAWN_ANCHOR)
            .nullRecipe(Items.GLOWSTONE)
            .nullRecipe(Items.BUCKET)
            .nullRecipe(Items.SHEARS)
            .nullRecipe(Items.FLINT_AND_STEEL)
            .nullRecipe(Items.SPYGLASS)
            .nullRecipe(Items.COMPASS)
            .nullRecipe(Items.CROSSBOW)
            .nullRecipe(Items.CLOCK)
            .nullRecipe(Items.PISTON)
            .nullRecipe(Items.STICKY_PISTON)
            .nullRecipe(Items.DISPENSER)
            .nullRecipe(Items.DROPPER)
            .nullRecipe(Items.DAYLIGHT_DETECTOR)
            .nullRecipe(Items.TRIPWIRE_HOOK)
            .nullRecipe(Items.TRAPPED_CHEST)
            .nullRecipe(Items.HOPPER)
            .nullRecipe(Items.REDSTONE_TORCH)
            .nullRecipe(Items.REPEATER)
            .nullRecipe(Items.COMPARATOR)
            .nullRecipe(Items.REDSTONE_LAMP)
            .nullRecipe(Items.OBSERVER)
            .nullRecipe(Items.MINECART)
            .nullRecipe(Items.CHEST_MINECART)
            .nullRecipe(Items.TNT_MINECART)
            .nullRecipe(Items.HOPPER_MINECART)
            .nullRecipe(Items.FURNACE_MINECART)
            .nullRecipe(Items.RAIL)
            .nullRecipe(Items.POWERED_RAIL)
            .nullRecipe(Items.DETECTOR_RAIL)
            .nullRecipe(Items.ACTIVATOR_RAIL)
            .nullRecipe(Items.STONE_PRESSURE_PLATE)
            .nullRecipe(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE)
            .nullRecipe(Items.LIGHT_WEIGHTED_PRESSURE_PLATE)
            .nullRecipe(Items.HEAVY_WEIGHTED_PRESSURE_PLATE)
            .nullRecipe(Items.STONE_BUTTON)
            .nullRecipe(Items.POLISHED_BLACKSTONE_BUTTON);

        TOOL_CRAFTING.recipe(DATA_GEN, Items.HOPPER)
            .result(Items.HOPPER, 1)
            .pattern("P P").pattern("PCP").pattern(" P ")
            .define('P', IRON.tag("plate"))
            .define('C', Items.CHEST)
            .toolTag(TOOL_WRENCH, TOOL_HAMMER)
            .build()
            .recipe(DATA_GEN, Items.BUCKET)
            .result(Items.BUCKET, 1)
            .pattern("P P").pattern(" P ")
            .define('P', IRON.tag("plate"))
            .toolTag(TOOL_HAMMER)
            .build();
    }

    private static void ulvMachine(IEntry<? extends ItemLike> result,
        Supplier<? extends ItemLike> base) {
        TOOL_CRAFTING.recipe(DATA_GEN, result)
            .result(result, 1)
            .pattern("BBB").pattern("VHV").pattern("WVW")
            .define('B', base)
            .define('W', CABLE.get(Voltage.ULV))
            .define('H', MACHINE_HULL.get(Voltage.ULV))
            .define('V', circuit(Voltage.ULV))
            .toolTag(TOOL_WRENCH)
            .build();
    }

    private static void ulvMachine(IEntry<? extends ItemLike> result,
        TagKey<Item> base) {
        TOOL_CRAFTING.recipe(DATA_GEN, result)
            .result(result, 1)
            .pattern("BBB").pattern("VHV").pattern("WVW")
            .define('B', base)
            .define('W', CABLE.get(Voltage.ULV))
            .define('H', MACHINE_HULL.get(Voltage.ULV))
            .define('V', circuit(Voltage.ULV))
            .toolTag(TOOL_WRENCH)
            .build();
    }

    private static void ulvFromPrimitive(MachineSet set, IEntry<? extends Block> primitive) {
        ulvMachine(set.entry(Voltage.ULV), primitive);
    }

    private record RecipeFactory(Voltage voltage) {
        private AssemblyRecipeBuilder1<RecipeFactory> recipe(
            IEntry<? extends ItemLike> item, Voltage v1) {
            var builder = ASSEMBLER.recipe(DATA_GEN, item)
                .outputItem(item, 1)
                .voltage(v1)
                .workTicks(MACHINE_TICKS)
                .inputItem(MACHINE_HULL.get(voltage), 1);
            return new AssemblyRecipeBuilder1<>(this, voltage, builder) {
                private int components = 0;
                private boolean hasCable = false;

                @Override
                public AssemblyRecipeBuilder1<RecipeFactory> component(
                    Map<Voltage, ? extends Supplier<? extends ItemLike>> component, int count) {
                    if (component != CABLE) {
                        components += count;
                    } else {
                        hasCable = true;
                    }
                    return super.component(component, count);
                }

                @Override
                protected Unit createObject() {
                    if (!hasCable) {
                        component(CABLE, Math.max(2, components * 2));
                    }
                    return super.createObject();
                }
            };
        }

        public AssemblyRecipeBuilder1<RecipeFactory> recipe(MachineSet set, Voltage v1) {
            if (!set.hasVoltage(voltage)) {
                return new AssemblyRecipeBuilder1<>(this);
            }
            return recipe(set.entry(voltage), v1);
        }

        public AssemblyRecipeBuilder1<RecipeFactory> recipe(
            Map<Voltage, ? extends IEntry<? extends ItemLike>> set, Voltage v1) {
            if (!set.containsKey(voltage)) {
                return new AssemblyRecipeBuilder1<>(this);
            }
            return recipe(set.get(voltage), v1);
        }

        public AssemblyRecipeBuilder1<RecipeFactory> recipe(MachineSet set) {
            return recipe(set, Voltage.fromRank(voltage.rank - 1));
        }

        public AssemblyRecipeBuilder1<RecipeFactory> recipe(
            Map<Voltage, ? extends IEntry<? extends ItemLike>> set) {
            return recipe(set, Voltage.fromRank(voltage.rank - 1));
        }
    }

    private static void machineRecipe(Voltage v, MaterialSet main,
        MaterialSet heat, MaterialSet electric,
        MaterialSet pipe, MaterialSet rotor) {
        var factory = new RecipeFactory(v);
        var wireNumber = 4 * v.rank;

        factory.recipe(RESEARCH_BENCH)
            .circuit(2)
            .component(SENSOR, 1)
            .component(EMITTER, 1)
            .tech(Technologies.SENSOR_AND_EMITTER)
            .build()
            .recipe(AllBlockEntities.ASSEMBLER)
            .circuit(2)
            .component(ROBOT_ARM, 2)
            .component(CONVEYOR_MODULE, 2)
            .tech(Technologies.ROBOT_ARM, Technologies.CONVEYOR_MODULE)
            .build()
            .recipe(LASER_ENGRAVER)
            .circuit(3)
            .component(ELECTRIC_PISTON, 2)
            .component(EMITTER, 1)
            .tech(Technologies.INTEGRATED_CIRCUIT)
            .build()
            .recipe(CIRCUIT_ASSEMBLER)
            .circuit(Voltage.fromRank(v.rank + 1), 4)
            .component(ROBOT_ARM, 1)
            .component(EMITTER, 1)
            .component(CONVEYOR_MODULE, 2)
            .tech(Technologies.INTEGRATED_CIRCUIT)
            .build()
            .recipe(STONE_GENERATOR)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 1)
            .component(ELECTRIC_PISTON, 1)
            .component(GRINDER, 1)
            .material(GLASS, "primary", 1)
            .tech(Technologies.PUMP_AND_PISTON, Technologies.MATERIAL_CUTTING)
            .build()
            .recipe(ORE_ANALYZER)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 3)
            .component(SENSOR, 1)
            .tech(Technologies.SENSOR_AND_EMITTER)
            .build()
            .recipe(MACERATOR)
            .circuit(3)
            .component(ELECTRIC_PISTON, 1)
            .component(CONVEYOR_MODULE, 1)
            .component(GRINDER, 1)
            .tech(Technologies.CONVEYOR_MODULE, Technologies.MATERIAL_CUTTING)
            .build()
            .recipe(ORE_WASHER)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 1)
            .material(rotor, "rotor", 2)
            .material(GLASS, "primary", 1)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(CENTRIFUGE)
            .circuit(4)
            .component(ELECTRIC_MOTOR, 2)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(THERMAL_CENTRIFUGE)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 2)
            .material(heat, "wire", wireNumber)
            .tech(Technologies.MOTOR, Technologies.ELECTRIC_HEATING)
            .build()
            .recipe(ELECTRIC_FURNACE)
            .circuit(2)
            .material(heat, "wire", wireNumber)
            .material(main, "plate", 4)
            .tech(Technologies.ELECTRIC_HEATING)
            .build()
            .recipe(ALLOY_SMELTER)
            .circuit(4)
            .material(heat, "wire", wireNumber * 2)
            .material(main, "plate", 8)
            .tech(Technologies.ELECTRIC_HEATING)
            .build()
            .recipe(MIXER)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 1)
            .material(rotor, "rotor", 1)
            .material(GLASS, "primary", 4)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(POLARIZER)
            .circuit(2)
            .material(electric, "wire", wireNumber)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(WIREMILL)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 4)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(BENDER)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 2)
            .component(ELECTRIC_PISTON, 2)
            .material(main, "plate", 4)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(LATHE)
            .circuit(3)
            .component(ELECTRIC_MOTOR, 1)
            .component(ELECTRIC_PISTON, 1)
            .component(GRINDER, 1)
            .tech(Technologies.PUMP_AND_PISTON, Technologies.MATERIAL_CUTTING)
            .build()
            .recipe(CUTTER)
            .circuit(3)
            .component(ELECTRIC_MOTOR, 1)
            .component(CONVEYOR_MODULE, 1)
            .component(BUZZSAW, 1)
            .tech(Technologies.CONVEYOR_MODULE, Technologies.MATERIAL_CUTTING)
            .build()
            .recipe(EXTRUDER)
            .circuit(4)
            .component(ELECTRIC_PISTON, 1)
            .material(heat, "wire", wireNumber)
            .material(pipe, "pipe", 1)
            .tech(Technologies.COLD_WORKING)
            .build()
            .recipe(EXTRACTOR)
            .circuit(2)
            .component(ELECTRIC_PISTON, 1)
            .component(ELECTRIC_PUMP, 1)
            .material(heat, "wire", wireNumber)
            .material(GLASS, "primary", 2)
            .tech(Technologies.HOT_WORKING)
            .build()
            .recipe(FLUID_SOLIDIFIER)
            .circuit(2)
            .component(ELECTRIC_PUMP, 2)
            .material(GLASS, "primary", 2)
            .tech(Technologies.HOT_WORKING)
            .build()
            .recipe(ELECTROLYZER)
            .circuit(4)
            .material(electric, "wire", wireNumber * 2)
            .material(GLASS, "primary", 1)
            .tech(Technologies.ELECTROLYZING)
            .build()
            .recipe(CHEMICAL_REACTOR)
            .circuit(4)
            .component(ELECTRIC_MOTOR, 2)
            .material(rotor, "rotor", 2)
            .material(GLASS, "primary", 2)
            .tech(Technologies.CHEMISTRY)
            .build()
            .recipe(ARC_FURNACE)
            .circuit(2)
            .material(electric, "wire", wireNumber * 4)
            .material(GRAPHITE, "dust", 4)
            .material(main, "plate", 4)
            .tech(Technologies.ARC_FURNACE)
            .build()
            .recipe(STEAM_TURBINE)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 2)
            .material(rotor, "rotor", 2)
            .material(pipe, "pipe", 2)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(GAS_TURBINE)
            .circuit(3)
            .component(ELECTRIC_MOTOR, 1)
            .component(ELECTRIC_PUMP, 1)
            .material(rotor, "rotor", 2)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(COMBUSTION_GENERATOR)
            .circuit(3)
            .component(ELECTRIC_MOTOR, 1)
            .component(ELECTRIC_PISTON, 1)
            .material(main, "gear", 2)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(ELECTRIC_CHEST)
            .circuit(2)
            .component(CONVEYOR_MODULE, 1)
            .material(main, "plate", 2)
            .item(() -> Items.CHEST, 1)
            .tech(Technologies.CONVEYOR_MODULE)
            .build()
            .recipe(ELECTRIC_TANK)
            .circuit(2)
            .component(ELECTRIC_PUMP, 1)
            .material(main, "plate", 2)
            .material(GLASS, "primary", 1)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(BATTERY_BOX)
            .circuit(2)
            .transform(Machines::pic)
            .component(CABLE, 4)
            .item(() -> Items.CHEST, 1)
            .tech(Technologies.BATTERY)
            .build()
            .recipe(TRANSFORMER)
            .circuit(4)
            .transform(Machines::pic)
            .component(CABLE, 1)
            .item(CABLE.get(Voltage.fromRank(v.rank - 1)), 4)
            .tech(Technologies.BATTERY)
            .build()
            .recipe(ELECTRIC_BUFFER)
            .circuit(4)
            .transform(Machines::pic)
            .component(CABLE, 2)
            .tech(Technologies.BATTERY)
            .build()
            .recipe(LOGISTIC_WORKER)
            .circuit(4)
            .component(CONVEYOR_MODULE, 2)
            .component(ELECTRIC_PUMP, 2)
            .material(main, "plate", 4)
            .tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            .build()
            .recipe(MULTIBLOCK_INTERFACE)
            .circuit(2)
            .component(CONVEYOR_MODULE, 1)
            .component(ELECTRIC_PUMP, 1)
            .item(() -> Items.CHEST, 1)
            .material(GLASS, "primary", 1)
            .tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            .build();
    }

    private static <P> AssemblyRecipeBuilder1<P> pic(AssemblyRecipeBuilder1<P> builder) {
        var v = builder.voltage();
        if (v.rank < Voltage.HV.rank) {
            return builder;
        } else if (v.rank < Voltage.IV.rank) {
            return builder.item(CHIPS.get("low_pic"), 2);
        } else if (v.rank < Voltage.ZPM.rank) {
            return builder.item(CHIPS.get("pic"), 2);
        } else {
            return builder.item(CHIPS.get("high_pic"), 2);
        }
    }
}
