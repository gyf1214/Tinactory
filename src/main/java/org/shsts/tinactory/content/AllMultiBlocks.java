package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlock;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMultiBlocks {
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> BLAST_FURNACE;

    // solid blocks
    public static final Set<RegistryEntry<Block>> SOLID_CASING;
    public static final RegistryEntry<Block> HEATPROOF_CASING;
    public static final RegistryEntry<Block> SOLID_STEEL_CASING;
    // coil blocks
    public static final Set<RegistryEntry<CoilBlock>> COIL_BLOCKS;
    public static final RegistryEntry<CoilBlock> CUPRONICKEL_COIL_BLOCK;
    public static final RegistryEntry<CoilBlock> KANTHAL_COIL_BLOCK;

    static {
        SOLID_CASING = new HashSet<>();
        HEATPROOF_CASING = solid("heatproof");
        SOLID_STEEL_CASING = solid("solid_steel");

        COIL_BLOCKS = new HashSet<>();
        CUPRONICKEL_COIL_BLOCK = AllMultiBlocks.coil("cupronickel", 1800);
        KANTHAL_COIL_BLOCK = AllMultiBlocks.coil("kanthal", 2700);

        BLAST_FURNACE = REGISTRATE.blockEntity("multi_block/blast_furnace",
                        PrimitiveBlock<SmartBlockEntity>::new)
                .blockEntity()
                .eventManager().ticking()
                .capability(MultiBlock::blastFurnace)
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
                .row("TTT")
                .build()
                .blockOrInterface('B', HEATPROOF_CASING)
                .sameBlockWithTag('C', "coil", AllTags.COIL).air('A')
                .block('T', HEATPROOF_CASING)
                .build()
                .build()
                .simpleCapability(RecipeProcessor::blastFurnace)
                .build()
                .translucent()
                .buildObject();
    }

    private static RegistryEntry<Block> solid(String name) {
        var ret = REGISTRATE.block("multi_block/solid/" + name, Block::new)
                .properties($ -> $.strength(2f, 8f))
                .register();
        SOLID_CASING.add(ret);
        return ret;
    }

    private static RegistryEntry<CoilBlock> coil(String name, int temperature) {
        var ret = REGISTRATE.block("multi_block/coil/" + name, CoilBlock.factory(temperature))
                .properties($ -> $.strength(2f, 8f))
                .register();
        COIL_BLOCKS.add(ret);
        return ret;
    }

    public static void init() {}
}
