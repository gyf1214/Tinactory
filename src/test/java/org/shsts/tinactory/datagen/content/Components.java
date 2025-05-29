package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.CircuitComponentTier;
import org.shsts.tinactory.content.electric.CircuitTier;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.shsts.tinactory.content.AllItems.ADVANCED_BUZZSAW;
import static org.shsts.tinactory.content.AllItems.ADVANCED_GRINDER;
import static org.shsts.tinactory.content.AllItems.ADVANCED_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.BASIC_BUZZSAW;
import static org.shsts.tinactory.content.AllItems.BASIC_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.BATTERY;
import static org.shsts.tinactory.content.AllItems.BOULES;
import static org.shsts.tinactory.content.AllItems.CABLE;
import static org.shsts.tinactory.content.AllItems.CAPACITOR;
import static org.shsts.tinactory.content.AllItems.CHIPS;
import static org.shsts.tinactory.content.AllItems.COMPONENT_ITEMS;
import static org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE;
import static org.shsts.tinactory.content.AllItems.DIODE;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP;
import static org.shsts.tinactory.content.AllItems.ELECTRONIC_CIRCUIT;
import static org.shsts.tinactory.content.AllItems.EMITTER;
import static org.shsts.tinactory.content.AllItems.FERTILIZER;
import static org.shsts.tinactory.content.AllItems.FLUID_CELL;
import static org.shsts.tinactory.content.AllItems.GOOD_BUZZSAW;
import static org.shsts.tinactory.content.AllItems.GOOD_ELECTRONIC;
import static org.shsts.tinactory.content.AllItems.GOOD_GRINDER;
import static org.shsts.tinactory.content.AllItems.GOOD_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.INDUCTOR;
import static org.shsts.tinactory.content.AllItems.INTEGRATED_PROCESSOR;
import static org.shsts.tinactory.content.AllItems.ITEM_FILTER;
import static org.shsts.tinactory.content.AllItems.ITEM_STORAGE_CELL;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.MAINFRAME;
import static org.shsts.tinactory.content.AllItems.MICROPROCESSOR;
import static org.shsts.tinactory.content.AllItems.PROCESSOR_ASSEMBLY;
import static org.shsts.tinactory.content.AllItems.RAW_WAFERS;
import static org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllItems.RESISTOR;
import static org.shsts.tinactory.content.AllItems.ROBOT_ARM;
import static org.shsts.tinactory.content.AllItems.SENSOR;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllItems.TRANSISTOR;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllItems.WAFERS;
import static org.shsts.tinactory.content.AllItems.WORKSTATION;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BATTERY_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CADMIUM;
import static org.shsts.tinactory.content.AllMaterials.CHROME;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.DIAMOND;
import static org.shsts.tinactory.content.AllMaterials.ELECTRUM;
import static org.shsts.tinactory.content.AllMaterials.EMERALD;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM_ARSENIDE;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.IRON_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.LITHIUM;
import static org.shsts.tinactory.content.AllMaterials.NEODYMIUM;
import static org.shsts.tinactory.content.AllMaterials.NICHROME;
import static org.shsts.tinactory.content.AllMaterials.NICKEL_ZINC_FERRITE;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.PTFE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.SAPPHIRE;
import static org.shsts.tinactory.content.AllMaterials.SILICON;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SODIUM_HYDROXIDE;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STAINLESS_STEEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.SULFURIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.TITANIUM;
import static org.shsts.tinactory.content.AllMaterials.VANADIUM_STEEL;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllMaterials.ZINC;
import static org.shsts.tinactory.content.AllMultiblocks.AUTOFARM_BASE;
import static org.shsts.tinactory.content.AllMultiblocks.CLEAN_STAINLESS_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.CLEAR_GLASS;
import static org.shsts.tinactory.content.AllMultiblocks.COIL_BLOCKS;
import static org.shsts.tinactory.content.AllMultiblocks.CUPRONICKEL_COIL_BLOCK;
import static org.shsts.tinactory.content.AllMultiblocks.FILTER_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.FROST_PROOF_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.GRATE_MACHINE_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.HEATPROOF_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.INERT_PTFE_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.KANTHAL_COIL_BLOCK;
import static org.shsts.tinactory.content.AllMultiblocks.NICHROME_COIL_BLOCK;
import static org.shsts.tinactory.content.AllMultiblocks.PLASCRETE;
import static org.shsts.tinactory.content.AllMultiblocks.PTFE_PIPE_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.SOLID_CASINGS;
import static org.shsts.tinactory.content.AllMultiblocks.SOLID_STEEL_CASING;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllRecipes.CIRCUIT_ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.CUTTER;
import static org.shsts.tinactory.content.AllRecipes.LASER_ENGRAVER;
import static org.shsts.tinactory.content.AllRecipes.LATHE;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_WALL;
import static org.shsts.tinactory.content.AllTags.COIL;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;
import static org.shsts.tinactory.content.electric.Circuits.board;
import static org.shsts.tinactory.content.electric.Circuits.circuitBoard;
import static org.shsts.tinactory.core.util.LocHelper.ae2;
import static org.shsts.tinactory.core.util.LocHelper.gregtech;
import static org.shsts.tinactory.core.util.LocHelper.mcLoc;
import static org.shsts.tinactory.core.util.LocHelper.name;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.machineItem;
import static org.shsts.tinactory.datagen.content.Models.solidBlock;
import static org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Components {
    private static final String RESEARCH_TEX = "metaitems/glass_vial/";
    private static final String GRINDER_TEX = "metaitems/component.grinder";
    private static final String BUZZSAW_TEX = "tools/buzzsaw";
    private static final int ASSEMBLY_TICKS = 100;

    public static void init() {
        components();
        circuits();
        misc();
        ulvRecipes();
        componentRecipes();
        circuitRecipes();
        multiblockRecipes();
    }

    private static void components() {
        COMPONENT_ITEMS.forEach(entry -> DATA_GEN.item(entry)
            .model(Models::componentItem)
            .build());

        BATTERY.forEach((v, entry) -> DATA_GEN.item(entry)
            .model(Models::batteryItem)
            .tag(AllTags.battery(v))
            .build());

        MACHINE_HULL.forEach((v, entry) -> DATA_GEN.item(entry)
            .model(machineItem(v, IO_TEX))
            .build());

        RESEARCH_EQUIPMENT.forEach((v, entry) -> DATA_GEN.item(entry)
            .model(basicItem(RESEARCH_TEX + "base", RESEARCH_TEX + "overlay"))
            .build());

        CABLE.forEach((v, entry) -> DATA_GEN.block(entry)
            .blockState(Models::cableBlock)
            .itemModel(Models::cableItem)
            .tag(MINEABLE_WITH_CUTTER)
            .build());

        DATA_GEN.item(GOOD_GRINDER)
            .model(basicItem(GRINDER_TEX + ".diamond"))
            .build()
            .item(ADVANCED_GRINDER)
            .model(basicItem(GRINDER_TEX + ".tungsten"))
            .build();

        for (var item : List.of(BASIC_BUZZSAW, GOOD_BUZZSAW, ADVANCED_BUZZSAW)) {
            DATA_GEN.item(item)
                .model(basicItem(BUZZSAW_TEX))
                .build();
        }

        Stream.concat(BOULES.stream(), RAW_WAFERS.stream())
            .forEach(entry -> DATA_GEN.item(entry)
                .model(basicItem("metaitems/" + entry.id()
                    .replace('/', '.')
                    .replace("wafer_raw.", "wafer.")
                    .replace("glowstone", "phosphorus")))
                .build());

        chip("integrated_circuit", "integrated_logic_circuit");
        chip("cpu", "central_processing_unit");
        chip("nano_cpu", "nano_central_processing_unit");
        chip("qbit_cpu", "qbit_central_processing_unit");
        chip("ram", "random_access_memory");
        chip("nand", "nand_memory_chip");
        chip("nor", "nor_memory_chip");
        chip("simple_soc", "simple_system_on_chip");
        chip("soc", "system_on_chip");
        chip("advanced_soc", "advanced_system_on_chip");
        chip("low_pic", "low_power_integrated_circuit");
        chip("pic", "power_integrated_circuit");
        chip("high_pic", "high_power_integrated_circuit");

        DATA_GEN.item(ITEM_FILTER)
            .model(Models::simpleItem)
            .build()
            .item(FERTILIZER)
            .model(Models::simpleItem)
            .build();

        for (var i = 0; i < ITEM_STORAGE_CELL.size(); i++) {
            var k = 1 << (2 * i);
            DATA_GEN.item(ITEM_STORAGE_CELL.get(i))
                .model(basicItem(ae2("item/item_storage_cell_" + k + "k")))
                .build();
        }
    }

    private static void chip(String name, String tex) {
        List.of(WAFERS.get(name), CHIPS.get(name))
            .forEach(entry -> DATA_GEN.item(entry)
                .model(basicItem("metaitems/" + entry.id()
                    .replace('/', '.')
                    .replace("chip.", "plate.")
                    .replace(name, tex)))
                .build());
    }

    private static void circuits() {
        Circuits.forEach((tier, level, item) -> DATA_GEN.item(item)
            .model(basicItem("metaitems/" + item.id().replace('/', '.')))
            .tag(AllTags.circuit(Circuits.getVoltage(tier, level)))
            .build());
        Circuits.forEachComponent((component, tier, item) -> {
            var texKey = tier.prefix.isEmpty() ? component : tier.prefix + "." + component;
            var builder = DATA_GEN.item(item)
                .model(basicItem("metaitems/component." + texKey));
            for (var tier1 : CircuitComponentTier.values()) {
                if (tier1.rank <= tier.rank) {
                    builder.tag(AllTags.circuitComponent(component, tier1));
                }
            }
            builder.build();
        });
        for (var tier : CircuitTier.values()) {
            var boardName = switch (tier) {
                case NANO -> "epoxy";
                case QUANTUM -> "fiber_reinforced";
                case CRYSTAL -> "multilayer.fiber_reinforced";
                default -> tier.board;
            };

            DATA_GEN.item(board(tier))
                .model(basicItem("metaitems/board." + boardName))
                .build()
                .item(circuitBoard(tier))
                .model(basicItem("metaitems/circuit_board." + tier.circuitBoard))
                .build();
        }
    }

    private static void misc() {
        DATA_GEN.item(STICKY_RESIN)
            .model(basicItem("metaitems/rubber_drop"))
            .build();

        SOLID_CASINGS.forEach(block -> DATA_GEN.block(block)
            .blockState(solidBlock("casings/solid/machine_casing_" + name(block.id(), -1)))
            .tag(MINEABLE_WITH_WRENCH)
            .build());

        COIL_BLOCKS.forEach(coil -> DATA_GEN.block(coil)
            .blockState(solidBlock("casings/coils/machine_coil_" + name(coil.id(), -1)))
            .tag(List.of(COIL, MINEABLE_WITH_WRENCH))
            .build());

        DATA_GEN.block(GRATE_MACHINE_CASING)
            .blockState(solidBlock("casings/pipe/grate_steel_front/top"))
            .tag(MINEABLE_WITH_WRENCH)
            .build()
            .block(AUTOFARM_BASE)
            .blockState(ctx -> ctx.provider().simpleBlock(ctx.object(),
                ctx.provider().models().cubeTop(ctx.id(),
                    gregtech("blocks/casings/solid/machine_casing_solid_steel"),
                    mcLoc("block/farmland_moist"))))
            .tag(MINEABLE_WITH_WRENCH)
            .build()
            .block(CLEAR_GLASS)
            .blockState(solidBlock("casings/transparent/fusion_glass"))
            .tag(MINEABLE_WITH_WRENCH)
            .tag(CLEANROOM_WALL)
            .tag(Tags.Blocks.GLASS)
            .build()
            .block(PLASCRETE)
            .blockState(solidBlock("casings/cleanroom/plascrete"))
            .tag(MINEABLE_WITH_WRENCH)
            .tag(CLEANROOM_WALL)
            .build()
            .block(FILTER_CASING)
            .blockState(ctx -> ctx.provider().simpleBlock(ctx.object(),
                ctx.provider().models().cubeColumn(ctx.id(),
                    gregtech("blocks/casings/cleanroom/plascrete"),
                    gregtech("blocks/casings/cleanroom/filter_casing"))))
            .tag(MINEABLE_WITH_WRENCH)
            .build()
            .block(PTFE_PIPE_CASING)
            .blockState(solidBlock("casings/pipe/machine_casing_pipe_polytetrafluoroethylene"))
            .tag(MINEABLE_WITH_WRENCH)
            .build();

        FLUID_CELL.forEach((v, item) -> {
            var texBase = v == Voltage.ULV ? "metaitems/fluid_cell" :
                "metaitems/large_fluid_cell." + name(item.id(), -1);
            DATA_GEN.item(item)
                .model(basicItem(texBase + "/base", texBase + "/overlay"))
                .build();
        });

        DATA_GEN.tag(BlockTags.DOORS, CLEANROOM_DOOR);
    }

    private static void ulvRecipes() {
        DATA_GEN.vanillaRecipe(() -> ShapelessRecipeBuilder
            .shapeless(CABLE.get(Voltage.ULV).get())
            .requires(Ingredient.of(IRON.tag("wire")), 4)
            .unlockedBy("has_wire", has(IRON.tag("wire"))));

        TOOL_CRAFTING.recipe(DATA_GEN, MACHINE_HULL.get(Voltage.ULV))
            .result(MACHINE_HULL.get(Voltage.ULV), 1)
            .pattern("###").pattern("#W#").pattern("###")
            .define('#', IRON.tag("plate"))
            .define('W', CABLE.get(Voltage.ULV))
            .toolTag(TOOL_WRENCH)
            .build()
            .recipe(DATA_GEN, FLUID_CELL.get(Voltage.ULV))
            .result(FLUID_CELL.get(Voltage.ULV), 1)
            .pattern("###").pattern("#G#").pattern(" # ")
            .define('#', IRON.tag("plate"))
            .define('G', GLASS.tag("primary"))
            .toolTag(TOOL_HAMMER, TOOL_WRENCH)
            .build();

        ASSEMBLER.recipe(DATA_GEN, FLUID_CELL.get(Voltage.ULV))
            .outputItem(FLUID_CELL.get(Voltage.ULV), 1)
            .inputItem(IRON.tag("plate"), 4)
            .inputItem(GLASS.tag("primary"), 1)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(1f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.SOLDERING)
            .build();
    }

    private static void componentRecipes() {
        TOOL_CRAFTING.recipe(DATA_GEN, CABLE.get(Voltage.LV))
            .result(CABLE.get(Voltage.LV), 1)
            .pattern("WWR").pattern("WWR").pattern("RR ")
            .define('W', TIN.tag("wire"))
            .define('R', RUBBER.tag("sheet"))
            .toolTag(TOOL_WIRE_CUTTER)
            .build();

        CABLE.forEach((v, cable) -> {
            if (v != Voltage.ULV) {
                ASSEMBLER.recipe(DATA_GEN, cable)
                    .outputItem(cable, 1)
                    .inputItem(cable.get().material.tag("wire"), 4)
                    .inputFluid(RUBBER.fluid(), RUBBER.fluidAmount(2))
                    .voltage(v == Voltage.LV ? Voltage.ULV : Voltage.LV)
                    .requireTech(Technologies.HOT_WORKING)
                    .workTicks(100L)
                    .build();
            }
        });

        componentRecipe(Voltage.LV, STEEL, COPPER, BRONZE, TIN, STEEL, BRASS, GLASS);
        componentRecipe(Voltage.MV, ALUMINIUM, CUPRONICKEL, BRASS, BRONZE, STEEL, ELECTRUM, RUBY);
        componentRecipe(Voltage.HV, STAINLESS_STEEL, ELECTRUM, STAINLESS_STEEL, STEEL, STEEL, CHROME, EMERALD);
        // TODO: sensor and emitter
        componentRecipe(Voltage.EV, TITANIUM, KANTHAL, TITANIUM, STAINLESS_STEEL, NEODYMIUM, CHROME, EMERALD);

        batteryRecipe(Voltage.LV, CADMIUM);
        batteryRecipe(Voltage.MV, SODIUM_HYDROXIDE);
        batteryRecipe(Voltage.HV, LITHIUM);

        // TODO: advanced_buzzsaw
        buzzsawRecipe(BASIC_BUZZSAW, COBALT_BRASS, Voltage.LV);
        buzzsawRecipe(GOOD_BUZZSAW, VANADIUM_STEEL, Voltage.MV);

        // TODO: advanced_grinder
        ASSEMBLER.recipe(DATA_GEN, GOOD_GRINDER)
            .outputItem(GOOD_GRINDER, 1)
            .inputItem(DIAMOND.tag("gem_flawless"), 1)
            .inputItem(STEEL.tag("plate"), 8)
            .inputItem(DIAMOND.tag("dust"), 4)
            .voltage(Voltage.MV)
            .workTicks(ASSEMBLY_TICKS)
            .requireTech(Technologies.MATERIAL_CUTTING)
            .build();

        researchRecipe(Voltage.ULV)
            .inputItem(IRON.tag("plate"), 1)
            .inputItem(COPPER.tag("wire"), 1)
            .build();

        researchRecipe(Voltage.LV)
            .inputItem(ELECTRIC_MOTOR.get(Voltage.LV), 1)
            .inputItem(STEEL.tag("gear"), 1)
            .build();

        researchRecipe(Voltage.MV)
            .inputItem(ELECTRIC_PUMP.get(Voltage.MV), 1)
            .inputItem(circuitBoard(CircuitTier.CPU), 1)
            .build();
    }

    private static class ComponentRecipeFactory {
        private final Voltage voltage;
        private final Voltage baseVoltage;

        public ComponentRecipeFactory(Voltage voltage) {
            this.voltage = voltage;
            this.baseVoltage = voltage == Voltage.LV ? Voltage.ULV : Voltage.LV;
        }

        public AssemblyRecipeBuilder<ComponentRecipeFactory> recipe(
            Map<Voltage, ? extends IEntry<? extends ItemLike>> component, int count) {
            if (!component.containsKey(voltage)) {
                return new AssemblyRecipeBuilder<>(this);
            }
            var builder = ASSEMBLER.recipe(DATA_GEN, component.get(voltage))
                .outputItem(component.get(voltage), count)
                .voltage(baseVoltage)
                .workTicks(ASSEMBLY_TICKS);
            return new AssemblyRecipeBuilder<>(this, voltage, builder);
        }

        public AssemblyRecipeBuilder<ComponentRecipeFactory> recipe(
            Map<Voltage, ? extends IEntry<? extends ItemLike>> component) {
            return recipe(component, 1);
        }
    }

    private static void componentRecipe(Voltage voltage, MaterialSet main,
        MaterialSet motor, MaterialSet pipe,
        MaterialSet rotor, MaterialSet magnetic,
        MaterialSet sensor, MaterialSet quartz) {
        var factory = new ComponentRecipeFactory(voltage);

        factory.recipe(ELECTRIC_MOTOR)
            .material(magnetic, "magnetic", 1)
            .material(main, "stick", 2)
            .material(motor, "wire", 2 * voltage.rank)
            .component(CABLE, 2)
            .tech(Technologies.MOTOR)
            .build()
            .recipe(ELECTRIC_PUMP)
            .component(ELECTRIC_MOTOR, 1)
            .material(pipe, "pipe", 1)
            .material(rotor, "rotor", 1)
            .material(rotor, "screw", 3)
            .material(RUBBER, "ring", 2)
            .component(CABLE, 1)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(ELECTRIC_PISTON)
            .component(ELECTRIC_MOTOR, 1)
            .material(main, "plate", 3)
            .material(main, "stick", 2)
            .material(main, "gear", 1)
            .component(CABLE, 2)
            .tech(Technologies.PUMP_AND_PISTON)
            .build()
            .recipe(CONVEYOR_MODULE)
            .component(ELECTRIC_MOTOR, 2)
            .component(CABLE, 1)
            .materialFluid(RUBBER, 6)
            .tech(Technologies.CONVEYOR_MODULE)
            .build()
            .recipe(SENSOR)
            .material(quartz, "gem", 1)
            .circuit(1)
            .material(sensor, "stick", 1)
            .material(main, "plate", 4)
            .tech(Technologies.SENSOR_AND_EMITTER)
            .build()
            .recipe(EMITTER)
            .material(quartz, "gem", 1)
            .circuit(2)
            .component(CABLE, 2)
            .material(sensor, "stick", 4)
            .tech(Technologies.SENSOR_AND_EMITTER)
            .build()
            .recipe(ROBOT_ARM)
            .circuit(1)
            .component(CABLE, 3)
            .component(ELECTRIC_MOTOR, 2)
            .component(ELECTRIC_PISTON, 1)
            .material(main, "stick", 2)
            .tech(Technologies.ROBOT_ARM)
            .build()
            .recipe(MACHINE_HULL)
            .material(main, "plate", 8)
            .component(CABLE, 2)
            .transform($ -> voltage.rank >= Voltage.HV.rank ? $.materialFluid(PE, 2f) : $)
            .tech(Technologies.SOLDERING)
            .build()
            .recipe(FLUID_CELL)
            .material(main, "plate", voltage.rank * 2)
            .material(rotor, "ring", voltage.rank)
            .materialFluid(SOLDERING_ALLOY, voltage.rank)
            .tech(Technologies.SOLDERING)
            .transformBuilder($ -> $.voltage(Voltage.LV))
            .build();
    }

    private static void batteryRecipe(Voltage voltage, MaterialSet material) {
        var wires = voltage.rank - 1;
        var plates = wires * wires;

        var builder = ASSEMBLER.recipe(DATA_GEN, BATTERY.get(voltage))
            .outputItem(BATTERY.get(voltage), 1);
        if (voltage.rank > Voltage.LV.rank) {
            var bat1 = AllTags.battery(Voltage.fromRank(voltage.rank - 1));
            builder.inputItem(bat1, 2);
        }
        builder.inputItem(CABLE.get(voltage), wires)
            .inputItem(BATTERY_ALLOY.tag("plate"), plates)
            .inputItem(material.tag("dust"), plates)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(wires))
            .voltage(Voltage.LV)
            .workTicks(ASSEMBLY_TICKS)
            .requireTech(Technologies.BATTERY)
            .build();
    }

    private static AssemblyRecipe.Builder researchRecipe(Voltage voltage) {
        return ASSEMBLER.recipe(DATA_GEN, RESEARCH_EQUIPMENT.get(voltage))
            .outputItem(RESEARCH_EQUIPMENT.get(voltage), 1)
            .workTicks(200L)
            .voltage(voltage);
    }

    private static void buzzsawRecipe(IEntry<Item> item, MaterialSet material, Voltage v) {
        LATHE.recipe(DATA_GEN, item)
            .outputItem(item, 1)
            .inputItem(material.tag("gear"), 1)
            .voltage(v)
            .workTicks(240L)
            .build();
    }

    private static void circuitRecipes() {
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(VACUUM_TUBE.getItem())
            .pattern("BGB").pattern("WWW")
            .define('G', GLASS.tag("primary"))
            .define('W', COPPER.tag("wire"))
            .define('B', IRON.tag("bolt"))
            .unlockedBy("has_wire", has(COPPER.tag("wire"))));

        ASSEMBLER.recipe(DATA_GEN, VACUUM_TUBE.item())
            .outputItem(VACUUM_TUBE.item(), 1)
            .inputItem(GLASS.tag("primary"), 1)
            .inputItem(COPPER.tag("wire"), 1)
            .inputItem(IRON.tag("bolt"), 1)
            .workTicks(120L)
            .voltage(Voltage.ULV)
            .requireTech(Technologies.SOLDERING)
            .build();

        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(ELECTRONIC_CIRCUIT.getItem())
            .pattern("RPR").pattern("TBT").pattern("WWW")
            .define('R', RESISTOR.getItem(CircuitComponentTier.NORMAL))
            .define('P', STEEL.tag("plate"))
            .define('T', VACUUM_TUBE.getItem())
            .define('B', circuitBoard(CircuitTier.ELECTRONIC).get())
            .define('W', RED_ALLOY.tag("wire"))
            .unlockedBy("has_board", has(circuitBoard(CircuitTier.ELECTRONIC).get())));

        circuitRecipe(ELECTRONIC_CIRCUIT, VACUUM_TUBE, 2, RESISTOR, 2, RED_ALLOY.tag("wire"), 2);

        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(GOOD_ELECTRONIC.getItem())
            .pattern("DPD").pattern("EBE").pattern("WEW")
            .define('D', DIODE.getItem(CircuitComponentTier.NORMAL))
            .define('P', STEEL.tag("plate"))
            .define('E', ELECTRONIC_CIRCUIT.getItem())
            .define('B', circuitBoard(CircuitTier.ELECTRONIC).get())
            .define('W', COPPER.tag("wire"))
            .unlockedBy("has_circuit", has(ELECTRONIC_CIRCUIT.getItem())));

        circuitRecipe(GOOD_ELECTRONIC, ELECTRONIC_CIRCUIT, 2, DIODE, 2, COPPER.tag("wire"), 2);

        circuitRecipe(BASIC_INTEGRATED, CHIPS.get("integrated_circuit"), RESISTOR, 2, DIODE, 2,
            COPPER.tag("wire_fine"), 2, TIN.tag("bolt"), 2);
        circuitRecipe(GOOD_INTEGRATED, BASIC_INTEGRATED, 2, RESISTOR, 2, DIODE, 2,
            GOLD.tag("wire_fine"), 4, SILVER.tag("bolt"), 4);
        circuitRecipe(ADVANCED_INTEGRATED, GOOD_INTEGRATED, 2, CHIPS.get("integrated_circuit"), 2,
            CHIPS.get("ram"), 2, TRANSISTOR, 4, ELECTRUM.tag("wire_fine"), 8, COPPER.tag("bolt"), 8);

        circuitRecipe(MICROPROCESSOR, 3, CHIPS.get("cpu"), RESISTOR, 2, CAPACITOR, 2,
            TRANSISTOR, 2, COPPER.tag("wire_fine"), 2);
        circuitRecipe(INTEGRATED_PROCESSOR, CHIPS.get("cpu"), RESISTOR, 2, CAPACITOR, 2,
            TRANSISTOR, 2, RED_ALLOY.tag("wire_fine"), 4);
        circuitRecipe(PROCESSOR_ASSEMBLY, INTEGRATED_PROCESSOR, 2, INDUCTOR, 2, CAPACITOR, 8,
            CHIPS.get("ram"), 4, RED_ALLOY.tag("wire_fine"), 8);
        circuitRecipe(WORKSTATION, PROCESSOR_ASSEMBLY, 2, DIODE, 4, CHIPS.get("ram"), 4,
            ELECTRUM.tag("wire_fine"), 16, GOLD.tag("bolt"), 16);
        circuitRecipe(MAINFRAME, ALUMINIUM.tag("stick"), 8, WORKSTATION, 2,
            CHIPS.get("ram"), 16, INDUCTOR, 8, CAPACITOR, 16, COPPER.tag("wire"), 16);

        // circuit components
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(RESISTOR.getItem(CircuitComponentTier.NORMAL))
            .pattern(" R ").pattern("WCW").pattern(" R ")
            .define('R', STICKY_RESIN.get())
            .define('W', COPPER.tag("wire"))
            .define('C', COAL.tag("dust"))
            .unlockedBy("has_resin", has(STICKY_RESIN.get())));

        ASSEMBLER.recipe(DATA_GEN, RESISTOR.loc(CircuitComponentTier.NORMAL))
            .outputItem(RESISTOR.item(CircuitComponentTier.NORMAL), 2)
            .inputItem(COAL.tag("dust"), 1)
            .inputItem(COPPER.tag("wire_fine"), 4)
            .inputFluid(RUBBER.fluid(), RUBBER.fluidAmount(1f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.ULV)
            .requireTech(Technologies.SOLDERING)
            .build()
            .recipe(DATA_GEN, CAPACITOR.loc(CircuitComponentTier.NORMAL))
            .outputItem(CAPACITOR.item(CircuitComponentTier.NORMAL), 8)
            .inputItem(PVC.tag("foil"), 1)
            .inputItem(ALUMINIUM.tag("foil"), 2)
            .inputFluid(PE.fluid(), PE.fluidAmount(1f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build()
            .recipe(DATA_GEN, INDUCTOR.loc(CircuitComponentTier.NORMAL))
            .outputItem(INDUCTOR.item(CircuitComponentTier.NORMAL), 4)
            .inputItem(NICKEL_ZINC_FERRITE.tag("ring"), 1)
            .inputItem(COPPER.tag("wire_fine"), 2)
            .inputFluid(PE.fluid(), PE.fluidAmount(0.25f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build()
            .recipe(DATA_GEN, DIODE.loc(CircuitComponentTier.NORMAL))
            .outputItem(DIODE.item(CircuitComponentTier.NORMAL), 4)
            .inputItem(GALLIUM_ARSENIDE.tag("dust"), 1)
            .inputItem(GLASS.tag("primary"), 1)
            .inputItem(COPPER.tag("wire_fine"), 4)
            .inputFluid(RUBBER.fluid(), RUBBER.fluidAmount(2f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.SOLDERING)
            .build()
            .recipe(DATA_GEN, suffix(DIODE.loc(CircuitComponentTier.NORMAL), "_from_wafer"))
            .outputItem(DIODE.item(CircuitComponentTier.NORMAL), 8)
            .inputItem(RAW_WAFERS.get(0), 1)
            .inputItem(COPPER.tag("wire_fine"), 4)
            .inputFluid(PE.fluid(), PE.fluidAmount(1f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build()
            .recipe(DATA_GEN, TRANSISTOR.loc(CircuitComponentTier.NORMAL))
            .outputItem(TRANSISTOR.item(CircuitComponentTier.NORMAL), 4)
            .inputItem(GALLIUM_ARSENIDE.tag("dust"), 1)
            .inputItem(TIN.tag("wire_fine"), 6)
            .inputFluid(RUBBER.fluid(), RUBBER.fluidAmount(2f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build()
            .recipe(DATA_GEN, suffix(TRANSISTOR.loc(CircuitComponentTier.NORMAL), "_from_pe"))
            .outputItem(TRANSISTOR.item(CircuitComponentTier.NORMAL), 8)
            .inputItem(SILICON.tag("dust"), 1)
            .inputItem(TIN.tag("wire_fine"), 6)
            .inputFluid(PE.fluid(), PE.fluidAmount(1f))
            .workTicks(ASSEMBLY_TICKS)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build();

        // boards
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(board(CircuitTier.ELECTRONIC).get(), 3)
            .pattern("SSS").pattern("WWW").pattern("SSS")
            .define('S', STICKY_RESIN.get())
            .define('W', ItemTags.PLANKS)
            .unlockedBy("has_resin", has(STICKY_RESIN.get())));

        ASSEMBLER.recipe(DATA_GEN, board(CircuitTier.ELECTRONIC))
            .outputItem(board(CircuitTier.ELECTRONIC), 1)
            .inputItem(ItemTags.PLANKS, 1)
            .inputItem(STICKY_RESIN, 2)
            .workTicks(200L)
            .voltage(Voltage.ULV)
            .requireTech(Technologies.SOLDERING)
            .build()
            .recipe(DATA_GEN, board(CircuitTier.INTEGRATED))
            .outputItem(board(CircuitTier.INTEGRATED), 1)
            .inputItem(board(CircuitTier.ELECTRONIC), 2)
            .inputItem(RED_ALLOY.tag("wire"), 8)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(1f))
            .workTicks(200L)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build();

        CHEMICAL_REACTOR.recipe(DATA_GEN, board(CircuitTier.CPU))
            .input(PE, "sheet", 1)
            .input(COPPER, "foil", 4)
            .input(SULFURIC_ACID, "dilute", 0.25f)
            .outputItem(board(CircuitTier.CPU), 1)
            .workTicks(240L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CPU)
            .build()
            .recipe(DATA_GEN, suffix(board(CircuitTier.CPU).loc(), "_from_pvc"))
            .input(PVC, "sheet", 1)
            .input(COPPER, "foil", 4)
            .input(SULFURIC_ACID, "dilute", 0.25f)
            .outputItem(board(CircuitTier.CPU), 2)
            .workTicks(240L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CPU)
            .build();

        // circuit boards
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(circuitBoard(CircuitTier.ELECTRONIC).get())
            .pattern("WWW").pattern("WBW").pattern("WWW")
            .define('B', board(CircuitTier.ELECTRONIC).get())
            .define('W', COPPER.tag("wire"))
            .unlockedBy("has_board", has(board(CircuitTier.ELECTRONIC).get())));

        ASSEMBLER.recipe(DATA_GEN, circuitBoard(CircuitTier.ELECTRONIC))
            .outputItem(circuitBoard(CircuitTier.ELECTRONIC), 1)
            .inputItem(board(CircuitTier.ELECTRONIC), 1)
            .inputItem(COPPER.tag("wire"), 8)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(0.5f))
            .workTicks(200L)
            .voltage(Voltage.ULV)
            .requireTech(Technologies.SOLDERING)
            .build()
            .recipe(DATA_GEN, circuitBoard(CircuitTier.INTEGRATED))
            .outputItem(circuitBoard(CircuitTier.INTEGRATED), 1)
            .inputItem(board(CircuitTier.INTEGRATED), 1)
            .inputItem(SILVER.tag("wire"), 8)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(0.5f))
            .workTicks(200L)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build();

        CHEMICAL_REACTOR.recipe(DATA_GEN, circuitBoard(CircuitTier.CPU))
            .inputItem(board(CircuitTier.CPU), 1)
            .input(COPPER, "foil", 6)
            .input(IRON_CHLORIDE, 0.25f)
            .outputItem(circuitBoard(CircuitTier.CPU), 1)
            .workTicks(320L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CPU)
            .build();

        // boules
        BLAST_FURNACE.recipe(DATA_GEN, BOULES.get(0))
            .outputItem(BOULES.get(0), 1)
            .inputItem(SILICON.tag("dust"), 32)
            .inputItem(GALLIUM_ARSENIDE.tag("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(6400L)
            .temperature(2100)
            .build();

        // raw wafers
        for (var i = 0; i < RAW_WAFERS.size(); i++) {
            var boule = BOULES.get(i);
            var wafer = RAW_WAFERS.get(i);
            CUTTER.recipe(DATA_GEN, wafer)
                .outputItem(wafer, 8 << i)
                .inputItem(boule, 1)
                .inputFluid(WATER.fluid(), WATER.fluidAmount(1 << i))
                .voltage(Voltage.fromRank(2 + 2 * i))
                .workTicks(400L << i)
                .build();
        }

        // engraving
        engravingRecipe("integrated_circuit", RUBY, 0, Voltage.LV, -1d, 0d);
        engravingRecipe("cpu", DIAMOND, 0, Voltage.MV, 0d, 0.5d);
        engravingRecipe("ram", SAPPHIRE, 0, Voltage.MV, -0.25d, 0.25d);
        engravingRecipe("low_pic", EMERALD, 0, Voltage.MV, -0.3d, 0.2d);

        // chips
        for (var entry : CHIPS.entrySet()) {
            var wafer = WAFERS.get(entry.getKey());
            var chip = entry.getValue();
            CUTTER.recipe(DATA_GEN, chip)
                .outputItem(chip, 6)
                .inputItem(wafer, 1)
                .inputFluid(WATER.fluid(), WATER.fluidAmount(0.75f))
                .voltage(Voltage.LV)
                .workTicks(300L)
                .build();
        }
    }

    private static void engravingRecipe(String name, MaterialSet lens, int level, Voltage voltage,
        double minCleanness, double maxCleanness) {
        var wafer = WAFERS.get(name);
        for (var i = 0; i < RAW_WAFERS.size(); i++) {
            if (i < level) {
                continue;
            }
            var j = i - level;
            var raw = RAW_WAFERS.get(i);

            var minC = 1d - (1d - minCleanness) / (1 << i);
            var maxC = 1d - (1d - maxCleanness) / (1 << i);
            LASER_ENGRAVER.recipe(DATA_GEN, suffix(wafer.loc(), "_from_" + name(raw.id(), -1)))
                .outputItem(wafer, 1 << j)
                .inputItem(0, raw, 1)
                .inputItemNotConsumed(1, lens.tag("lens"))
                .voltage(Voltage.fromRank(voltage.rank + j))
                .workTicks(1000L << level)
                .requireCleanness(minC, maxC)
                .build();
        }
    }

    @SuppressWarnings("unchecked")
    private static void circuitRecipe(Circuits.Circuit circuit, Object... args) {
        var i = 0;
        var output = 1;
        if (args[0] instanceof Integer k) {
            output = k;
            i++;
        }
        var builder = CIRCUIT_ASSEMBLER.recipe(DATA_GEN, circuit.item())
            .outputItem(circuit.item(), output);

        if (circuit.level().voltageOffset < 2) {
            builder.inputItem(circuit.circuitBoard(), 1);
        }

        for (; i < args.length; i++) {
            var item = args[i];
            var count = 1;
            if (i + 1 < args.length && args[i + 1] instanceof Integer k) {
                count = k;
                i++;
            }
            if (item instanceof TagKey<?>) {
                builder.inputItem((TagKey<Item>) item, count);
            } else if (item instanceof Circuits.CircuitComponent component) {
                builder.inputItem(component.tag(circuit.tier().componentTier), count);
            } else if (item instanceof Circuits.Circuit circuit1) {
                builder.inputItem(circuit1.item(), count);
            } else if (item instanceof ItemLike itemLike) {
                builder.inputItem(() -> itemLike, count);
            } else if (item instanceof Supplier<?>) {
                builder.inputItem((Supplier<? extends ItemLike>) item, count);
            } else {
                throw new IllegalArgumentException();
            }
        }
        var level = 1 + Math.max(0, circuit.level().voltageOffset);
        var voltage = circuit.tier().baseVoltage;
        if (voltage.rank < Voltage.LV.rank) {
            voltage = Voltage.LV;
        }
        builder.voltage(voltage)
            .inputFluid(SOLDERING_ALLOY.fluid(),
                SOLDERING_ALLOY.fluidAmount((1 << (level - 1)) / 2f))
            .workTicks(200L * level)
            .build();
    }

    private static void multiblockRecipes() {
        solidRecipe(HEATPROOF_CASING, Voltage.ULV, INVAR, Technologies.STEEL);
        solidRecipe(SOLID_STEEL_CASING, Voltage.LV, STEEL, Technologies.STEEL);
        solidRecipe(FROST_PROOF_CASING, Voltage.LV, ALUMINIUM, Technologies.VACUUM_FREEZER);
        solidRecipe(CLEAN_STAINLESS_CASING, Voltage.MV, STAINLESS_STEEL, Technologies.DISTILLATION);

        coilRecipe(CUPRONICKEL_COIL_BLOCK, Voltage.ULV, CUPRONICKEL, BRONZE, Technologies.STEEL);
        coilRecipe(KANTHAL_COIL_BLOCK, Voltage.LV, KANTHAL, SILVER, Technologies.KANTHAL);
        coilRecipe(NICHROME_COIL_BLOCK, Voltage.MV, NICHROME, STAINLESS_STEEL, Technologies.NICHROME);

        ASSEMBLER.recipe(DATA_GEN, ITEM_FILTER)
            .outputItem(ITEM_FILTER, 1)
            .inputItem(STEEL.tag("plate"), 1)
            .inputItem(ZINC.tag("foil"), 8)
            .voltage(Voltage.LV)
            .workTicks(200L)
            .requireTech(Technologies.SIFTING)
            .build()
            .recipe(DATA_GEN, GRATE_MACHINE_CASING)
            .outputItem(GRATE_MACHINE_CASING, 2)
            .inputItem(STEEL.tag("stick"), 4)
            .inputItem(ELECTRIC_MOTOR.get(Voltage.LV), 1)
            .inputItem(TIN.tag("rotor"), 1)
            .inputItem(ITEM_FILTER, 6)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(2))
            .voltage(Voltage.LV)
            .workTicks(140L)
            .requireTech(Technologies.SIFTING)
            .build()
            .recipe(DATA_GEN, AUTOFARM_BASE)
            .outputItem(AUTOFARM_BASE, 1)
            .inputItem(STEEL.tag("stick"), 2)
            .inputItem(() -> Blocks.COARSE_DIRT, 2)
            .inputItem(() -> Blocks.PODZOL, 2)
            .inputItem(STEEL.tag("plate"), 3)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(2))
            .workTicks(200L)
            .voltage(Voltage.LV)
            .requireTech(Technologies.AUTOFARM)
            .build()
            .recipe(DATA_GEN, CLEAR_GLASS)
            .outputItem(CLEAR_GLASS, 1)
            .inputItem(STEEL.tag("stick"), 2)
            .inputItem(GLASS.tag("primary"), 1)
            .inputFluid(PE.fluid(), PE.fluidAmount(3f))
            .workTicks(200L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ORGANIC_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, PLASCRETE)
            .outputItem(PLASCRETE, 1)
            .inputItem(STEEL.tag("stick"), 2)
            .inputItem(PE.tag("sheet"), 3)
            .workTicks(200L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CLEANROOM)
            .build()
            .recipe(DATA_GEN, FILTER_CASING)
            .outputItem(FILTER_CASING, 2)
            .inputItem(STEEL.tag("stick"), 4)
            .inputItem(ELECTRIC_MOTOR.get(Voltage.MV), 1)
            .inputItem(BRONZE.tag("rotor"), 1)
            .inputItem(ITEM_FILTER, 3)
            .inputItem(PE.tag("sheet"), 3)
            .inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(2))
            .workTicks(200L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.CLEANROOM)
            .build()
            .recipe(DATA_GEN, INERT_PTFE_CASING)
            .outputItem(INERT_PTFE_CASING, 1)
            .inputItem(SOLID_STEEL_CASING, 1)
            .inputFluid(PTFE.fluid(), PTFE.fluidAmount(1.5f))
            .workTicks(200L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build()
            .recipe(DATA_GEN, PTFE_PIPE_CASING)
            .outputItem(PTFE_PIPE_CASING, 1)
            .inputItem(STEEL.tag("stick"), 2)
            .inputItem(PTFE.tag("pipe"), 2)
            .inputItem(PTFE.tag("sheet"), 2)
            .workTicks(200L)
            .voltage(Voltage.MV)
            .requireTech(Technologies.ADVANCED_CHEMISTRY)
            .build();
    }

    private static void solidRecipe(IEntry<Block> block, Voltage v, MaterialSet mat,
        ResourceLocation tech) {
        ASSEMBLER.recipe(DATA_GEN, block)
            .outputItem(block, 1)
            .inputItem(mat.tag("stick"), 2)
            .inputItem(mat.tag("plate"), 3)
            .workTicks(140L)
            .voltage(v)
            .transform($ -> v != Voltage.ULV ?
                $.inputFluid(SOLDERING_ALLOY.fluid(), SOLDERING_ALLOY.fluidAmount(2)) : $)
            .requireTech(tech)
            .build();
    }

    private static void coilRecipe(IEntry<CoilBlock> coil, Voltage v,
        MaterialSet wire, MaterialSet foil, ResourceLocation tech) {
        ASSEMBLER.recipe(DATA_GEN, coil)
            .outputItem(coil, 1)
            .inputItem(wire.tag("wire"), 8 * v.rank)
            .inputItem(foil.tag("foil"), 8 * v.rank)
            .transform($ -> v.rank >= Voltage.MV.rank ?
                $.inputFluid(PE.fluid(), PE.fluidAmount(2f)) : $)
            .workTicks(200L)
            .voltage(v)
            .requireTech(tech)
            .build();
    }
}
