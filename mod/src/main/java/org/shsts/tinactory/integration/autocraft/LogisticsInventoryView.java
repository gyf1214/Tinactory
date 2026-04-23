package org.shsts.tinactory.integration.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.IInventoryView;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.integration.logistics.StackHelper;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsInventoryView implements IInventoryView {
    private final Map<PortType, CraftPortChannel<?>> channels;

    public LogisticsInventoryView(IPort<ItemStack> itemPort, IPort<FluidStack> fluidPort) {
        channels = new EnumMap<>(PortType.class);
        channels.put(PortType.ITEM, new CraftPortChannel<>(StackHelper.ITEM_ADAPTER, itemPort));
        channels.put(PortType.FLUID, new CraftPortChannel<>(StackHelper.FLUID_ADAPTER, fluidPort));
    }

    @Override
    public long amountOf(IStackKey key) {
        return channel(key.type()).amountOf(key);
    }

    @Override
    public long extract(IStackKey key, long amount, boolean simulate) {
        return channel(key.type()).extract(key, amount, simulate);
    }

    @Override
    public long insert(IStackKey key, long amount, boolean simulate) {
        return channel(key.type()).insert(key, amount, simulate);
    }

    public List<CraftAmount> snapshotAvailable() {
        var ret = new ArrayList<CraftAmount>();
        for (var channel : channels.values()) {
            ret.addAll(channel.snapshot());
        }
        return ret;
    }

    private CraftPortChannel<?> channel(PortType type) {
        return Objects.requireNonNull(channels.get(type), "missing channel for ingredient key type " + type);
    }
}
