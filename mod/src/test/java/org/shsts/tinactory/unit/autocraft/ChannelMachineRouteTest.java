package org.shsts.tinactory.unit.autocraft;

import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.ChannelMachineRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChannelMachineRouteTest {
    @Test
    void inputRouteShouldTransferViaInsert() {
        var key = CraftKey.item("tinactory:iron", "");
        var port = new RecordingPort();
        var route = new ChannelMachineRoute<>(
            key,
            IMachineRoute.Direction.INPUT,
            Stack.ADAPTER,
            port,
            (routeKey, amount) -> new Stack(routeKey.id(), amount),
            stack -> CraftKey.item(stack.id(), ""));

        var moved = route.transfer(17, false);

        assertEquals(17L, moved);
        assertEquals(1, port.insertCalls);
        assertEquals(0, port.extractCalls);
    }

    @Test
    void outputRouteShouldTransferViaExtract() {
        var key = CraftKey.item("tinactory:iron", "");
        var port = new RecordingPort();
        var route = new ChannelMachineRoute<>(
            key,
            IMachineRoute.Direction.OUTPUT,
            Stack.ADAPTER,
            port,
            (routeKey, amount) -> new Stack(routeKey.id(), amount),
            stack -> CraftKey.item(stack.id(), ""));

        var moved = route.transfer(17, false);

        assertEquals(17L, moved);
        assertEquals(0, port.insertCalls);
        assertEquals(1, port.extractCalls);
    }

    private record Stack(String id, int amount) {
        private static final IStackAdapter<Stack> ADAPTER = new IStackAdapter<>() {
            @Override
            public Stack empty() {
                return new Stack("", 0);
            }

            @Override
            public boolean isEmpty(Stack stack) {
                return stack.amount() <= 0;
            }

            @Override
            public Stack copy(Stack stack) {
                return new Stack(stack.id(), stack.amount());
            }

            @Override
            public int amount(Stack stack) {
                return stack.amount();
            }

            @Override
            public Stack withAmount(Stack stack, int amount) {
                return new Stack(stack.id(), amount);
            }

            @Override
            public boolean canStack(Stack left, Stack right) {
                return Objects.equals(left.id(), right.id());
            }

            @Override
            public IIngredientKey keyOf(Stack stack) {
                return new Key(stack.id());
            }
        };
    }

    private record Key(String id) implements IIngredientKey {}

    private static final class RecordingPort implements IPort<Stack> {
        private int insertCalls;
        private int extractCalls;

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(Stack stack) {
            return true;
        }

        @Override
        public Stack insert(Stack stack, boolean simulate) {
            insertCalls++;
            return new Stack(stack.id(), 0);
        }

        @Override
        public Stack extract(Stack stack, boolean simulate) {
            extractCalls++;
            return stack;
        }

        @Override
        public Stack extract(int limit, boolean simulate) {
            return new Stack("", 0);
        }

        @Override
        public int getStorageAmount(Stack stack) {
            return 0;
        }

        @Override
        public Collection<Stack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return true;
        }
    }
}
