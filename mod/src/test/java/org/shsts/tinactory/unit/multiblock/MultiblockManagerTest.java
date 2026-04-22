package org.shsts.tinactory.unit.multiblock;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.core.multiblock.IMultiblock;
import org.shsts.tinactory.core.multiblock.MultiblockManager;
import org.shsts.tinactory.core.multiblock.MultiblockRuntime;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiblockManagerTest {
    private static final BlockPos ORIGIN = new BlockPos(10, 20, 30);
    private static final BlockPos OTHER = ORIGIN.east();
    private static final BlockPos CLEANROOM_POS = ORIGIN.below();

    @Test
    void registersMultipleOccupiedPositionsAndRejectsConflict() {
        var manager = new MultiblockManager();
        var first = runtime("first", ORIGIN, OTHER);
        var second = runtime("second", OTHER);

        assertTrue(manager.register(first.runtime, List.of(ORIGIN, OTHER)));
        assertFalse(manager.register(second.runtime, List.of(OTHER)));

        manager.invalidate(OTHER);
        first.host.empty();
        first.runtime.tick(manager);

        assertEquals(1, first.host.invalidates);
        assertEquals(0, second.host.registers);
    }

    @Test
    void invalidatingOccupiedPositionOnlyMarksOwningRuntimeDirty() {
        var manager = new MultiblockManager();
        var first = runtime("first", ORIGIN);
        var second = runtime("second", OTHER);
        manager.register(first.runtime, List.of(ORIGIN));
        manager.register(second.runtime, List.of(OTHER));

        manager.invalidate(ORIGIN);
        first.host.empty();
        second.host.empty();
        first.runtime.tick(manager);
        second.runtime.tick(manager);

        assertEquals(1, first.host.checks);
        assertEquals(1, first.host.invalidates);
        assertEquals(0, second.host.checks);
        assertEquals(0, second.host.invalidates);
    }

    @Test
    void runtimeInvalidationClearsAllOccupiedPositionsSharingToken() {
        var manager = new MultiblockManager();
        var first = runtime("first", ORIGIN, OTHER);
        var second = runtime("second", ORIGIN, OTHER);
        manager.register(first.runtime, List.of(ORIGIN, OTHER));

        first.runtime.invalidate();

        assertTrue(manager.register(second.runtime, List.of(ORIGIN, OTHER)));
    }

    @Test
    void cleanroomRegistrationReturnsHostAndOverlapsReplaceThroughWeakMap() {
        var manager = new MultiblockManager();
        var first = runtime("first", ORIGIN);
        var second = runtime("second", OTHER);

        manager.registerCleanroom(first.runtime, ORIGIN, 1, 1, 2);
        assertSame(first.host, manager.getCleanroom(CLEANROOM_POS).orElseThrow());

        manager.registerCleanroom(second.runtime, ORIGIN, 1, 1, 2);
        assertSame(second.host, manager.getCleanroom(CLEANROOM_POS).orElseThrow());
    }

    @Test
    void destroyClearsOccupiedAndCleanroomMaps() {
        var manager = new MultiblockManager();
        var first = runtime("first", ORIGIN, OTHER);
        var second = runtime("second", ORIGIN, OTHER);
        manager.register(first.runtime, List.of(ORIGIN, OTHER));
        manager.registerCleanroom(first.runtime, ORIGIN, 1, 1, 2);

        manager.destroy();

        assertTrue(manager.getCleanroom(CLEANROOM_POS).isEmpty());
        assertTrue(manager.register(second.runtime, List.of(ORIGIN, OTHER)));
    }

    private static RuntimeFixture runtime(String id, BlockPos... blocks) {
        var host = new FakeHost(id).structure(blocks);
        return new RuntimeFixture(host, new MultiblockRuntime(host, 2));
    }

    private record RuntimeFixture(FakeHost host, MultiblockRuntime runtime) {}

    private static final class FakeHost implements IMultiblock {
        private final String id;
        private Optional<Collection<BlockPos>> structure = Optional.empty();
        private int checks;
        private int registers;
        private int invalidates;

        private FakeHost(String id) {
            this.id = id;
        }

        private FakeHost structure(BlockPos... blocks) {
            structure = Optional.of(List.of(blocks));
            return this;
        }

        private void empty() {
            structure = Optional.empty();
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
}
