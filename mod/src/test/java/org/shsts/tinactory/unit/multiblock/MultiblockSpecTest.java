package org.shsts.tinactory.unit.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.core.multiblock.MultiblockSpec;
import org.shsts.tinactory.unit.fixture.TestBlock;
import org.shsts.tinactory.unit.fixture.TestContainer;
import org.shsts.tinactory.unit.fixture.TestMachine;
import org.shsts.tinactory.unit.fixture.TestMultiblockCheckCtx;

import java.util.List;
import java.util.function.BiConsumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MultiblockSpecTest {
    private static final BlockPos CENTER = new BlockPos(10, 20, 30);

    private static MultiblockSpec.Builder<TestBlock, Void> builder() {
        return MultiblockSpec.<TestBlock, Void>builder(null)
            .checkBlock('A', block -> block == TestBlock.BASE)
            .checkBlock('B', block -> block == TestBlock.CASING)
            .checkBlock('C', block -> block == TestBlock.COIL)
            .checkBlock('G', block -> block == TestBlock.GLASS);
    }

    private static TestMultiblockCheckCtx contextWithCenter() {
        return new TestMultiblockCheckCtx(CENTER)
            .block(CENTER, TestBlock.BASE);
    }

    private static void assertInvalid(IllegalArgumentException exception, String message) {
        assertEquals(message, exception.getMessage());
    }

    @Test
    void rejectsMismatchedLayerSizes() {
        var spec = builder();
        spec.layer().row("A$").row("BB").build();
        var layer = spec.layer().row("AAA").row("BBB");

        assertInvalid(assertThrows(IllegalArgumentException.class, layer::build), "layer size not same");
    }

    @Test
    void rejectsMismatchedNonEmptyRowWidths() {
        var layer = builder().layer().row("A$").row("B");

        assertInvalid(assertThrows(IllegalArgumentException.class, layer::build), "layer rows are not same size");
    }

    @Test
    void rejectsMissingCenter() {
        var spec = builder();
        spec.layer().row("AB").row("BA").build();

        assertInvalid(assertThrows(IllegalArgumentException.class, spec::build), "contains no center");
    }

    @Test
    void rejectsDuplicateCenters() {
        var spec = builder();
        spec.layer().row("$A").row("B$").build();

        assertInvalid(assertThrows(IllegalArgumentException.class, spec::build), "contains more than 1 center");
    }

    @Test
    void rejectsUndefinedSymbols() {
        var spec = builder();
        spec.layer().row("$Z").row("AA").build();

        assertInvalid(assertThrows(IllegalArgumentException.class, spec::build), "invalid character in spec: 'Z'");
    }

    @Test
    void normalizesEmptyRows() {
        var spec = builder();
        spec.layer().row("$A").row("").build();

        assertDoesNotThrow(spec::build);
    }

    @Test
    void usesDefaultSouthAndEastOrientationWhenFacingIsMissing() {
        var spec = builder();
        spec.layer().row("$A").row("BC").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .block(CENTER.east(), TestBlock.BASE)
            .block(CENTER.south(), TestBlock.CASING)
            .block(CENTER.south().east(), TestBlock.COIL);

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertEquals(CENTER, ctx.getProperty("base"));
        assertEquals(Direction.EAST, ctx.getProperty("dirW"));
        assertEquals(Direction.SOUTH, ctx.getProperty("dirD"));
        assertEquals(List.of(CENTER.east(), CENTER.south(), CENTER.south().east()), ctx.structure());
    }

    @Test
    void usesExplicitFacingFromContext() {
        var spec = builder();
        spec.layer().row("$A").row("BC").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .facing(Direction.EAST)
            .block(CENTER.north(), TestBlock.BASE)
            .block(CENTER.east(), TestBlock.CASING)
            .block(CENTER.east().north(), TestBlock.COIL);

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertEquals(Direction.NORTH, ctx.getProperty("dirW"));
        assertEquals(Direction.EAST, ctx.getProperty("dirD"));
        assertEquals(List.of(CENTER.north(), CENTER.east(), CENTER.east().north()), ctx.structure());
    }

    @Test
    void traversesLayersAboveAndBelowCenterLayer() {
        var spec = builder();
        spec.layer().row("A").build();
        spec.layer().row("$").build();
        spec.layer().row("B").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .block(CENTER.below(), TestBlock.BASE)
            .block(CENTER.above(), TestBlock.CASING);

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertEquals(3, ctx.getProperty("height"));
        assertEquals(List.of(CENTER.above(), CENTER.below()), ctx.structure());
    }

    @Test
    void appliesVariableHeightMinimumAndMaximum() {
        var spec = builder();
        spec.layer().height(1, 3).row("A").build();
        spec.layer().row("$").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .block(CENTER.below(), TestBlock.BASE)
            .block(CENTER.below(2), TestBlock.BASE);

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertEquals(3, ctx.getProperty("height"));
        assertEquals(List.of(CENTER.below(), CENTER.below(2)), ctx.structure());
    }

    @Test
    void failsWhenVariableHeightMinimumIsMissing() {
        var spec = builder();
        spec.layer().height(2, 3).row("A").build();
        spec.layer().row("$").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .block(CENTER.below(), TestBlock.BASE);

        checker.accept(ctx);

        assertTrue(ctx.isFailed());
    }

    @Test
    void preservesFailedOptionalAttemptPropertyMutations() {
        BiConsumer<IMultiblockCheckCtx<TestBlock>, BlockPos> mutateThenFail = (ctx, pos) -> {
            ctx.setProperty("attempted", pos);
            ctx.setFailed();
        };
        var spec = MultiblockSpec.<TestBlock, Void>builder(null)
            .check('A', (ctx, pos) -> {
                if (ctx.getBlock(pos).filter(block -> block == TestBlock.BASE).isEmpty()) {
                    ctx.setFailed();
                }
            })
            .check('M', mutateThenFail);
        spec.layer().row("$").build();
        spec.layer().height(0, 1).row("M").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter();

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertEquals(CENTER.above(), ctx.getProperty("attempted"));
    }

    @Test
    void excludesIgnoredAndCenterPositionsFromCollectedBlocks() {
        var spec = builder();
        spec.layer().row("$ ").row(" A").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .block(CENTER.south().east(), TestBlock.BASE);

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertEquals(List.of(CENTER.south().east()), ctx.structure());
    }

    @Test
    void acceptsOneMultiblockInterfaceMachine() {
        var interfaceMachine = new TestMachine(new TestContainer()).multiblock(true);
        var spec = builder().interfaceSlot('I');
        spec.layer().row("$I").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .machine(CENTER.east(), interfaceMachine);

        checker.accept(ctx);

        assertFalse(ctx.isFailed());
        assertSame(interfaceMachine, ctx.getProperty("interface"));
    }

    @Test
    void rejectsDuplicateInterfaceMachines() {
        var interfaceMachine = new TestMachine(new TestContainer()).multiblock(true);
        var spec = builder().interfaceSlot('I');
        spec.layer().row("$I").row("II").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .machine(CENTER.east(), interfaceMachine)
            .machine(CENTER.south(), interfaceMachine)
            .machine(CENTER.south().east(), interfaceMachine);

        checker.accept(ctx);

        assertTrue(ctx.isFailed());
    }

    @Test
    void rejectsNonMultiblockInterfaceMachine() {
        var spec = builder().interfaceSlot('I');
        spec.layer().row("$I").build();
        var checker = spec.buildObject();
        var ctx = contextWithCenter()
            .machine(CENTER.east(), new TestMachine(new TestContainer()));

        checker.accept(ctx);

        assertTrue(ctx.isFailed());
    }
}
