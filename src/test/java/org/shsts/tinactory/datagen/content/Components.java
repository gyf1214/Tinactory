package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.electric.CircuitComponentTier;
import org.shsts.tinactory.content.electric.CircuitTier;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.registrate.common.RegistryEntry;

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
import static org.shsts.tinactory.content.AllItems.CHIPS;
import static org.shsts.tinactory.content.AllItems.COMPONENT_ITEMS;
import static org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE;
import static org.shsts.tinactory.content.AllItems.DIODE;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP;
import static org.shsts.tinactory.content.AllItems.ELECTRONIC_CIRCUIT;
import static org.shsts.tinactory.content.AllItems.EMITTER;
import static org.shsts.tinactory.content.AllItems.FLUID_CELL;
import static org.shsts.tinactory.content.AllItems.GOOD_BUZZSAW;
import static org.shsts.tinactory.content.AllItems.GOOD_ELECTRONIC;
import static org.shsts.tinactory.content.AllItems.GOOD_GRINDER;
import static org.shsts.tinactory.content.AllItems.GOOD_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.ITEM_FILTER;
import static org.shsts.tinactory.content.AllItems.MACHINE_HULL;
import static org.shsts.tinactory.content.AllItems.RAW_WAFERS;
import static org.shsts.tinactory.content.AllItems.RESEARCH_EQUIPMENT;
import static org.shsts.tinactory.content.AllItems.RESISTOR;
import static org.shsts.tinactory.content.AllItems.ROBOT_ARM;
import static org.shsts.tinactory.content.AllItems.SENSOR;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllItems.TRANSISTOR;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllItems.WAFERS;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BATTERY_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.BRASS;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CADMIUM;
import static org.shsts.tinactory.content.AllMaterials.COAL;
import static org.shsts.tinactory.content.AllMaterials.COBALT_BRASS;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.DIAMOND;
import static org.shsts.tinactory.content.AllMaterials.ELECTRUM;
import static org.shsts.tinactory.content.AllMaterials.GALLIUM_ARSENIDE;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.RUBBER;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.SILICON;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.ZINC;
import static org.shsts.tinactory.content.AllMultiBlocks.COIL_BLOCKS;
import static org.shsts.tinactory.content.AllMultiBlocks.CUPRONICKEL_COIL_BLOCK;
import static org.shsts.tinactory.content.AllMultiBlocks.GRATE_MACHINE_CASING;
import static org.shsts.tinactory.content.AllMultiBlocks.HEATPROOF_CASING;
import static org.shsts.tinactory.content.AllMultiBlocks.KANTHAL_COIL_BLOCK;
import static org.shsts.tinactory.content.AllMultiBlocks.SOLID_CASING;
import static org.shsts.tinactory.content.AllMultiBlocks.SOLID_STEEL_CASING;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CIRCUIT_ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.CUTTER;
import static org.shsts.tinactory.content.AllRecipes.LASER_ENGRAVER;
import static org.shsts.tinactory.content.AllRecipes.LATHE;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.AllTags.COIL;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_CUTTER;
import static org.shsts.tinactory.content.AllTags.MINEABLE_WITH_WRENCH;
import static org.shsts.tinactory.content.AllTags.TOOL_HAMMER;
import static org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER;
import static org.shsts.tinactory.content.AllTags.TOOL_WRENCH;
import static org.shsts.tinactory.core.util.LocHelper.name;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.datagen.DataGen.DATA_GEN;
import static org.shsts.tinactory.datagen.content.Models.basicItem;
import static org.shsts.tinactory.datagen.content.Models.machineItem;
import static org.shsts.tinactory.datagen.content.Models.solidBlock;
import static org.shsts.tinactory.datagen.content.model.MachineModel.IO_TEX;

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
        miscRecipes();
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

        Stream.concat(BOULES.stream(), RAW_WAFERS.stream()).forEach(entry -> DATA_GEN.item(entry)
            .model(basicItem("metaitems/" + entry.id
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
            .build();
    }

    private static void chip(String name, String tex) {
        List.of(WAFERS.get(name), CHIPS.get(name)).forEach(entry -> DATA_GEN.item(entry)
            .model(basicItem("metaitems/" + entry.id
                .replace('/', '.')
                .replace("chip.", "plate.")
                .replace(name, tex)))
            .build());
    }

    private static void circuits() {
        Circuits.forEach((tier, level, item) -> DATA_GEN.item(item)
            .model(basicItem("metaitems/" + item.id.replace('/', '.')))
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

            DATA_GEN.item(Circuits.board(tier))
                .model(basicItem("metaitems/board." + boardName))
                .build()
                .item(Circuits.circuitBoard(tier))
                .model(basicItem("metaitems/circuit_board." + tier.circuitBoard))
                .build();
        }
    }

    private static void misc() {
        DATA_GEN.item(STICKY_RESIN)
            .model(basicItem("metaitems/rubber_drop"))
            .build();

        SOLID_CASING.forEach(block -> DATA_GEN.block(block)
            .blockState(solidBlock("casings/solid/machine_casing_" + name(block.id, -1)))
            .tag(MINEABLE_WITH_WRENCH)
            .build());

        COIL_BLOCKS.forEach(coil -> DATA_GEN.block(coil)
            .blockState(solidBlock("casings/coils/machine_coil_" + name(coil.id, -1)))
            .tag(COIL, MINEABLE_WITH_WRENCH)
            .build());

        DATA_GEN.block(GRATE_MACHINE_CASING)
            .blockState(solidBlock("casings/pipe/grate_steel_front/top"))
            .tag(MINEABLE_WITH_WRENCH)
            .build();

        FLUID_CELL.forEach((v, item) -> {
            var texBase = v == Voltage.ULV ? "metaitems/fluid_cell" :
                "metaitems/large_fluid_cell." + name(item.id, -1);
            DATA_GEN.item(item)
                .model(basicItem(texBase + "/base", texBase + "/overlay"))
                .build();
        });
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
            .build();

        TOOL_CRAFTING.recipe(DATA_GEN, FLUID_CELL.get(Voltage.ULV))
            .result(FLUID_CELL.get(Voltage.ULV), 1)
            .pattern("###").pattern("#G#").pattern(" # ")
            .define('#', IRON.tag("plate"))
            .define('G', GLASS.tag("primary"))
            .toolTag(TOOL_HAMMER, TOOL_WRENCH)
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
                    .outputItem(2, cable, 1)
                    .input(0, () -> new ProcessingIngredients.TagIngredient(cable.get().material.tag("wire"), 4))
                    .inputFluid(1, RUBBER.fluidEntry(), RUBBER.fluidAmount(2))
                    .voltage(v == Voltage.LV ? Voltage.ULV : Voltage.LV)
                    .requireTech(Technologies.HOT_WORKING)
                    .workTicks(100L)
                    .build();
            }
        });

        componentRecipe(Voltage.LV, STEEL, COPPER, BRONZE, TIN, STEEL, BRASS, GLASS);
        componentRecipe(Voltage.MV, ALUMINIUM, CUPRONICKEL, BRASS, BRONZE, STEEL, ELECTRUM, RUBY);

        batteryRecipe(Voltage.LV, CADMIUM);
        // TODO: Na
        batteryRecipe(Voltage.MV, CADMIUM);
        // TODO: Li
        batteryRecipe(Voltage.HV, CADMIUM);

        buzzsawRecipe(BASIC_BUZZSAW, COBALT_BRASS, Voltage.LV);

        researchRecipe(Voltage.ULV)
            .inputItem(0, IRON.tag("plate"), 1)
            .inputItem(0, COPPER.tag("wire"), 1)
            .build();

        researchRecipe(Voltage.LV)
            .inputItem(0, ELECTRIC_MOTOR.get(Voltage.LV), 1)
            .inputItem(0, STEEL.tag("gear"), 1)
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
            Map<Voltage, ? extends RegistryEntry<? extends ItemLike>> component, int count) {
            if (!component.containsKey(voltage)) {
                return new AssemblyRecipeBuilder<>(this);
            }
            var builder = ASSEMBLER.recipe(DATA_GEN, component.get(voltage))
                .outputItem(2, component.get(voltage), count)
                .voltage(baseVoltage)
                .workTicks(ASSEMBLY_TICKS);
            return new AssemblyRecipeBuilder<>(this, voltage, builder);
        }

        public AssemblyRecipeBuilder<ComponentRecipeFactory> recipe(
            Map<Voltage, ? extends RegistryEntry<? extends ItemLike>> component) {
            return recipe(component, 1);
        }
    }

    private static void componentRecipe(Voltage voltage, MaterialSet main,
        MaterialSet heat, MaterialSet pipe,
        MaterialSet rotor, MaterialSet magnetic,
        MaterialSet sensor, MaterialSet quartz) {
        var factory = new ComponentRecipeFactory(voltage);

        factory.recipe(ELECTRIC_MOTOR)
            .material(magnetic, "magnetic", 1)
            .material(main, "stick", 1)
            .material(heat, "wire", 2 * voltage.rank)
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
            // TODO: plastic
            .build()
            .recipe(FLUID_CELL)
            .material(main, "plate", voltage.rank * 2)
            .material(rotor, "ring", voltage.rank)
            .materialFluid(SOLDERING_ALLOY, voltage.rank)
            .transformBuilder($ -> $.voltage(Voltage.LV))
            .build();
    }

    private static void batteryRecipe(Voltage voltage, MaterialSet material) {
        var wires = voltage.rank - 1;
        var plates = wires * wires;

        var builder = ASSEMBLER.recipe(DATA_GEN, BATTERY.get(voltage))
            .outputItem(2, BATTERY.get(voltage), 1);
        if (voltage.rank > Voltage.LV.rank) {
            var bat1 = AllTags.battery(Voltage.fromRank(voltage.rank - 1));
            builder.inputItem(0, bat1, 2);
        }
        builder.inputItem(0, CABLE.get(voltage), wires)
            .inputItem(0, BATTERY_ALLOY.tag("plate"), plates)
            .inputItem(0, material.tag("dust"), plates)
            .inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(wires))
            .voltage(Voltage.LV)
            .workTicks(ASSEMBLY_TICKS)
            .build();
    }

    private static AssemblyRecipe.Builder researchRecipe(Voltage voltage) {
        return ASSEMBLER.recipe(DATA_GEN, RESEARCH_EQUIPMENT.get(voltage))
            .outputItem(2, RESEARCH_EQUIPMENT.get(voltage), 1)
            .workTicks(200L)
            .voltage(voltage);
    }

    private static void buzzsawRecipe(RegistryEntry<Item> item, MaterialSet material, Voltage v) {
        LATHE.recipe(DATA_GEN, item)
            .outputItem(1, item, 1)
            .inputItem(0, material.tag("gear"), 1)
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
            .outputItem(2, VACUUM_TUBE.item(), 1)
            .inputItem(0, GLASS.tag("primary"), 1)
            .inputItem(0, COPPER.tag("wire"), 1)
            .inputItem(0, IRON.tag("bolt"), 1)
            .workTicks(120L)
            .voltage(Voltage.ULV)
            .build();

        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(ELECTRONIC_CIRCUIT.getItem())
            .pattern("RPR").pattern("TBT").pattern("WWW")
            .define('R', RESISTOR.getItem(CircuitComponentTier.NORMAL))
            .define('P', STEEL.tag("plate"))
            .define('T', VACUUM_TUBE.getItem())
            .define('B', Circuits.circuitBoard(CircuitTier.ELECTRONIC).get())
            .define('W', RED_ALLOY.tag("wire"))
            .unlockedBy("has_board", has(Circuits.circuitBoard(CircuitTier.ELECTRONIC).get())));

        circuitRecipe(ELECTRONIC_CIRCUIT, VACUUM_TUBE, 2, RESISTOR, 2, RED_ALLOY.tag("wire"), 2);

        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(GOOD_ELECTRONIC.getItem())
            .pattern("DPD").pattern("EBE").pattern("WEW")
            .define('D', DIODE.getItem(CircuitComponentTier.NORMAL))
            .define('P', STEEL.tag("plate"))
            .define('E', ELECTRONIC_CIRCUIT.getItem())
            .define('B', Circuits.circuitBoard(CircuitTier.ELECTRONIC).get())
            .define('W', COPPER.tag("wire"))
            .unlockedBy("has_circuit", has(ELECTRONIC_CIRCUIT.getItem())));

        circuitRecipe(GOOD_ELECTRONIC, ELECTRONIC_CIRCUIT, 2, DIODE, 2, COPPER.tag("wire"), 2);

        circuitRecipe(BASIC_INTEGRATED, CHIPS.get("integrated_circuit"), RESISTOR, 2, DIODE, 2,
            COPPER.tag("wire_fine"), 2, TIN.tag("bolt"), 2);
        circuitRecipe(GOOD_INTEGRATED, BASIC_INTEGRATED, 2, RESISTOR, 2, DIODE, 2,
            GOLD.tag("wire_fine"), 4, SILVER.tag("bolt"), 4);
        circuitRecipe(ADVANCED_INTEGRATED, GOOD_INTEGRATED, 2, CHIPS.get("integrated_circuit"), 2,
            CHIPS.get("ram"), 2, TRANSISTOR, 4, ELECTRUM.tag("wire_fine"), 8, COPPER.tag("bolt"), 8);

        // circuit components
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(RESISTOR.getItem(CircuitComponentTier.NORMAL))
            .pattern(" R ").pattern("WCW").pattern(" R ")
            .define('R', STICKY_RESIN.get())
            .define('W', COPPER.tag("wire"))
            .define('C', COAL.tag("dust"))
            .unlockedBy("has_resin", has(STICKY_RESIN.get())));

        ASSEMBLER.recipe(DATA_GEN, RESISTOR.item(CircuitComponentTier.NORMAL))
            .outputItem(2, RESISTOR.item(CircuitComponentTier.NORMAL), 1)
            .inputItem(0, COAL.tag("dust"), 1)
            .inputItem(0, STICKY_RESIN, 1)
            .inputItem(0, COPPER.tag("wire_fine"), 4)
            .workTicks(ASSEMBLY_TICKS)
            .build();

        ASSEMBLER.recipe(DATA_GEN, DIODE.item(CircuitComponentTier.NORMAL))
            .outputItem(2, DIODE.item(CircuitComponentTier.NORMAL), 1)
            .inputItem(0, GALLIUM_ARSENIDE.tag("dust"), 1)
            .inputItem(0, GLASS.tag("primary"), 1)
            .inputItem(0, COPPER.tag("wire_fine"), 4)
            .workTicks(ASSEMBLY_TICKS)
            .build();

        ASSEMBLER.recipe(DATA_GEN, suffix(DIODE.loc(CircuitComponentTier.NORMAL), "_from_wafer"))
            .outputItem(2, DIODE.item(CircuitComponentTier.NORMAL), 1)
            .inputItem(0, GALLIUM_ARSENIDE.tag("dust"), 1)
            .inputItem(0, GLASS.tag("primary"), 1)
            .inputItem(0, COPPER.tag("wire_fine"), 4)
            .workTicks(ASSEMBLY_TICKS)
            .build();

        // boards
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(Circuits.board(CircuitTier.ELECTRONIC).get(), 3)
            .pattern("SSS").pattern("WWW").pattern("SSS")
            .define('S', STICKY_RESIN.get())
            .define('W', ItemTags.PLANKS)
            .unlockedBy("has_resin", has(STICKY_RESIN.get())));

        ASSEMBLER.recipe(DATA_GEN, Circuits.board(CircuitTier.ELECTRONIC))
            .outputItem(2, Circuits.board(CircuitTier.ELECTRONIC), 1)
            .inputItem(0, ItemTags.PLANKS, 1)
            .inputItem(0, STICKY_RESIN, 2)
            .workTicks(200L)
            .voltage(Voltage.ULV)
            .build();

        ASSEMBLER.recipe(DATA_GEN, Circuits.board(CircuitTier.INTEGRATED))
            .outputItem(2, Circuits.board(CircuitTier.INTEGRATED), 1)
            .inputItem(0, Circuits.board(CircuitTier.ELECTRONIC), 2)
            .inputItem(0, RED_ALLOY.tag("wire"), 8)
            .inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(1f))
            .workTicks(200L)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build();

        // circuit boards
        DATA_GEN.vanillaRecipe(() -> ShapedRecipeBuilder
            .shaped(Circuits.circuitBoard(CircuitTier.ELECTRONIC).get())
            .pattern("WWW").pattern("WBW").pattern("WWW")
            .define('B', Circuits.board(CircuitTier.ELECTRONIC).get())
            .define('W', COPPER.tag("wire"))
            .unlockedBy("has_board", has(Circuits.board(CircuitTier.ELECTRONIC).get())));

        ASSEMBLER.recipe(DATA_GEN, Circuits.circuitBoard(CircuitTier.ELECTRONIC))
            .outputItem(2, Circuits.circuitBoard(CircuitTier.ELECTRONIC), 1)
            .inputItem(0, Circuits.board(CircuitTier.ELECTRONIC), 1)
            .inputItem(0, COPPER.tag("wire"), 8)
            .inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(0.5f))
            .workTicks(200L)
            .voltage(Voltage.ULV)
            .build();

        ASSEMBLER.recipe(DATA_GEN, Circuits.circuitBoard(CircuitTier.INTEGRATED))
            .outputItem(2, Circuits.circuitBoard(CircuitTier.INTEGRATED), 1)
            .inputItem(0, Circuits.board(CircuitTier.INTEGRATED), 1)
            .inputItem(0, SILVER.tag("wire"), 8)
            .inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(0.5f))
            .workTicks(200L)
            .voltage(Voltage.LV)
            .requireTech(Technologies.INTEGRATED_CIRCUIT)
            .build();

        // boules
        BLAST_FURNACE.recipe(DATA_GEN, BOULES.get(0))
            .outputItem(2, BOULES.get(0), 1)
            .inputItem(0, SILICON.tag("dust"), 32)
            .inputItem(0, GALLIUM_ARSENIDE.tag("dust"), 1)
            .voltage(Voltage.LV)
            .workTicks(6400L)
            .temperature(2100)
            .build();

        // raw wafers
        for (var i = 0; i < RAW_WAFERS.size(); i++) {
            var boule = BOULES.get(i);
            var wafer = RAW_WAFERS.get(i);
            CUTTER.recipe(DATA_GEN, wafer)
                .outputItem(2, wafer, 8 << i)
                .inputItem(0, boule, 1)
                .inputFluid(1, Fluids.WATER, 1000 << i)
                .voltage(Voltage.fromRank(2 + 2 * i))
                .workTicks(400L << i)
                .build();
        }

        // engraving
        engravingRecipe("integrated_circuit", RUBY, 0, Voltage.LV);
        engravingRecipe("cpu", DIAMOND, 0, Voltage.LV);

        // chips
        for (var entry : CHIPS.entrySet()) {
            var wafer = WAFERS.get(entry.getKey());
            var chip = entry.getValue();
            CUTTER.recipe(DATA_GEN, chip)
                .outputItem(2, chip, 6)
                .inputItem(0, wafer, 1)
                .inputFluid(1, Fluids.WATER, 750)
                .voltage(Voltage.LV)
                .workTicks(300L)
                .build();
        }
    }

    private static void engravingRecipe(String name, MaterialSet lens, int level, Voltage voltage) {
        var wafer = WAFERS.get(name);
        for (var i = 0; i < RAW_WAFERS.size(); i++) {
            if (i < level) {
                continue;
            }
            var j = i - level;
            var raw = RAW_WAFERS.get(i);
            LASER_ENGRAVER.recipe(DATA_GEN, suffix(wafer.loc, "_from_" + name(raw.id, -1)))
                .outputItem(2, wafer, 1 << j)
                .inputItem(0, raw, 1)
                .inputItemNotConsumed(1, lens.tag("lens"))
                .voltage(Voltage.fromRank(voltage.rank + j * 2))
                .workTicks(1000L << level)
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
            .outputItem(2, circuit.item(), output);

        if (circuit.level().voltageOffset < 2) {
            builder.inputItem(0, circuit.circuitBoard(), 1);
        }

        for (; i < args.length; i++) {
            var item = args[i];
            var count = 1;
            if (i + 1 < args.length && args[i + 1] instanceof Integer k) {
                count = k;
                i++;
            }
            if (item instanceof TagKey<?>) {
                builder.inputItem(0, (TagKey<Item>) item, count);
            } else if (item instanceof Circuits.CircuitComponent component) {
                builder.inputItem(0, component.tag(circuit.tier().componentTier), count);
            } else if (item instanceof Circuits.Circuit circuit1) {
                builder.inputItem(0, circuit1.item(), count);
            } else if (item instanceof ItemLike itemLike) {
                builder.inputItem(0, () -> itemLike, count);
            } else if (item instanceof Supplier<?>) {
                builder.inputItem(0, (Supplier<? extends ItemLike>) item, count);
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
            .inputFluid(1, SOLDERING_ALLOY.fluidEntry(),
                SOLDERING_ALLOY.fluidAmount((1 << (level - 1)) / 2f))
            .workTicks(200L * level)
            .build();
    }

    private static void miscRecipes() {
        solidRecipe(HEATPROOF_CASING, Voltage.ULV, INVAR, Technologies.STEEL);
        solidRecipe(SOLID_STEEL_CASING, Voltage.LV, STEEL, Technologies.STEEL);

        coilRecipe(CUPRONICKEL_COIL_BLOCK, Voltage.ULV, CUPRONICKEL, BRONZE, Technologies.STEEL);
        coilRecipe(KANTHAL_COIL_BLOCK, Voltage.LV, KANTHAL, SILVER, Technologies.KANTHAL);

        ASSEMBLER.recipe(DATA_GEN, ITEM_FILTER)
            .outputItem(2, ITEM_FILTER, 1)
            .inputItem(0, STEEL.tag("plate"), 1)
            .inputItem(0, ZINC.tag("foil"), 8)
            .voltage(Voltage.LV)
            .workTicks(ASSEMBLY_TICKS)
            .requireTech(Technologies.SIFTING)
            .build()
            .recipe(DATA_GEN, GRATE_MACHINE_CASING)
            .outputItem(2, GRATE_MACHINE_CASING, 2)
            .inputItem(0, STEEL.tag("stick"), 4)
            .inputItem(0, ELECTRIC_MOTOR.get(Voltage.LV), 1)
            .inputItem(0, TIN.tag("rotor"), 1)
            .inputItem(0, ITEM_FILTER, 6)
            .inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(2))
            .voltage(Voltage.LV)
            .workTicks(140L)
            .requireTech(Technologies.SIFTING)
            .build();
    }

    private static void solidRecipe(RegistryEntry<Block> block, Voltage v, MaterialSet mat,
        ResourceLocation tech) {
        ASSEMBLER.recipe(DATA_GEN, block)
            .outputItem(2, block, 1)
            .inputItem(0, mat.entry("plate"), 3)
            .inputItem(0, mat.entry("stick"), 2)
            .workTicks(140L)
            .voltage(v)
            .transform($ -> v != Voltage.ULV ?
                $.inputFluid(1, SOLDERING_ALLOY.fluidEntry(), SOLDERING_ALLOY.fluidAmount(2)) : $)
            .requireTech(tech)
            .build();
    }

    private static void coilRecipe(RegistryEntry<CoilBlock> coil, Voltage v,
        MaterialSet wire, MaterialSet foil, ResourceLocation tech) {
        ASSEMBLER.recipe(DATA_GEN, coil)
            .outputItem(2, coil, 1)
            .inputItem(0, wire.entry("wire"), 8 * v.rank)
            .inputItem(0, foil.entry("foil"), 8 * v.rank)
            .workTicks(200L)
            .voltage(v)
            .requireTech(tech)
            .build();
    }
}
