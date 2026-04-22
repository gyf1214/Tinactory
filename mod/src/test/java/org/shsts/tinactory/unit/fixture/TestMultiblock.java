package org.shsts.tinactory.unit.fixture;

import net.minecraft.core.BlockPos;
import org.shsts.tinactory.core.multiblock.IMultiblock;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public final class TestMultiblock implements IMultiblock {
    private final String id;
    private Optional<Collection<BlockPos>> structure = Optional.empty();
    private int checks;
    private int registers;
    private int invalidates;

    public TestMultiblock(String id) {
        this.id = id;
    }

    public TestMultiblock structure(BlockPos... blocks) {
        structure = Optional.of(List.of(blocks));
        return this;
    }

    public void empty() {
        structure = Optional.empty();
    }

    public int checks() {
        return checks;
    }

    public int registers() {
        return registers;
    }

    public int invalidates() {
        return invalidates;
    }

    @Override
    public Optional<Collection<BlockPos>> checkStructure() {
        checks++;
        return structure;
    }

    @Override
    public void onRegisterStructure() {
        registers++;
    }

    @Override
    public void onInvalidateStructure() {
        invalidates++;
    }

    @Override
    public String toString() {
        return id;
    }
}
