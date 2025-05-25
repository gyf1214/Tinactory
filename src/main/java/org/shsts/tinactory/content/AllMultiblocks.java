package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.Tags;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.multiblock.CoilMultiblock;
import org.shsts.tinactory.content.multiblock.DistillationTower;
import org.shsts.tinactory.content.network.FixedBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.machine.RecipeProcessor;
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
    public static final IEntry<PrimitiveBlock> AUTOFARM;
    public static final IEntry<PrimitiveBlock> VACUUM_FREEZER;
    public static final IEntry<PrimitiveBlock> DISTILLATION_TOWER;
    public static final IEntry<PrimitiveBlock> PYROLYSE_OVEN;
    public static final IEntry<FixedBlock> CLEANROOM;
    public static final IEntry<PrimitiveBlock> LARGE_CHEMICAL_REACTOR;

    // solid blocks
    public static final Set<IEntry<Block>> SOLID_CASINGS;
    public static final IEntry<Block> HEATPROOF_CASING;
    public static final IEntry<Block> SOLID_STEEL_CASING;
    public static final IEntry<Block> FROST_PROOF_CASING;
    public static final IEntry<Block> CLEAN_STAINLESS_CASING;
    public static final IEntry<Block> INERT_PTFE_CASING;
    // coil blocks
    public static final Set<IEntry<CoilBlock>> COIL_BLOCKS;
    public static final IEntry<CoilBlock> CUPRONICKEL_COIL_BLOCK;
    public static final IEntry<CoilBlock> KANTHAL_COIL_BLOCK;
    public static final IEntry<CoilBlock> NICHROME_COIL_BLOCK;
    // misc
    public static final IEntry<Block> GRATE_MACHINE_CASING;
    public static final IEntry<Block> AUTOFARM_BASE;
    public static final IEntry<GlassBlock> CLEAR_GLASS;
    public static final IEntry<Block> PLASCRETE;
    public static final IEntry<Block> FILTER_CASING;
    public static final IEntry<Block> PTFE_PIPE_CASING;

    private static final Transformer<BlockBehaviour.Properties> CASING_PROPERTY;

    static {
        CASING_PROPERTY = $ -> $.strength(2f, 8f)
            .requiresCorrectToolForDrops()
            .isValidSpawn(AllItems::never);

        SOLID_CASINGS = new HashSet<>();
        HEATPROOF_CASING = solid("heatproof");
        SOLID_STEEL_CASING = solid("solid_steel");
        FROST_PROOF_CASING = solid("frost_proof");
        CLEAN_STAINLESS_CASING = solid("clean_stainless_steel");
        INERT_PTFE_CASING = solid("inert_ptfe");

        COIL_BLOCKS = new HashSet<>();
        CUPRONICKEL_COIL_BLOCK = coil("cupronickel", 1800);
        KANTHAL_COIL_BLOCK = coil("kanthal", 2700);
        NICHROME_COIL_BLOCK = coil("nichrome", 3600);

        GRATE_MACHINE_CASING = misc("grate_machine_casing");
        AUTOFARM_BASE = misc("autofarm_base");

        CLEAR_GLASS = REGISTRATE.block("multiblock/misc/clear_glass", GlassBlock::new)
            .material(Material.GLASS)
            .properties(CASING_PROPERTY)
            .properties($ -> $.isViewBlocking(AllItems::never).noOcclusion())
            .renderType(() -> RenderType::cutout)
            .register();

        PLASCRETE = misc("plascrete");
        FILTER_CASING = misc("filter_casing");
        PTFE_PIPE_CASING = misc("ptfe_pipe_casing");

        BLAST_FURNACE = multiblock("blast_furnace")
            .blockEntity()
            .transform(RecipeProcessor::blastFurnace)
            .child(Multiblock.builder(CoilMultiblock::new))
            .layout(AllLayouts.BLAST_FURNACE)
            .appearanceBlock(HEATPROOF_CASING)
            .spec()
            .layer()
            .row('B', 3, 2)
            .row("B$B").build()
            .layer().height(2)
            .row("CCC")
            .row("CAC")
            .row("CCC").build()
            .layer()
            .row('T', 3, 3).build()
            .blockOrInterface('B', HEATPROOF_CASING)
            .block('T', HEATPROOF_CASING)
            .sameBlockWithTag('C', "coil", AllTags.COIL)
            .air('A')
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
            .row(" BBB ", 3)
            .empty().build()
            .layer()
            .empty()
            .row(" CCC ")
            .row(" CAC ")
            .row(" CCC ")
            .empty().build()
            .layer()
            .row(" CCC ")
            .row("CAAAC", 3)
            .row(" CCC ").build()
            .layer()
            .row(" CCC ")
            .row("CGGGC", 3)
            .row(" C$C ").build()
            .layer()
            .row(" CCC ")
            .row("CGGGC", 3)
            .row(" CCC ").build()
            .blockOrInterface('B', SOLID_STEEL_CASING)
            .block('C', SOLID_STEEL_CASING)
            .block('G', GRATE_MACHINE_CASING)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();

        AUTOFARM = multiblock("autofarm")
            .blockEntity()
            .child(Multiblock.simple(AllRecipes.AUTOFARM, true))
            .layout(AllLayouts.AUTOFARM)
            .appearanceBlock(SOLID_STEEL_CASING)
            .spec()
            .layer()
            .row("BBBBB")
            .row("BDDDB", 3)
            .row("BB$BB").build()
            .layer().height(2)
            .row("CWWWC")
            .row("WAAAW", 3)
            .row("CWWWC").build()
            .layer()
            .row("CCCCC")
            .row("CGGGC", 3)
            .row("CCCCC").build()
            .blockOrInterface('B', SOLID_STEEL_CASING)
            .block('D', AUTOFARM_BASE)
            .block('C', SOLID_STEEL_CASING)
            .tag('G', Tags.Blocks.GLASS)
            .checkBlock('W', block -> block.is(Tags.Blocks.GLASS) || block.is(SOLID_STEEL_CASING.get()))
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
            .row('B', 3, 2)
            .row("B$B").build()
            .layer()
            .row("CCC")
            .row("CAC")
            .row("CCC").build()
            .layer()
            .row('C', 3, 3).build()
            .blockOrInterface('B', FROST_PROOF_CASING)
            .block('C', FROST_PROOF_CASING)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();

        DISTILLATION_TOWER = multiblock("distillation_tower")
            .blockEntity()
            .transform(RecipeProcessor.multiblock(AllRecipes.DISTILLATION, true))
            .child(Multiblock.builder(DistillationTower::new))
            .appearanceBlock(CLEAN_STAINLESS_CASING)
            .spec()
            .layer()
            .row('B', 3, 2)
            .row("B$B").build()
            .layer().height(1, 6)
            .row("CCC")
            .row("CAC")
            .row("CCC").build()
            .layer()
            .row('C', 3, 3).build()
            .blockOrInterface('B', CLEAN_STAINLESS_CASING)
            .block('C', CLEAN_STAINLESS_CASING)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();

        PYROLYSE_OVEN = multiblock("pyrolyse_oven")
            .blockEntity()
            .transform(RecipeProcessor.coil(AllRecipes.PYROLYSE_OVEN, false, 1500))
            .child(Multiblock.builder(CoilMultiblock::new))
            .layout(AllLayouts.PYROLYSE_OVEN)
            .appearanceBlock(HEATPROOF_CASING)
            .spec()
            .layer()
            .row("TTT")
            .row("CCC")
            .row("BBB").build()
            .layer()
            .row("TTT")
            .row("CAC")
            .row("B$B").build()
            .layer()
            .row("TTT")
            .row("CCC")
            .row("BBB").build()
            .blockOrInterface('B', HEATPROOF_CASING)
            .block('T', HEATPROOF_CASING)
            .sameBlockWithTag('C', "coil", AllTags.COIL)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();

        CLEANROOM = BlockEntityBuilder.builder("multiblock/cleanroom", FixedBlock::new)
            .translucent()
            .blockEntity()
            .child(Multiblock.builder(Cleanroom::new))
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

        LARGE_CHEMICAL_REACTOR = multiblock("large_chemical_reactor")
            .blockEntity()
            .child(Multiblock.simple(AllRecipes.CHEMICAL_REACTOR, false))
            .layout(AllLayouts.LARGE_CHEMICAL_REACTOR)
            .appearanceBlock(INERT_PTFE_CASING)
            .spec()
            .layer()
            .row("BBB")
            .row("BCB")
            .row("BBB").build()
            .layer()
            .row("BBB")
            .row("BAB")
            .row("B$B").build()
            .layer()
            .row('B', 3, 3).build()
            .blockOrInterface('B', INERT_PTFE_CASING)
            .sameBlockWithTag('C', "coil", AllTags.COIL)
            .air('A')
            .build()
            .build()
            .end()
            .buildObject();
    }

    private static BlockEntityBuilder<PrimitiveBlock, ?> multiblock(String name) {
        return BlockEntityBuilder.builder("multiblock/" + name, PrimitiveBlock::new)
            .translucent();
    }

    private static IEntry<Block> solid(String name) {
        var ret = REGISTRATE.block("multiblock/solid/" + name, Block::new)
            .properties(CASING_PROPERTY)
            .register();
        SOLID_CASINGS.add(ret);
        return ret;
    }

    private static IEntry<CoilBlock> coil(String name, int temperature) {
        var ret = REGISTRATE.block("multiblock/coil/" + name, CoilBlock.factory(temperature))
            .properties(CASING_PROPERTY)
            .register();
        COIL_BLOCKS.add(ret);
        return ret;
    }

    private static IEntry<Block> misc(String name) {
        return REGISTRATE.block("multiblock/misc/" + name, Block::new)
            .properties(CASING_PROPERTY)
            .register();
    }

    public static void init() {}
}
