package org.shsts.tinactory.unit.autocraft;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.integration.autocraft.LogisticsInventoryView;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LogisticsInventoryViewTest {
    private static boolean bootstrapped;

    @Test
    void extractShouldSupportAmountsLargerThanIntegerMaxValue() {
        bootstrapMinecraft();
        var itemPort = new RecordingItemPort();
        var view = new LogisticsInventoryView(itemPort, IPort.empty());
        var key = CraftKey.item("minecraft:stone", "");
        var amount = (long) Integer.MAX_VALUE + 25L;

        var extracted = view.extract(key, amount, false);

        assertEquals(amount, extracted);
        assertEquals(2, itemPort.extractCalls);
    }

    @Test
    void insertShouldSupportAmountsLargerThanIntegerMaxValue() {
        bootstrapMinecraft();
        var itemPort = new RecordingItemPort();
        var view = new LogisticsInventoryView(itemPort, IPort.empty());
        var key = CraftKey.item("minecraft:stone", "");
        var amount = (long) Integer.MAX_VALUE + 7L;

        var inserted = view.insert(key, amount, false);

        assertEquals(amount, inserted);
        assertEquals(2, itemPort.insertCalls);
    }

    private static void bootstrapMinecraft() {
        if (!bootstrapped) {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
            bootstrapped = true;
        }
    }

    private static final class RecordingItemPort implements IPort<ItemStack> {
        private int extractCalls;
        private int insertCalls;

        @Override
        public PortType type() {
            return PortType.ITEM;
        }

        @Override
        public boolean acceptInput(ItemStack stack) {
            return true;
        }

        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            insertCalls++;
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extract(ItemStack stack, boolean simulate) {
            extractCalls++;
            return stack.copy();
        }

        @Override
        public ItemStack extract(int limit, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getStorageAmount(ItemStack stack) {
            return Integer.MAX_VALUE;
        }

        @Override
        public Collection<ItemStack> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return true;
        }
    }
}
