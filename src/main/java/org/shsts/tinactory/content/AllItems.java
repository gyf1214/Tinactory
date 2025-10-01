package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.shsts.tinactory.content.electric.Circuits;
import org.shsts.tinactory.content.logistics.MEStorageCell;
import org.shsts.tinactory.content.material.RubberLogBlock;
import org.shsts.tinactory.content.material.RubberTreeGrower;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllItems {
    public static final Map<String, Map<Voltage, ? extends Supplier<? extends ItemLike>>> COMPONENTS;
    public static final List<IEntry<Item>> STORAGE_COMPONENT;
    public static final List<IEntry<MEStorageCell>> ITEM_STORAGE_CELL;
    public static final List<IEntry<MEStorageCell>> FLUID_STORAGE_CELL;

    public static final IEntry<RubberLogBlock> RUBBER_LOG;
    public static final IEntry<LeavesBlock> RUBBER_LEAVES;
    public static final IEntry<SaplingBlock> RUBBER_SAPLING;

    static {
        COMPONENTS = new HashMap<>();

        Circuits.buildBoards();

        RUBBER_LOG = REGISTRATE.block("rubber_tree/log", RubberLogBlock::new)
            .material(Material.WOOD)
            .properties(p -> p.strength(2f).sound(SoundType.WOOD))
            .register();

        RUBBER_LEAVES = REGISTRATE.block("rubber_tree/leaves", LeavesBlock::new)
            .material(Material.LEAVES)
            .properties(p -> p.strength(0.2f).randomTicks()
                .sound(SoundType.GRASS).noOcclusion()
                .isValidSpawn(AllItems::never)
                .isSuffocating(AllItems::never)
                .isViewBlocking(AllItems::never))
            .renderType(() -> RenderType::cutout)
            .tint(0xFF55FF55)
            .register();

        RUBBER_SAPLING = REGISTRATE.block("rubber_tree/sapling",
                prop -> new SaplingBlock(new RubberTreeGrower(), prop))
            .material(Material.PLANT)
            .properties(p -> p.noCollission().randomTicks()
                .instabreak().sound(SoundType.GRASS))
            .renderType(() -> RenderType::cutout)
            .register();

        ITEM_STORAGE_CELL = new ArrayList<>();
        FLUID_STORAGE_CELL = new ArrayList<>();
        STORAGE_COMPONENT = new ArrayList<>();
        for (var i = 0; i < 4; i++) {
            var k = 1 << (2 * i);
            var bytes = 1048576 * k;
            STORAGE_COMPONENT.add(REGISTRATE.item(
                "component/storage_component/" + k + "m").register());
            ITEM_STORAGE_CELL.add(REGISTRATE.item(
                "logistics/item_storage_cell/" + k + "m",
                MEStorageCell.itemCell(bytes)).register());
            FLUID_STORAGE_CELL.add(REGISTRATE.item(
                "logistics/fluid_storage_cell/" + k + "m",
                MEStorageCell.fluidCell(bytes)).register());
        }
    }

    public static void init() {}

    public static Map<Voltage, ? extends Supplier<? extends ItemLike>> getComponent(String name) {
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
