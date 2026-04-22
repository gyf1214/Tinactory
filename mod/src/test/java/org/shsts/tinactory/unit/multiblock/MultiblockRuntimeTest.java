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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiblockRuntimeTest {
    private static final BlockPos ORIGIN = new BlockPos(10, 20, 30);
    private static final BlockPos OTHER = ORIGIN.east();

    @Test
    void tickDoesNothingWhileRetryCountdownHasNotExpired() {
        var manager = manager();
        var host = new FakeHost("host").structure(ORIGIN);
        var runtime = new MultiblockRuntime(host, 2);
        manager.register(new MultiblockRuntime(new FakeHost("other"), 2), List.of(ORIGIN));

        runtime.tick(manager);
        runtime.tick(manager);
        runtime.tick(manager);

        assertEquals(1, host.checks);
        assertEquals(0, host.registers);
    }

    @Test
    void successfulRegistrationCallsRegisterCallbackOnce() {
        var manager = manager();
        var host = new FakeHost("host").structure(ORIGIN, OTHER);
        var runtime = new MultiblockRuntime(host, 2);

        runtime.tick(manager);
        runtime.tick(manager);

        assertEquals(1, host.checks);
        assertEquals(1, host.registers);
    }

    @Test
    void failedRegistrationRetriesAfterConfiguredCycle() {
        var manager = manager();
        var blocker = new MultiblockRuntime(new FakeHost("blocker"), 2);
        var host = new FakeHost("host").structure(ORIGIN);
        var runtime = new MultiblockRuntime(host, 2);
        manager.register(blocker, List.of(ORIGIN));

        runtime.tick(manager);
        blocker.invalidate();
        runtime.tick(manager);
        runtime.tick(manager);
        runtime.tick(manager);

        assertEquals(2, host.checks);
        assertEquals(1, host.registers);
    }

    @Test
    void dirtyStructureRecheckInvalidatesWhenStructureNoLongerMatches() {
        var manager = manager();
        var host = new FakeHost("host").structure(ORIGIN);
        var runtime = new MultiblockRuntime(host, 2);

        runtime.tick(manager);
        host.empty();
        runtime.markStructureDirty();
        runtime.tick(manager);

        assertEquals(2, host.checks);
        assertEquals(1, host.registers);
        assertEquals(1, host.invalidates);
    }

    @Test
    void dirtyStructureRecheckKeepsRegistrationWhenStructureStillMatches() {
        var manager = manager();
        var host = new FakeHost("host").structure(ORIGIN);
        var runtime = new MultiblockRuntime(host, 2);

        runtime.tick(manager);
        runtime.markStructureDirty();
        runtime.tick(manager);

        assertEquals(2, host.checks);
        assertEquals(1, host.registers);
        assertEquals(0, host.invalidates);
    }

    @Test
    void invalidateClearsAllRegisteredPositionsBeforeCallback() {
        var manager = manager();
        var host = new FakeHost("host").structure(ORIGIN, OTHER);
        var runtime = new MultiblockRuntime(host, 2);

        runtime.tick(manager);
        manager.registerCleanroom(runtime, ORIGIN, 2, 2, 2);
        runtime.invalidate();
        var secondHost = new FakeHost("second").structure(ORIGIN, OTHER);
        var secondRuntime = new MultiblockRuntime(secondHost, 2);
        secondRuntime.tick(manager);

        assertEquals(1, host.invalidates);
        assertEquals(1, secondHost.registers);
        assertTrue(manager.getCleanroom(ORIGIN).isEmpty());
        assertTrue(manager.getCleanroom(ORIGIN.below()).isEmpty());
    }

    private static MultiblockManager manager() {
        return new MultiblockManager();
    }

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
