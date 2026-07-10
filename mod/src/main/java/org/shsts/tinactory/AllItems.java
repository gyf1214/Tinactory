package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.logistics.MEStorageCellSet;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.content.material.RubberTreeGrower;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final Map<String, Map<Voltage, ? extends IEntry<? extends ItemLike>>> COMPONENTS;

    public static final List<MEStorageCellSet> STORAGE_CELLS;

    public static final IEntry<RubberLogBlock> RUBBER_LOG;
    public static final IEntry<LeavesBlock> RUBBER_LEAVES;
    public static final IEntry<SaplingBlock> RUBBER_SAPLING;

    static {
        COMPONENTS = new HashMap<>();
        STORAGE_CELLS = new ArrayList<>();

        Circuits.buildBoards();

        RUBBER_LOG = REGISTRATE.block("rubber_tree/log", RubberLogBlock::new)
            .properties(p -> p.mapColor(Blocks.OAK_LOG.defaultMapColor())
                .strength(2f).sound(SoundType.WOOD))
            .creativeTab(CreativeModeTabs.BUILDING_BLOCKS)
            .register();

        RUBBER_LEAVES = REGISTRATE.block("rubber_tree/leaves", LeavesBlock::new)
            .properties(p -> p.mapColor(Blocks.OAK_LEAVES.defaultMapColor())
                .strength(0.2f).randomTicks()
                .sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn(AllItems::never)
                .isSuffocating(AllItems::never)
                .isViewBlocking(AllItems::never))
            .creativeTab(CreativeModeTabs.NATURAL_BLOCKS)
            .tint(0xFF55FF55)
            .register();

        RUBBER_SAPLING = REGISTRATE.block("rubber_tree/sapling",
                prop -> new SaplingBlock(RubberTreeGrower.INSTANCE, prop))
            .properties(p -> p.mapColor(Blocks.OAK_SAPLING.defaultMapColor())
                .noCollission().randomTicks()
                .instabreak().sound(SoundType.GRASS))
            .creativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS)
            .register();
    }

    public static void init() {}

    public static Map<Voltage, ? extends IEntry<? extends ItemLike>> getComponent(String name) {
        return COMPONENTS.get(name);
    }

    @SuppressWarnings("unchecked")
    public static <U extends ItemLike> Map<Voltage, IEntry<U>> componentEntry(String name) {
        return (Map<Voltage, IEntry<U>>) getComponent(name);
    }

    public static <A> boolean never(BlockState state, BlockGetter world, BlockPos pos, A val) {
        return false;
    }

    public static boolean never(BlockState state, BlockGetter world, BlockPos pos) {
        return false;
    }
}
