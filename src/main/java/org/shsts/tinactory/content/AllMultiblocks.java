package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.multiblock.HalfBlock;
import org.shsts.tinactory.content.multiblock.LensBlock;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.content.network.FixedBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.AllMaterials.getMaterial;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_CONNECTOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_DOOR;
import static org.shsts.tinactory.content.AllTags.CLEANROOM_WALL;
import static org.shsts.tinactory.content.machine.MachineMeta.MACHINE_PROPERTY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMultiblocks {
    public static final Map<String, MultiblockSet> MULTIBLOCK_SETS;
    public static final IEntry<FixedBlock> CLEANROOM;

    // solid blocks
    public static final Set<IEntry<Block>> SOLID_CASINGS;
    public static final IEntry<Block> HEATPROOF_CASING;
    public static final IEntry<Block> SOLID_STEEL_CASING;
    public static final IEntry<Block> FROST_PROOF_CASING;
    public static final IEntry<Block> CLEAN_STAINLESS_CASING;
    public static final IEntry<Block> INERT_PTFE_CASING;
    public static final IEntry<Block> STABLE_TITANIUM_CASING;
    // coil blocks
    public static final Map<String, IEntry<CoilBlock>> COIL_BLOCKS;
    public static final IEntry<CoilBlock> CUPRONICKEL_COIL_BLOCK;
    public static final IEntry<CoilBlock> KANTHAL_COIL_BLOCK;
    public static final IEntry<CoilBlock> NICHROME_COIL_BLOCK;
    public static final IEntry<CoilBlock> TUNGSTEN_COIL_BLOCK;
    // misc
    public static final IEntry<Block> GRATE_MACHINE_CASING;
    public static final IEntry<Block> AUTOFARM_BASE;
    public static final IEntry<GlassBlock> CLEAR_GLASS;
    public static final IEntry<Block> PLASCRETE;
    public static final IEntry<Block> FILTER_CASING;
    public static final IEntry<Block> PTFE_PIPE_CASING;
    public static final IEntry<HalfBlock> LAUNCH_SITE_BASE;
    public static final IEntry<LensBlock> BASIC_LITHOGRAPHY_LENS;
    public static final IEntry<LensBlock> GOOD_LITHOGRAPHY_LENS;

    private static final Transformer<BlockBehaviour.Properties> CASING_PROPERTY;

    static {
        CASING_PROPERTY = $ -> $.strength(3f, 8f)
            .sound(SoundType.METAL)
            .requiresCorrectToolForDrops()
            .isValidSpawn(AllItems::never);

        SOLID_CASINGS = new HashSet<>();
        HEATPROOF_CASING = solid("heatproof");
        SOLID_STEEL_CASING = solid("solid_steel");
        FROST_PROOF_CASING = solid("frost_proof");
        CLEAN_STAINLESS_CASING = solid("clean_stainless_steel");
        INERT_PTFE_CASING = solid("inert_ptfe");
        STABLE_TITANIUM_CASING = solid("stable_titanium");

        COIL_BLOCKS = new HashMap<>();
        CUPRONICKEL_COIL_BLOCK = coil("cupronickel", 1800);
        KANTHAL_COIL_BLOCK = coil("kanthal", 2700);
        NICHROME_COIL_BLOCK = coil("nichrome", 3600);
        TUNGSTEN_COIL_BLOCK = coil("tungsten", 4500);

        GRATE_MACHINE_CASING = misc("grate_machine_casing");
        AUTOFARM_BASE = misc("autofarm_base");

        CLEAR_GLASS = REGISTRATE.block("multiblock/misc/clear_glass", GlassBlock::new)
            .transform(AllMultiblocks::glass)
            .register();

        PLASCRETE = misc("plascrete");
        FILTER_CASING = misc("filter_casing");
        PTFE_PIPE_CASING = misc("ptfe_pipe_casing");

        LAUNCH_SITE_BASE = REGISTRATE.block("multiblock/misc/launch_site_base", HalfBlock::new)
            .material(Material.HEAVY_METAL)
            .properties(CASING_PROPERTY)
            .register();

        var basicLens = List.of(
            getMaterial("ruby").entry("lens"),
            getMaterial("diamond").entry("lens"),
            getMaterial("sapphire").entry("lens"),
            getMaterial("emerald").entry("lens"));

        BASIC_LITHOGRAPHY_LENS = REGISTRATE.block("multiblock/misc/lithography_lens/basic",
                props -> new LensBlock(props, basicLens))
            .transform(AllMultiblocks::glass)
            .register();

        var goodLens = new ArrayList<>(basicLens);
        goodLens.add(getMaterial("topaz").entry("lens"));
        goodLens.add(getMaterial("blue_topaz").entry("lens"));

        GOOD_LITHOGRAPHY_LENS = REGISTRATE.block("multiblock/misc/lithography_lens/good",
                props -> new LensBlock(props, goodLens))
            .transform(AllMultiblocks::glass)
            .register();

        MULTIBLOCK_SETS = new HashMap<>();

        CLEANROOM = BlockEntityBuilder.builder("multiblock/cleanroom", FixedBlock::new)
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
            .block()
            .material(Material.HEAVY_METAL)
            .properties(MACHINE_PROPERTY)
            .translucent()
            .end()
            .buildObject();
    }

    private static IEntry<Block> solid(String name) {
        var ret = REGISTRATE.block("multiblock/solid/" + name, Block::new)
            .material(Material.HEAVY_METAL)
            .properties(CASING_PROPERTY)
            .register();
        SOLID_CASINGS.add(ret);
        return ret;
    }

    private static IEntry<CoilBlock> coil(String name, int temperature) {
        var ret = REGISTRATE.block("multiblock/coil/" + name, CoilBlock.factory(temperature))
            .material(Material.HEAVY_METAL)
            .properties(CASING_PROPERTY)
            .register();
        COIL_BLOCKS.put(name, ret);
        return ret;
    }

    private static IEntry<Block> misc(String name) {
        return REGISTRATE.block("multiblock/misc/" + name, Block::new)
            .material(Material.HEAVY_METAL)
            .properties(CASING_PROPERTY)
            .register();
    }

    private static <U extends Block, P> IBlockBuilder<U, P> glass(IBlockBuilder<U, P> builder) {
        return builder.material(Material.BARRIER)
            .properties(CASING_PROPERTY)
            .properties($ -> $.isViewBlocking(AllItems::never)
                .noOcclusion()
                .sound(SoundType.GLASS))
            .translucent();
    }

    public static MultiblockSet getMultiblock(String name) {
        return MULTIBLOCK_SETS.get(name);
    }

    public static void init() {}
}
