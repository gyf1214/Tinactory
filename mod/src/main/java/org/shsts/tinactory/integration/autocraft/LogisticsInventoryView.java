package org.shsts.tinactory.integration.autocraft;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.logistics.PortTransmitter;
import org.shsts.tinactory.integration.logistics.FluidPortAdapter;
import org.shsts.tinactory.integration.logistics.ItemPortAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsInventoryView implements IInventoryView {
    private final Map<CraftKey.Type, Channel<?>> channels;

    public LogisticsInventoryView(IPort<ItemStack> itemPort, IPort<FluidStack> fluidPort) {
        channels = new EnumMap<>(CraftKey.Type.class);
        channels.put(CraftKey.Type.ITEM, new Channel<>(
            ItemPortAdapter.INSTANCE,
            new PortTransmitter<>(ItemPortAdapter.INSTANCE),
            itemPort,
            LogisticsInventoryView::toItemStack,
            LogisticsInventoryView::fromItemStack));
        channels.put(CraftKey.Type.FLUID, new Channel<>(
            FluidPortAdapter.INSTANCE,
            new PortTransmitter<>(FluidPortAdapter.INSTANCE),
            fluidPort,
            LogisticsInventoryView::toFluidStack,
            LogisticsInventoryView::fromFluidStack));
    }

    @Override
    public long amountOf(CraftKey key) {
        return channel(key.type()).amountOf(key);
    }

    @Override
    public long extract(CraftKey key, long amount, boolean simulate) {
        return channel(key.type()).extract(key, amount, simulate);
    }

    @Override
    public long insert(CraftKey key, long amount, boolean simulate) {
        return channel(key.type()).insert(key, amount, simulate);
    }

    public List<CraftAmount> snapshotAvailable() {
        var ret = new ArrayList<CraftAmount>();
        for (var channel : channels.values()) {
            channel.snapshotTo(ret);
        }
        return ret;
    }

    private Channel<?> channel(CraftKey.Type type) {
        return Objects.requireNonNull(channels.get(type), "missing channel for craft key type " + type);
    }

    static CraftKey fromItemStack(ItemStack stack) {
        var itemId = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemId == null) {
            throw new IllegalArgumentException("unknown item stack: " + stack);
        }
        var nbt = stack.hasTag() ? stack.getTag().toString() : "";
        return CraftKey.item(itemId.toString(), nbt);
    }

    static CraftKey fromFluidStack(FluidStack stack) {
        var fluidId = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (fluidId == null) {
            throw new IllegalArgumentException("unknown fluid stack: " + stack);
        }
        var nbt = stack.hasTag() ? stack.getTag().toString() : "";
        return CraftKey.fluid(fluidId.toString(), nbt);
    }

    static ItemStack toItemStack(CraftKey key, int amount) {
        var item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(key.id()));
        if (item == null) {
            throw new IllegalArgumentException("unknown item id: " + key.id());
        }
        var stack = new ItemStack(item, amount);
        var tag = parseTag(key.nbt());
        if (!tag.isEmpty()) {
            stack.setTag(tag);
        }
        return stack;
    }

    static FluidStack toFluidStack(CraftKey key, int amount) {
        var fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(key.id()));
        if (fluid == null) {
            throw new IllegalArgumentException("unknown fluid id: " + key.id());
        }
        var stack = new FluidStack(fluid, amount);
        var tag = parseTag(key.nbt());
        if (!tag.isEmpty()) {
            stack.setTag(tag);
        }
        return stack;
    }

    private static CompoundTag parseTag(String nbt) {
        if (nbt.isBlank()) {
            return new CompoundTag();
        }
        try {
            return TagParser.parseTag(nbt);
        } catch (CommandSyntaxException ex) {
            throw new IllegalArgumentException("invalid nbt: " + nbt, ex);
        }
    }

    private record Channel<T>(
        IStackAdapter<T> stackAdapter,
        PortTransmitter<T> transmitter,
        IPort<T> port,
        BiFunction<CraftKey, Integer, T> stackOf,
        Function<T, CraftKey> keyOf
    ) {
        private long amountOf(CraftKey key) {
            return port.getStorageAmount(stackOf.apply(key, 1));
        }

        private long extract(CraftKey key, long amount, boolean simulate) {
            if (amount <= 0L) {
                return 0L;
            }
            var left = amount;
            long extractedTotal = 0L;
            while (left > 0L) {
                var chunk = (int) Math.min(left, Integer.MAX_VALUE);
                var expected = stackOf.apply(key, chunk);
                var moved = movedByExtract(expected, simulate);
                if (moved <= 0L) {
                    break;
                }
                extractedTotal += moved;
                left -= moved;
            }
            return extractedTotal;
        }

        private long insert(CraftKey key, long amount, boolean simulate) {
            if (amount <= 0L) {
                return 0L;
            }
            var left = amount;
            long insertedTotal = 0L;
            while (left > 0L) {
                var chunk = (int) Math.min(left, Integer.MAX_VALUE);
                var expected = stackOf.apply(key, chunk);
                var moved = movedByInsert(expected, chunk, simulate);
                if (moved <= 0L) {
                    break;
                }
                insertedTotal += moved;
                left -= moved;
            }
            return insertedTotal;
        }

        private void snapshotTo(List<CraftAmount> out) {
            for (var stack : port.getAllStorages()) {
                if (!stackAdapter.isEmpty(stack)) {
                    out.add(new CraftAmount(keyOf.apply(stack), stackAdapter.amount(stack)));
                }
            }
        }

        private int movedByExtract(T expected, boolean simulate) {
            if (simulate) {
                var moved = transmitter.probe(
                    port,
                    new SinkPort<>(stackAdapter),
                    expected,
                    stackAdapter.amount(expected));
                return stackAdapter.amount(moved);
            }
            return stackAdapter.amount(port.extract(expected, false));
        }

        private int movedByInsert(T expected, int expectedAmount, boolean simulate) {
            if (simulate) {
                var moved = transmitter.probe(
                    new SourcePort<>(stackAdapter, expected),
                    port,
                    expected,
                    expectedAmount);
                return stackAdapter.amount(moved);
            }
            var remaining = port.insert(expected, false);
            return expectedAmount - stackAdapter.amount(remaining);
        }
    }

    private record SinkPort<T>(IStackAdapter<T> stackAdapter) implements IPort<T> {
        @Override
        public PortType type() {
            return PortType.NONE;
        }

        @Override
        public boolean acceptInput(T stack) {
            return true;
        }

        @Override
        public T insert(T stack, boolean simulate) {
            return stackAdapter.empty();
        }

        @Override
        public T extract(T stack, boolean simulate) {
            return stackAdapter.empty();
        }

        @Override
        public T extract(int limit, boolean simulate) {
            return stackAdapter.empty();
        }

        @Override
        public int getStorageAmount(T stack) {
            return 0;
        }

        @Override
        public Collection<T> getAllStorages() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return false;
        }
    }

    private record SourcePort<T>(IStackAdapter<T> stackAdapter, T stack) implements IPort<T> {
        @Override
        public PortType type() {
            return PortType.NONE;
        }

        @Override
        public boolean acceptInput(T input) {
            return false;
        }

        @Override
        public T insert(T input, boolean simulate) {
            return input;
        }

        @Override
        public T extract(T requested, boolean simulate) {
            if (stackAdapter.isEmpty(stack) ||
                stackAdapter.isEmpty(requested) ||
                !stackAdapter.canStack(stack, requested)) {
                return stackAdapter.empty();
            }
            var moved = Math.min(stackAdapter.amount(stack), stackAdapter.amount(requested));
            return stackAdapter.withAmount(stack, moved);
        }

        @Override
        public T extract(int limit, boolean simulate) {
            if (stackAdapter.isEmpty(stack) || limit <= 0) {
                return stackAdapter.empty();
            }
            var moved = Math.min(stackAdapter.amount(stack), limit);
            return stackAdapter.withAmount(stack, moved);
        }

        @Override
        public int getStorageAmount(T requested) {
            if (stackAdapter.isEmpty(stack) ||
                stackAdapter.isEmpty(requested) ||
                !stackAdapter.canStack(stack, requested)) {
                return 0;
            }
            return stackAdapter.amount(stack);
        }

        @Override
        public Collection<T> getAllStorages() {
            return stackAdapter.isEmpty(stack) ? List.of() : List.of(stackAdapter.copy(stack));
        }

        @Override
        public boolean acceptOutput() {
            return !stackAdapter.isEmpty(stack);
        }
    }
}
