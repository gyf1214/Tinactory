package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.material.MiscMeta;
import org.shsts.tinactory.content.multiblock.Cleanroom;
import org.shsts.tinactory.content.multiblock.CoilBlock;
import org.shsts.tinactory.content.multiblock.HalfBlock;
import org.shsts.tinactory.content.multiblock.LensBlock;
import org.shsts.tinactory.content.multiblock.MultiblockSet;
import org.shsts.tinactory.content.network.FixedBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinycorelib.api.registrate.builder.IBlockBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static final Map<String, IEntry<Block>> SOLID_CASINGS;
    public static final Map<String, IEntry<CoilBlock>> COIL_BLOCKS;

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

    static {
        SOLID_CASINGS = new HashMap<>();
        COIL_BLOCKS = new HashMap<>();
        MULTIBLOCK_SETS = new HashMap<>();

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
            .properties(MiscMeta.CASING_PROPERTY)
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

    private static IEntry<Block> misc(String name) {
        return REGISTRATE.block("multiblock/misc/" + name, Block::new)
            .material(Material.HEAVY_METAL)
            .properties(MiscMeta.CASING_PROPERTY)
            .register();
    }

    private static <U extends Block, P> IBlockBuilder<U, P> glass(IBlockBuilder<U, P> builder) {
        return builder.material(Material.BARRIER)
            .properties(MiscMeta.CASING_PROPERTY)
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
