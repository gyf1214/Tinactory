package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMultiBlocks {
    public static final IEntry<PrimitiveBlock> BLAST_FURNACE;
    public static final IEntry<PrimitiveBlock> SIFTER;
    public static final IEntry<PrimitiveBlock> VACUUM_FREEZER;

    // solid blocks
    public static final Set<IEntry<Block>> SOLID_CASING;
    public static final IEntry<Block> HEATPROOF_CASING;
    public static final IEntry<Block> SOLID_STEEL_CASING;
    public static final IEntry<Block> FROST_PROOF_CASING;
    // coil blocks
    public static final Set<IEntry<CoilBlock>> COIL_BLOCKS;
    public static final IEntry<CoilBlock> CUPRONICKEL_COIL_BLOCK;
    public static final IEntry<CoilBlock> KANTHAL_COIL_BLOCK;
    // misc
    public static final IEntry<Block> GRATE_MACHINE_CASING;

    private static final Transformer<BlockBehaviour.Properties> CASING_PROPERTY;

    static {
        CASING_PROPERTY = $ -> $.strength(2f, 8f).requiresCorrectToolForDrops();

        SOLID_CASING = new HashSet<>();
        HEATPROOF_CASING = solid("heatproof");
        SOLID_STEEL_CASING = solid("solid_steel");
        FROST_PROOF_CASING = solid("frost_proof");

        COIL_BLOCKS = new HashSet<>();
        CUPRONICKEL_COIL_BLOCK = coil("cupronickel", 1800);
        KANTHAL_COIL_BLOCK = coil("kanthal", 2700);

        GRATE_MACHINE_CASING = REGISTRATE.block(
                "multi_block/grate_machine_casing", Block::new)
            .properties(CASING_PROPERTY)
            .register();

        BLAST_FURNACE = multiBlock("blast_furnace")
            .blockEntity()
            .transform(RecipeProcessor::blastFurnace)
            .child(MultiBlock::blastFurnace)
            .layout(AllLayouts.BLAST_FURNACE)
            .appearanceBlock(HEATPROOF_CASING)
            .spec()
            .layer()
            .row("BBB")
            .row("BBB")
            .row("B$B").build()
            .layer().height(2)
            .row("CCC")
            .row("CAC")
            .row("CCC").build()
            .layer()
            .row("TTT")
            .row("TTT")
            .row("TTT").build()
            .blockOrInterface('B', HEATPROOF_CASING)
            .sameBlockWithTag('C', "coil", AllTags.COIL).air('A')
            .block('T', HEATPROOF_CASING)
            .build()
            .build()
            .end()
            .buildObject();

        SIFTER = multiBlock("sifter")
            .blockEntity()
            .transform(RecipeProcessor.multiBlock(
                AllRecipes.SIFTER, true))
            .child(MultiBlock::simple)
            .layout(AllLayouts.SIFTER)
            .appearanceBlock(SOLID_STEEL_CASING)
            .spec()
            .layer()
            .empty()
            .row(" BBB ")
            .row(" BBB ")
            .row(" BBB ")
            .empty().build()
            .layer()
            .empty()
            .row(" CCC ")
            .row(" CAC ")
            .row(" CCC ")
            .empty().build()
            .layer()
            .row(" CCC ")
            .row("CAAAC")
            .row("CAAAC")
            .row("CAAAC")
            .row(" CCC ").build()
            .layer()
            .row(" CCC ")
            .row("CGGGC")
            .row("CGGGC")
            .row("CGGGC")
            .row(" C$C ").build()
            .layer()
            .row(" CCC ")
            .row("CGGGC")
            .row("CGGGC")
            .row("CGGGC")
            .row(" CCC ").build()
            .blockOrInterface('B', SOLID_STEEL_CASING)
            .block('C', SOLID_STEEL_CASING)
            .block('G', GRATE_MACHINE_CASING)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();

        VACUUM_FREEZER = multiBlock("vacuum_freezer")
            .blockEntity()
            .transform(RecipeProcessor.multiBlock(
                AllRecipes.VACUUM_FREEZER, true))
            .child(MultiBlock::simple)
            .appearanceBlock(FROST_PROOF_CASING)
            .spec()
            .layer()
            .row("BBB")
            .row("BBB")
            .row("B$B")
            .build()
            .layer()
            .row("CCC")
            .row("CAC")
            .row("CCC")
            .build()
            .layer()
            .row("CCC")
            .row("CCC")
            .row("CCC")
            .build()
            .blockOrInterface('B', FROST_PROOF_CASING)
            .block('C', FROST_PROOF_CASING)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();
    }

    private static BlockEntityBuilder<PrimitiveBlock, ?> multiBlock(String name) {
        return BlockEntityBuilder.builder("multi_block/" + name, PrimitiveBlock::new)
            .translucent();
    }

    private static IEntry<Block> solid(String name) {
        var ret = REGISTRATE.block("multi_block/solid/" + name, Block::new)
            .properties(CASING_PROPERTY)
            .register();
        SOLID_CASING.add(ret);
        return ret;
    }

    private static IEntry<CoilBlock> coil(String name, int temperature) {
        var ret = REGISTRATE.block("multi_block/coil/" + name, CoilBlock.factory(temperature))
            .properties(CASING_PROPERTY)
            .register();
        COIL_BLOCKS.add(ret);
        return ret;
    }

    public static void init() {}
}
