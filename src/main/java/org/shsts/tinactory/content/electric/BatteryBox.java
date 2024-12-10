package org.shsts.tinactory.content.electric;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import java.util.function.Function;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllNetworks.ELECTRIC_COMPONENT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBox extends CapabilityProvider implements IEventSubscriber,
    IProcessor, IElectricMachine, ILayoutProvider, INBTSerializable<CompoundTag> {
    private final Layout layout;
    private final BlockEntity blockEntity;
    private final Voltage voltage;
    private Machine machine;
    private final WrapperItemHandler handler;
    private final LazyOptional<IItemHandler> itemHandlerCap;

    public BatteryBox(BlockEntity blockEntity, Layout layout) {
        this.layout = layout;
        this.blockEntity = blockEntity;
        this.voltage = RecipeProcessor.getBlockVoltage(blockEntity);
        var size = voltage.rank * voltage.rank;
        this.handler = new WrapperItemHandler(size);
        for (var i = 0; i < size; i++) {
            handler.setFilter(i, this::allowItem);
        }
        this.itemHandlerCap = LazyOptional.of(() -> handler);
    }

    private boolean allowItem(ItemStack stack) {
        return stack.getItem() instanceof BatteryItem batteryItem &&
            batteryItem.voltage == voltage;
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        var factor = machine.getNetwork().orElseThrow()
            .getComponent(ELECTRIC_COMPONENT)
            .getBufferFactor();
        var sign = MathUtil.compare(factor);
        if (sign == 0) {
            return;
        }
        for (var i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BatteryItem battery)) {
                continue;
            }
            var cap = Math.min(voltage.value, sign > 0 ?
                battery.capacity - battery.getPowerLevel(stack) :
                battery.getPowerLevel(stack));
            battery.charge(stack, (long) Math.floor(cap * factor));
        }
    }

    @Override
    public double getProgress() {
        return 0d;
    }

    @Override
    public long getVoltage() {
        return voltage.value;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.BUFFER;
    }

    @Override
    public double getPowerGen() {
        var ret = 0d;
        for (var i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BatteryItem battery) {
                ret += Math.min(voltage.value, battery.getPowerLevel(stack));
            }
        }
        return ret;
    }

    @Override
    public double getPowerCons() {
        var ret = 0d;
        for (var i = 0; i < handler.getSlots(); i++) {
            var stack = handler.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BatteryItem battery) {
                ret += Math.min(voltage.value, battery.capacity - battery.getPowerLevel(stack));
            }
        }
        return ret;
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD, $ -> machine = MACHINE.get(blockEntity));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get() || cap == PROCESSOR.get() ||
            cap == LAYOUT_PROVIDER.get()) {
            return myself();
        } else if (cap == MENU_ITEM_HANDLER.get()) {
            return itemHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return StackHelper.serializeItemHandler(handler);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        StackHelper.deserializeItemHandler(handler, tag);
    }

    public static <P> Function<P, CapabilityProviderBuilder<SmartBlockEntity,
        P>> builder(Layout layout) {
        return CapabilityProviderBuilder.fromFactory("battery_box", be -> new BatteryBox(be, layout));
    }
}
