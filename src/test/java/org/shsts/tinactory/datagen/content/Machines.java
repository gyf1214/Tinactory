package org.shsts.tinactory.datagen.content;

import com.google.common.collect.Streams;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.datagen.content.model.MachineModel;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllBlockEntities.BATTERY_BOX;
import static org.shsts.tinactory.content.AllBlockEntities.BENDER;
import static org.shsts.tinactory.content.AllBlockEntities.CENTRIFUGE;
import static org.shsts.tinactory.content.AllBlockEntities.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllBlockEntities.CIRCUIT_ASSEMBLER;
import static org.shsts.tinactory.content.AllBlockEntities.COMPRESSOR;
import static org.shsts.tinactory.content.AllBlockEntities.CUTTER;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_TANK;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTROLYZER;
import static org.shsts.tinactory.content.AllBlockEntities.EXTRACTOR;
import static org.shsts.tinactory.content.AllBlockEntities.EXTRUDER;
import static org.shsts.tinactory.content.AllBlockEntities.FLUID_SOLIDIFIER;
import static org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.LASER_ENGRAVER;
import static org.shsts.tinactory.content.AllBlockEntities.LATHE;
import static org.shsts.tinactory.content.AllBlockEntities.LOGISTIC_WORKER;
import static org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.MACERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.MIXER;
import static org.shsts.tinactory.content.AllBlockEntities.MULTI_BLOCK_INTERFACE;
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
import static org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_BUFFER;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP;
import static org.shsts.tinactory.content.AllItems.EMITTER;
import static org.shsts.tinactory.content.AllItems.GRINDER;
import static org.shsts.tinactory.content.AllItems.ITEM_FILTER;
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
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMultiBlocks.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllMultiBlocks.DISTILLATION_TOWER;
import static org.shsts.tinactory.content.AllMultiBlocks.HEATPROOF_CASING;
import static org.shsts.tinactory.content.AllMultiBlocks.SIFTER;
import static org.shsts.tinactory.content.AllMultiBlocks.SOLID_STEEL_CASING;
import static org.shsts.tinactory.content.AllMultiBlocks.VACUUM_FREEZER;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;
import static org.shsts.tinactory.content.AllTags.circuit;
import static org.shsts.tinactory.content.AllTags.machineTag;
import static org.shsts.tinactory.datagen.content.Models.cubeBlock;
import static org.shsts.tinactory.datagen.content.Models.machineBlock;
import static org.shsts.tinactory.datagen.content.Models.multiBlock;
import static org.shsts.tinactory.datagen.content.Models.multiBlockInterface;
import static org.shsts.tinactory.datagen.content.model.MachineModel.IO_OUT_TEX;
import static org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX;
import static org.shsts.tinactory.datagen.content.model.MachineModel.ME_BUS;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Machines {
    private static final String BOILER_TEX = "generators/boiler/coal";
    private static final long ASSEMBLE_TICKS = 200;

    public static void init() {
        machineItems();
        primitiveRecipes();
        ulvRecipes();
        basicRecipes();
        miscRecipes();
    }

    private static void machineItems() {
        primitiveMachine(STONE_GENERATOR, PRIMITIVE_STONE_GENERATOR, "machines/rock_crusher");
        primitiveMachine(ORE_ANALYZER, PRIMITIVE_ORE_ANALYZER, "machines/electromagnetic_separator");
        primitiveMachine(ORE_WASHER, PRIMITIVE_ORE_WASHER, "machines/ore_washer");
        machine(RESEARCH_BENCH, "overlay/machine/overlay_screen");
        machine(AllBlockEntities.ASSEMBLER);
        machine(LASER_ENGRAVER);
        machine(CIRCUIT_ASSEMBLER, "machines/assembler");
        machine(MACERATOR);
        machine(CENTRIFUGE);
        machine(THERMAL_CENTRIFUGE);
        machine(ELECTRIC_FURNACE, "machines/electric_furnace");
        machine(ALLOY_SMELTER);
        machine(MIXER);
        machine(POLARIZER);
        machine(WIREMILL);
        machine(BENDER);
        machine(COMPRESSOR);
        machine(LATHE);
        machine(CUTTER);
        machine(EXTRUDER);
        machine(EXTRACTOR);
        machine(FLUID_SOLIDIFIER);
        machine(ELECTROLYZER);
        machine(CHEMICAL_REACTOR);
        machine(STEAM_TURBINE, $ -> $.ioTex(IO_TEX)
            .overlay(Direction.NORTH, "generators/steam_turbine/overlay_side")
            .overlay(Direction.SOUTH, "generators/steam_turbine/overlay_side"));
        machine(BATTERY_BOX, IO_OUT_TEX);
        machine(ELECTRIC_CHEST, $ -> $.ioTex(IO_TEX)
            .overlay(Direction.UP, "overlay/machine/overlay_qchest")
            .overlay(Direction.NORTH, "overlay/machine/overlay_screen_glass"));
        machine(ELECTRIC_TANK, $ -> $.ioTex(IO_TEX)
            .overlay(Direction.UP, "overlay/machine/overlay_qtank")
            .overlay(Direction.NORTH, "overlay/machine/overlay_screen_glass"));
        machine(LOGISTIC_WORKER, "cover/overlay_conveyor");

        DATA_GEN.block(NETWORK_CONTROLLER)
            .child(MachineModel::builder)
            .casing("casings/computer/computer_casing")
            .overlay("overlay/machine/overlay_maintenance_full_auto")
            .ioTex(ME_BUS)
            .build()
            .tag(MINEABLE_WITH_WRENCH)
            .build()
            .block(WORKBENCH)
            .blockState(cubeBlock("casings/crafting_table"))
            .tag(List.of(BlockTags.MINEABLE_WITH_AXE, MINEABLE_WITH_WRENCH))
            .build()
            .block(LOW_PRESSURE_BOILER)
            .child(MachineModel::builder)
            .casing(Voltage.ULV).overlay(BOILER_TEX).ioTex(ME_BUS)
            .build()
            .tag(MINEABLE_WITH_WRENCH)
            .build()
            .block(HIGH_PRESSURE_BOILER)
            .child(MachineModel::builder)
            .casing(Voltage.MV).overlay(BOILER_TEX).ioTex(ME_BUS)
            .build()
            .tag(MINEABLE_WITH_WRENCH)
            .build();

        multiBlockItem(BLAST_FURNACE, "heatproof", "blast_furnace", AllRecipes.BLAST_FURNACE);
        multiBlockItem(SIFTER, "solid_steel", "blast_furnace", AllRecipes.SIFTER);
        multiBlockItem(VACUUM_FREEZER, "frost_proof", "vacuum_freezer", AllRecipes.VACUUM_FREEZER);
        multiBlockItem(DISTILLATION_TOWER, "clean_stainless_steel", "distillation_tower",
            AllRecipes.DISTILLATION);

        MULTI_BLOCK_INTERFACE.values().forEach(b -> DATA_GEN.block(b)
            .blockState(multiBlockInterface(IO_TEX))
            .tag(MINEABLE_WITH_WRENCH)
            .build());

        Streams.concat(TRANSFORMER.values().stream(), ELECTRIC_BUFFER.values().stream())
            .forEach(b -> DATA_GEN.block(b)
                .child(MachineModel::builder)
                .overlay(Direction.NORTH, IO_TEX)
                .overlay(Direction.SOUTH, IO_OUT_TEX)
                .build()
                .tag(MINEABLE_WITH_WRENCH)
                .build());
    }

    private static void multiBlockItem(IEntry<? extends Block> block, String casing,
        String overlay, IRecipeType<?> type) {
        DATA_GEN.block(block)
            .blockState(multiBlock(casing, overlay))
            .tag(MINEABLE_WITH_WRENCH)
            .itemTag(machineTag(type))
            .build();
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
            .workTicks(ASSEMBLE_TICKS)
            .build()
            .recipe(DATA_GEN, BLAST_FURNACE)
            .outputItem(BLAST_FURNACE, 1)
            .inputItem(HEATPROOF_CASING, 1)
            .inputItem(ELECTRIC_FURNACE.entry(Voltage.ULV), 3)
            .inputItem(circuit(Voltage.ULV), 3)
            .inputItem(CABLE.get(Voltage.ULV), 2)
            .requireTech(Technologies.STEEL)
            .voltage(Voltage.ULV)
            .workTicks(ASSEMBLE_TICKS)
            .build()
            .recipe(DATA_GEN, MULTI_BLOCK_INTERFACE.get(Voltage.ULV))
            .outputItem(MULTI_BLOCK_INTERFACE.get(Voltage.ULV), 1)
            .inputItem(MACHINE_HULL.get(Voltage.ULV), 1)
            .inputItem(circuit(Voltage.ULV), 2)
            .inputItem(CABLE.get(Voltage.ULV), 2)
            .inputItem(() -> Blocks.CHEST, 1)
            .inputItem(GLASS.tag("primary"), 1)
            .voltage(Voltage.ULV)
            .workTicks(ASSEMBLE_TICKS)
            .requireTech(Technologies.STEEL)
            .build();
    }

    private static void basicRecipes() {
        machineRecipe(Voltage.LV, STEEL, COPPER, TIN, BRONZE, TIN);
        machineRecipe(Voltage.MV, ALUMINIUM, CUPRONICKEL, COPPER, BRASS, BRONZE);

        ASSEMBLER.recipe(DATA_GEN, SIFTER)
            .outputItem(SIFTER, 1)
            .inputItem(SOLID_STEEL_CASING, 1)
            .inputItem(circuit(Voltage.MV), 3)
            .inputItem(ELECTRIC_PISTON.get(Voltage.LV), 4)
            .inputItem(CABLE.get(Voltage.LV), 4)
            .inputItem(ITEM_FILTER, 4)
            .inputItem(STEEL.tag("plate"), 4)
            .voltage(Voltage.LV)
            .workTicks(ASSEMBLE_TICKS)
            .requireTech(Technologies.SIFTING)
            .build();
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

        // disable vanilla recipes
        DATA_GEN.nullRecipe(Items.BLAST_FURNACE)
            .nullRecipe(Items.SMOKER)
            .nullRecipe(Items.STONECUTTER)
            .nullRecipe(Items.FLETCHING_TABLE)
            .nullRecipe(Items.CARTOGRAPHY_TABLE)
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
            .nullRecipe(Items.WHITE_WOOL)
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
            .nullRecipe(Items.ACTIVATOR_RAIL);

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

    private static Optional<TagKey<Item>> getMachineTag(MachineSet set) {
        if (set instanceof ProcessingSet processingSet) {
            return Optional.of(machineTag(processingSet.recipeType));
        } else if (set == ELECTRIC_FURNACE) {
            return Optional.of(AllTags.ELECTRIC_FURNACE);
        }
        return Optional.empty();
    }

    private static void machine(MachineSet set,
        Transformer<MachineModel.Builder<?>> model) {
        var tag = getMachineTag(set);
        tag.ifPresent($ -> DATA_GEN.tag($, AllTags.MACHINE));
        for (var voltage : set.voltages) {
            var builder = DATA_GEN.block(set.entry(voltage))
                .child(MachineModel::builder)
                .transform(model.cast())
                .build()
                .tag(MINEABLE_WITH_WRENCH);
            tag.ifPresent(builder::itemTag);
            builder.build();
        }
    }

    private static void machine(MachineSet set, String overlay, String ioTex) {
        machine(set, $ -> $.overlay(overlay).ioTex(ioTex));
    }

    private static void machine(MachineSet set, String overlay) {
        machine(set, overlay, IO_TEX);
    }

    private static void machine(ProcessingSet set) {
        machine(set, "machines/" + set.recipeType.id());
    }

    private static void primitiveMachine(MachineSet set, IEntry<? extends Block> primitive,
        String overlay) {
        machine(set, overlay);
        var tag = getMachineTag(set);
        var builder = DATA_GEN.block(primitive)
            .blockState(machineBlock(overlay))
            .tag(MINEABLE_WITH_WRENCH)
            .tag(BlockTags.MINEABLE_WITH_AXE);
        tag.ifPresent(builder::itemTag);
        builder.build();
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

    private static class RecipeFactory {
        private final Voltage voltage;

        public RecipeFactory(Voltage voltage) {
            this.voltage = voltage;
        }

        private AssemblyRecipeBuilder<RecipeFactory> recipe(
            IEntry<? extends ItemLike> item, Voltage v1) {
            var builder = ASSEMBLER.recipe(DATA_GEN, item)
                .outputItem(item, 1)
                .voltage(v1)
                .workTicks(ASSEMBLE_TICKS)
                .inputItem(MACHINE_HULL.get(voltage), 1);
            return new AssemblyRecipeBuilder<>(this, voltage, builder) {
                private int components = 0;
                private boolean hasCable = false;

                @Override
                public AssemblyRecipeBuilder<RecipeFactory> component(
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
                    if (!hasCable && components > 0) {
                        component(CABLE, components);
                    }
                    return super.createObject();
                }
            };
        }

        public AssemblyRecipeBuilder<RecipeFactory> recipe(MachineSet set, Voltage v1) {
            if (!set.hasVoltage(voltage)) {
                return new AssemblyRecipeBuilder<>(this);
            }
            return recipe(set.entry(voltage), v1);
        }

        public AssemblyRecipeBuilder<RecipeFactory> recipe(
            Map<Voltage, ? extends IEntry<? extends ItemLike>> set, Voltage v1) {
            if (!set.containsKey(voltage)) {
                return new AssemblyRecipeBuilder<>(this);
            }
            return recipe(set.get(voltage), v1);
        }

        public AssemblyRecipeBuilder<RecipeFactory> recipe(MachineSet set) {
            return recipe(set, Voltage.fromRank(voltage.rank - 1));
        }

        public AssemblyRecipeBuilder<RecipeFactory> recipe(
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
            .material(main, "plate", 1)
            .tech(Technologies.ELECTRIC_HEATING)
            .build()
            .recipe(ALLOY_SMELTER)
            .circuit(4)
            .material(heat, "wire", wireNumber * 2)
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
            .material(main, "plate", 1)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(COMPRESSOR)
            .circuit(2)
            .component(ELECTRIC_PISTON, 4)
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
            .recipe(STEAM_TURBINE)
            .circuit(2)
            .component(ELECTRIC_MOTOR, 2)
            .material(rotor, "rotor", 2)
            .material(pipe, "pipe", 2)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(ELECTRIC_CHEST)
            .circuit(4)
            .component(CONVEYOR_MODULE, 1)
            .material(main, "plate", 2)
            .item(() -> Items.CHEST, 1)
            .tech(Technologies.CONVEYOR_MODULE)
            .build()
            .recipe(ELECTRIC_TANK)
            .circuit(4)
            .component(ELECTRIC_PUMP, 1)
            .material(main, "plate", 2)
            .material(GLASS, "primary", 1)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            // TODO: PIC
            .recipe(BATTERY_BOX)
            .circuit(2)
            .component(CABLE, 4)
            .item(() -> Items.CHEST, 1)
            .tech(Technologies.BATTERY)
            .build()
            // TODO: PIC
            .recipe(TRANSFORMER)
            .circuit(4)
            .component(CABLE, 1)
            .item(CABLE.get(Voltage.fromRank(v.rank - 1)), 4)
            .tech(Technologies.BATTERY)
            .build()
            // TODO: PIC
            .recipe(ELECTRIC_BUFFER)
            .circuit(4)
            .component(CABLE, 2)
            .tech(Technologies.BATTERY)
            .build()
            .recipe(LOGISTIC_WORKER)
            .circuit(2)
            .tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            .build()
            .recipe(MULTI_BLOCK_INTERFACE)
            .circuit(2)
            .component(CONVEYOR_MODULE, 1)
            .component(ELECTRIC_PUMP, 1)
            .item(() -> Items.CHEST, 1)
            .material(GLASS, "primary", 1)
            .tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            .build();
    }
}
