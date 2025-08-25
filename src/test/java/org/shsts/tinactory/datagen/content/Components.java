package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.electric.CircuitTier;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.function.Supplier;

import static org.shsts.tinactory.content.AllItems.ADVANCED_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.BASIC_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.CAPACITOR;
import static org.shsts.tinactory.content.AllItems.CHIPS;
import static org.shsts.tinactory.content.AllItems.DIODE;
import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ELECTRONIC_CIRCUIT;
import static org.shsts.tinactory.content.AllItems.GOOD_ELECTRONIC;
import static org.shsts.tinactory.content.AllItems.GOOD_INTEGRATED;
import static org.shsts.tinactory.content.AllItems.INDUCTOR;
import static org.shsts.tinactory.content.AllItems.INTEGRATED_PROCESSOR;
import static org.shsts.tinactory.content.AllItems.ITEM_FILTER;
import static org.shsts.tinactory.content.AllItems.MAINFRAME;
import static org.shsts.tinactory.content.AllItems.MICROPROCESSOR;
import static org.shsts.tinactory.content.AllItems.PROCESSOR_ASSEMBLY;
import static org.shsts.tinactory.content.AllItems.RAW_WAFERS;
import static org.shsts.tinactory.content.AllItems.RESISTOR;
import static org.shsts.tinactory.content.AllItems.STICKY_RESIN;
import static org.shsts.tinactory.content.AllItems.TRANSISTOR;
import static org.shsts.tinactory.content.AllItems.VACUUM_TUBE;
import static org.shsts.tinactory.content.AllItems.WAFERS;
import static org.shsts.tinactory.content.AllItems.WORKSTATION;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.COPPER;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.DIAMOND;
import static org.shsts.tinactory.content.AllMaterials.ELECTRUM;
import static org.shsts.tinactory.content.AllMaterials.EMERALD;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.GOLD;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.IRON_CHLORIDE;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.NICHROME;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.PTFE;
import static org.shsts.tinactory.content.AllMaterials.PVC;
import static org.shsts.tinactory.content.AllMaterials.RED_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.RUBY;
import static org.shsts.tinactory.content.AllMaterials.SAPPHIRE;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STAINLESS_STEEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
import static org.shsts.tinactory.content.AllMaterials.SULFURIC_ACID;
import static org.shsts.tinactory.content.AllMaterials.TIN;
import static org.shsts.tinactory.content.AllMaterials.ZINC;
import static org.shsts.tinactory.content.AllMultiblocks.AUTOFARM_BASE;
import static org.shsts.tinactory.content.AllMultiblocks.CLEAN_STAINLESS_CASING;
import static org.shsts.tinactory.content.AllMultiblocks.CLEAR_GLASS;
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
import static org.shsts.tinactory.content.AllMultiblocks.SOLID_STEEL_CASING;
import static org.shsts.tinactory.content.AllRecipes.ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.CHEMICAL_REACTOR;
import static org.shsts.tinactory.content.AllRecipes.CIRCUIT_ASSEMBLER;
import static org.shsts.tinactory.content.AllRecipes.LASER_ENGRAVER;
import static org.shsts.tinactory.content.AllRecipes.has;
import static org.shsts.tinactory.content.electric.Circuits.board;
import static org.shsts.tinactory.content.electric.Circuits.circuitBoard;
import static org.shsts.tinactory.core.util.LocHelper.name;
import static org.shsts.tinactory.core.util.LocHelper.suffix;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Components {
    public static void init() {
        circuitRecipes();
        multiblockRecipes();
    }

    private static void circuitRecipes() {
        circuitRecipe(ELECTRONIC_CIRCUIT, VACUUM_TUBE, 2, RESISTOR, 2, RED_ALLOY.tag("wire"), 2);

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

        // engraving
        engravingRecipe("integrated_circuit", RUBY, 0, Voltage.LV, -1d, 0d);
        engravingRecipe("cpu", DIAMOND, 0, Voltage.MV, 0d, 0.5d);
        engravingRecipe("ram", SAPPHIRE, 0, Voltage.MV, -0.25d, 0.25d);
        engravingRecipe("low_pic", EMERALD, 0, Voltage.MV, -0.3d, 0.2d);
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
