package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import static org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR;
import static org.shsts.tinactory.content.AllItems.ITEM_FILTER;
import static org.shsts.tinactory.content.AllMaterials.ALUMINIUM;
import static org.shsts.tinactory.content.AllMaterials.BRONZE;
import static org.shsts.tinactory.content.AllMaterials.CUPRONICKEL;
import static org.shsts.tinactory.content.AllMaterials.GLASS;
import static org.shsts.tinactory.content.AllMaterials.INVAR;
import static org.shsts.tinactory.content.AllMaterials.KANTHAL;
import static org.shsts.tinactory.content.AllMaterials.NICHROME;
import static org.shsts.tinactory.content.AllMaterials.PE;
import static org.shsts.tinactory.content.AllMaterials.PTFE;
import static org.shsts.tinactory.content.AllMaterials.SILVER;
import static org.shsts.tinactory.content.AllMaterials.SOLDERING_ALLOY;
import static org.shsts.tinactory.content.AllMaterials.STAINLESS_STEEL;
import static org.shsts.tinactory.content.AllMaterials.STEEL;
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
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Components {
    public static void init() {
        multiblockRecipes();
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
