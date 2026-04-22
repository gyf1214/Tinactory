package org.shsts.tinactory.unit.fixture;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.multiblock.MultiblockCheckCtx;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class TestMultiblockCheckCtx extends MultiblockCheckCtx<TestBlock> {
    private final Map<BlockPos, TestBlock> blocks = new HashMap<>();
    private final Map<BlockPos, IMachine> machines = new HashMap<>();
    private Optional<Direction> facing = Optional.empty();

    public TestMultiblockCheckCtx(BlockPos center) {
        super(center);
    }

    public TestMultiblockCheckCtx block(BlockPos pos, TestBlock block) {
        blocks.put(pos, block);
        return this;
    }

    public TestMultiblockCheckCtx machine(BlockPos pos, IMachine machine) {
        machines.put(pos, machine);
        return this;
    }

    public TestMultiblockCheckCtx facing(Direction value) {
        facing = Optional.of(value);
        return this;
    }

    @Override
    public Optional<TestBlock> getBlock(BlockPos pos) {
        return Optional.ofNullable(blocks.get(pos));
    }

    @Override
    public Optional<IMachine> getMachine(BlockPos pos) {
        return Optional.ofNullable(machines.get(pos));
    }

    @Override
    public Optional<Direction> getFacing() {
        return facing;
    }
}
