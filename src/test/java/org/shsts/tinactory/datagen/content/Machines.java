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
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.machine.MachineSet;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.datagen.content.model.MachineModel;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER;
import static org.shsts.tinactory.content.AllBlockEntities.ASSEMBLER;
import static org.shsts.tinactory.content.AllBlockEntities.BATTERY_BOX;
import static org.shsts.tinactory.content.AllBlockEntities.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.CENTRIFUGE;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST;
import static org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE;
import static org.shsts.tinactory.content.AllBlockEntities.HIGH_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.LOW_PRESSURE_BOILER;
import static org.shsts.tinactory.content.AllBlockEntities.MACERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.MULTI_BLOCK_INTERFACE;
import static org.shsts.tinactory.content.AllBlockEntities.NETWORK_CONTROLLER;
import static org.shsts.tinactory.content.AllBlockEntities.ORE_ANALYZER;
import static org.shsts.tinactory.content.AllBlockEntities.ORE_WASHER;
import static org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_ANALYZER;
import static org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_ORE_WASHER;
import static org.shsts.tinactory.content.AllBlockEntities.PRIMITIVE_STONE_GENERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.RESEARCH_BENCH;
import static org.shsts.tinactory.content.AllBlockEntities.STEAM_TURBINE;
import static org.shsts.tinactory.content.AllBlockEntities.STONE_GENERATOR;
import static org.shsts.tinactory.content.AllBlockEntities.THERMAL_CENTRIFUGE;
import static org.shsts.tinactory.content.AllBlockEntities.WORKBENCH;
import static org.shsts.tinactory.content.AllItems.CABLE;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.TEST_TRANSFORMER;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.FLINT;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.STONE;
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
        machine(ASSEMBLER, "machines/assembler");
        machine(MACERATOR, "machines/macerator");
        machine(CENTRIFUGE, "machines/centrifuge");
        machine(THERMAL_CENTRIFUGE, "machines/thermal_centrifuge");
        machine(ELECTRIC_FURNACE, "machines/electric_furnace");
        machine(ALLOY_SMELTER, "machines/alloy_smelter");
        machine(STEAM_TURBINE, "generators/steam_turbine/overlay_side");
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
                .build()
                .block(MULTI_BLOCK_INTERFACE)
                .blockState(machineBlock("casings/solid/machine_casing_solid_steel", IO_TEX))
                .tag(MINEABLE_WITH_WRENCH)
                .build()
                .block(TEST_TRANSFORMER)
                .blockState(MachineModel::builder, MachineModel::blockState)
                .overlay(Direction.NORTH, IO_TEX)
                .overlay(Direction.SOUTH, "overlay/machine/overlay_energy_out_multi")
                .build()
                .tag(MINEABLE_WITH_WRENCH)
                .build();
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
        ulvMachine(ASSEMBLER.entry(Voltage.ULV), WORKBENCH);
        ulvMachine(ELECTRIC_FURNACE.entry(Voltage.ULV), () -> Blocks.FURNACE);

        TOOL_CRAFTING.recipe(DATA_GEN, ALLOY_SMELTER.entry(Voltage.ULV))
                .result(ALLOY_SMELTER.entry(Voltage.ULV), 1)
                .pattern("WVW").pattern("VHV").pattern("WVW")
                .define('W', CABLE.get(Voltage.ULV))
                .define('H', ELECTRIC_FURNACE.entry(Voltage.ULV))
                .define('V', VACUUM_TUBE)
                .toolTag(AllTags.TOOL_WRENCH)
                .build();

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

    private static void machine(MachineSet set, String overlay, String ioTex) {
        var tag = getMachineTag(set);
        tag.ifPresent($ -> DATA_GEN.tag($, AllTags.MACHINE));
        for (var voltage : set.voltages) {
            var builder = DATA_GEN.block(set.entry(voltage))
                    .blockState(MachineModel::builder, MachineModel::blockState)
                    .overlay(overlay).ioTex(ioTex)
                    .build()
                    .tag(MINEABLE_WITH_WRENCH);
            tag.ifPresent(builder::itemTag);
            builder.build();
        }
    }

    private static void machine(MachineSet set, String overlay) {
        machine(set, overlay, IO_TEX);
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
