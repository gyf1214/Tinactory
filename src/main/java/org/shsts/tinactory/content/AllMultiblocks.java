package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.network.FixedBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashSet;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_CONNECTOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_WALL;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMultiblocks {
    public static final IEntry<PrimitiveBlock> BLAST_FURNACE;
    public static final IEntry<PrimitiveBlock> SIFTER;
    public static final IEntry<PrimitiveBlock> VACUUM_FREEZER;
    public static final IEntry<PrimitiveBlock> DISTILLATION_TOWER;
    public static final IEntry<FixedBlock> CLEANROOM;

    // solid blocks
    public static final Set<IEntry<Block>> SOLID_CASINGS;
    public static final IEntry<Block> HEATPROOF_CASING;
    public static final IEntry<Block> SOLID_STEEL_CASING;
    public static final IEntry<Block> FROST_PROOF_CASING;
    public static final IEntry<Block> CLEAN_STAINLESS_CASING;
    public static final IEntry<Block> PLASCRETE;
    public static final IEntry<Block> FILTER_CASING;
    // coil blocks
    public static final Set<IEntry<CoilBlock>> COIL_BLOCKS;
    public static final IEntry<CoilBlock> CUPRONICKEL_COIL_BLOCK;
    public static final IEntry<CoilBlock> KANTHAL_COIL_BLOCK;
    // misc
    public static final IEntry<Block> GRATE_MACHINE_CASING;

    private static final Transformer<BlockBehaviour.Properties> CASING_PROPERTY;

    static {
        CASING_PROPERTY = $ -> $.strength(2f, 8f).requiresCorrectToolForDrops();

        SOLID_CASINGS = new HashSet<>();
        HEATPROOF_CASING = solid("heatproof");
        SOLID_STEEL_CASING = solid("solid_steel");
        FROST_PROOF_CASING = solid("frost_proof");
        CLEAN_STAINLESS_CASING = solid("clean_stainless_steel");
        PLASCRETE = solid("plascrete", false);
        FILTER_CASING = solid("filter_casing", false);

        COIL_BLOCKS = new HashSet<>();
        CUPRONICKEL_COIL_BLOCK = coil("cupronickel", 1800);
        KANTHAL_COIL_BLOCK = coil("kanthal", 2700);

        GRATE_MACHINE_CASING = REGISTRATE.block(
                "multiblock/grate_machine_casing", Block::new)
            .properties(CASING_PROPERTY)
            .register();

        BLAST_FURNACE = multiblock("blast_furnace")
            .blockEntity()
            .child(Multiblock::blastFurnace)
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

        SIFTER = multiblock("sifter")
            .blockEntity()
            .child(Multiblock.simple(AllRecipes.SIFTER, true))
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

        VACUUM_FREEZER = multiblock("vacuum_freezer")
            .blockEntity()
            .child(Multiblock.simple(AllRecipes.VACUUM_FREEZER, true))
            .layout(AllLayouts.VACUUM_FREEZER)
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

        DISTILLATION_TOWER = multiblock("distillation_tower")
            .blockEntity()
            .child(Multiblock::distillationTower)
            .appearanceBlock(CLEAN_STAINLESS_CASING)
            .spec()
            .layer()
            .row("BBB")
            .row("BBB")
            .row("B$B")
            .build()
            .layer().height(1, 6)
            .row("CCC")
            .row("CAC")
            .row("CCC")
            .build()
            .layer()
            .row("CCC")
            .row("CCC")
            .row("CCC")
            .build()
            .blockOrInterface('B', CLEAN_STAINLESS_CASING)
            .block('C', CLEAN_STAINLESS_CASING)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();

        CLEANROOM = BlockEntityBuilder.builder("multiblock/cleanroom", FixedBlock::new)
            .translucent()
            .blockEntity()
            .child(Multiblock::cleanroom)
            .appearanceBlock(PLASCRETE)
            .spec(Cleanroom::spec)
            .baseBlock(PLASCRETE)
            .ceilingBlock(FILTER_CASING)
            .wallTag(CLEANROOM_WALL)
            .connectorTag(CLEANROOM_CONNECTOR)
            .doorTag(CLEANROOM_DOOR)
            .maxSize(7)
            .maxConnector(1)
            .maxDoor(2)
            .build()
            .build()
            .end()
            .buildObject();
    }

    private static BlockEntityBuilder<PrimitiveBlock, ?> multiblock(String name) {
        return BlockEntityBuilder.builder("multiblock/" + name, PrimitiveBlock::new)
            .translucent();
    }

    private static IEntry<Block> solid(String name, boolean addToSet) {
        var ret = REGISTRATE.block("multiblock/solid/" + name, Block::new)
            .properties(CASING_PROPERTY)
            .register();
        if (addToSet) {
            SOLID_CASINGS.add(ret);
        }
        return ret;
    }

    private static IEntry<Block> solid(String name) {
        return solid(name, true);
    }

    private static IEntry<CoilBlock> coil(String name, int temperature) {
        var ret = REGISTRATE.block("multiblock/coil/" + name, CoilBlock.factory(temperature))
            .properties(CASING_PROPERTY)
            .register();
        COIL_BLOCKS.add(ret);
        return ret;
    }

    public static void init() {}
}
