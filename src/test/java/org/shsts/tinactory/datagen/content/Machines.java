package org.shsts.tinactory.datagen.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.datagen.content.model.MachineModel;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllBlockEntities.BATTERY_BOX;
import static org.shsts.tinactory.content.AllBlockEntities.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.CENTRIFUGE;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.EXTRACTOR;
import static org.shsts.tinactory.content.AllBlockEntities.FLUID_SOLIDIFIER;
import static org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.MACERATOR;
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
import static org.shsts.tinactory.content.AllBlockEntities.WORKBENCH;
import static org.shsts.tinactory.content.AllItems.CABLE;
import static org.shsts.tinactory.content.AllItems.HEAT_PROOF_BLOCK;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.TRANSFORMER;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STONE;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.content.AllTags.machineTag;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.cubeBlock;
import static org.shsts.tinactory.datagen.content.Models.machineBlock;
import static org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX;
import static org.shsts.tinactory.datagen.content.model.MachineModel.ME_BUS;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Machines {
    private static final String BOILER_TEX = "generators/boiler/coal";
    private static final long ASSEMBLE_TICKS = 200;

    public static void init() {
        machineItems();
        primitive();
        ulv();
        misc();
    }

    private static void machineItems() {
        primitiveMachine(STONE_GENERATOR, PRIMITIVE_STONE_GENERATOR, "machines/rock_crusher");
        primitiveMachine(ORE_ANALYZER, PRIMITIVE_ORE_ANALYZER, "machines/electromagnetic_separator");
        primitiveMachine(ORE_WASHER, PRIMITIVE_ORE_WASHER, "machines/ore_washer");
        machine(RESEARCH_BENCH, "overlay/machine/overlay_screen");
        machine(AllBlockEntities.ASSEMBLER);
        machine(MACERATOR);
        machine(CENTRIFUGE);
        machine(THERMAL_CENTRIFUGE);
        machine(ELECTRIC_FURNACE, "machines/electric_furnace");
        machine(ALLOY_SMELTER);
        machine(POLARIZER);
        machine(EXTRACTOR);
        machine(FLUID_SOLIDIFIER);
        machine(STEAM_TURBINE, $ -> $.ioTex(IO_TEX)
                .overlay(Direction.NORTH, "generators/steam_turbine/overlay_side")
                .overlay(Direction.SOUTH, "generators/steam_turbine/overlay_side"));
        machine(BATTERY_BOX, "overlay/machine/overlay_energy_out_multi");
        machine(ELECTRIC_CHEST, "overlay/machine/overlay_qchest", ME_BUS);

        DATA_GEN.block(NETWORK_CONTROLLER)
                .blockState(MachineModel::builder, MachineModel::blockState)
                .casing(Voltage.LV)
                .overlay("overlay/machine/overlay_screen")
                .ioTex(ME_BUS)
                .build()
                .tag(MINEABLE_WITH_WRENCH)
                .build()
                .block(WORKBENCH)
                .blockState(cubeBlock("casings/crafting_table"))
                .tag(BlockTags.MINEABLE_WITH_AXE, MINEABLE_WITH_WRENCH)
                .build()
                .block(LOW_PRESSURE_BOILER)
                .blockState(machineBlock(Voltage.ULV, BOILER_TEX))
                .tag(MINEABLE_WITH_WRENCH)
                .build()
                .block(HIGH_PRESSURE_BOILER)
                .blockState(machineBlock(Voltage.MV, BOILER_TEX))
                .tag(MINEABLE_WITH_WRENCH)
                .build()
                .block(BLAST_FURNACE)
                .blockState(machineBlock("casings/solid/machine_casing_heatproof",
                        "multiblock/blast_furnace"))
                .tag(MINEABLE_WITH_WRENCH)
                .build();

        MULTI_BLOCK_INTERFACE.values().forEach(b -> DATA_GEN.block(b)
                .blockState(machineBlock(ME_BUS))
                .tag(MINEABLE_WITH_WRENCH)
                .build());

        TRANSFORMER.values().forEach(b -> DATA_GEN.block(b)
                .blockState(MachineModel::builder, MachineModel::blockState)
                .overlay(Direction.NORTH, IO_TEX)
                .overlay(Direction.SOUTH, "overlay/machine/overlay_energy_out_multi")
                .build()
                .tag(MINEABLE_WITH_WRENCH)
                .build());
    }

    private static void primitive() {
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

    private static void ulv() {
        ulvFromPrimitive(STONE_GENERATOR, PRIMITIVE_STONE_GENERATOR);
        ulvFromPrimitive(ORE_ANALYZER, PRIMITIVE_ORE_ANALYZER);
        ulvFromPrimitive(ORE_WASHER, PRIMITIVE_ORE_WASHER);
        ulvMachine(NETWORK_CONTROLLER, VACUUM_TUBE);
        ulvMachine(RESEARCH_BENCH.entry(Voltage.ULV), () -> Blocks.CRAFTING_TABLE);
        ulvMachine(AllBlockEntities.ASSEMBLER.entry(Voltage.ULV), WORKBENCH);
        ulvMachine(ELECTRIC_FURNACE.entry(Voltage.ULV), () -> Blocks.FURNACE);

        TOOL_CRAFTING.recipe(DATA_GEN, STEAM_TURBINE.entry(Voltage.ULV))
                .result(STEAM_TURBINE.entry(Voltage.ULV), 1)
                .pattern("PVP").pattern("RHR").pattern("WVW")
                .define('P', COPPER.tag("pipe"))
                .define('R', IRON.tag("rotor"))
                .define('W', CABLE.get(Voltage.ULV))
                .define('H', MACHINE_HULL.get(Voltage.ULV))
                .define('V', VACUUM_TUBE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

        ASSEMBLER.recipe(DATA_GEN, ALLOY_SMELTER.entry(Voltage.ULV))
                .outputItem(2, ALLOY_SMELTER.entry(Voltage.ULV), 1)
                .inputItem(0, ELECTRIC_FURNACE.entry(Voltage.ULV), 1)
                .inputItem(0, VACUUM_TUBE, 2)
                .inputItem(0, CABLE.get(Voltage.ULV), 4)
                .requireTech(Technologies.ALLOY_SMELTING)
                .voltage(Voltage.ULV)
                .workTicks(ASSEMBLE_TICKS)
                .build()
                .recipe(DATA_GEN, BLAST_FURNACE)
                .outputItem(2, BLAST_FURNACE, 1)
                .inputItem(0, HEAT_PROOF_BLOCK, 1)
                .inputItem(0, ELECTRIC_FURNACE.entry(Voltage.ULV), 3)
                .inputItem(0, VACUUM_TUBE, 3)
                .inputItem(0, CABLE.get(Voltage.ULV), 2)
                .requireTech(Technologies.STEEL)
                .voltage(Voltage.ULV)
                .workTicks(ASSEMBLE_TICKS)
                .build();
    }

    private static void misc() {
        TOOL_CRAFTING.recipe(DATA_GEN, LOW_PRESSURE_BOILER)
                .result(LOW_PRESSURE_BOILER, 1)
                .pattern("PPP").pattern("PWP").pattern("VFV")
                .define('P', IRON.tag("plate"))
                .define('W', CABLE.get(Voltage.ULV))
                .define('V', VACUUM_TUBE)
                .define('F', Blocks.FURNACE.asItem())
                .toolTag(AllTags.TOOL_WRENCH)
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

    private static void machine(MachineSet set, Transformer<MachineModel.Builder<?>> model) {
        var tag = getMachineTag(set);
        tag.ifPresent($ -> DATA_GEN.tag($, AllTags.MACHINE));
        for (var voltage : set.voltages) {
            var builder = DATA_GEN.block(set.entry(voltage))
                    .blockState(MachineModel::builder, MachineModel::blockState)
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
        machine(set, "machines/" + set.recipeType.id);
    }

    private static void primitiveMachine(MachineSet set, RegistryEntry<? extends Block> primitive,
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

    private static void ulvMachine(RegistryEntry<? extends ItemLike> result,
                                   Supplier<? extends ItemLike> base) {
        TOOL_CRAFTING.recipe(DATA_GEN, result)
                .result(result, 1)
                .pattern("BBB").pattern("VHV").pattern("WVW")
                .define('B', base)
                .define('W', CABLE.get(Voltage.ULV))
                .define('H', MACHINE_HULL.get(Voltage.ULV))
                .define('V', VACUUM_TUBE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();
    }

    private static void ulvFromPrimitive(MachineSet set, RegistryEntry<? extends Block> primitive) {
        ulvMachine(set.entry(Voltage.ULV), primitive);
    }
}
