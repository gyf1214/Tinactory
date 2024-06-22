package org.shsts.tinactory.content.electric;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBox extends CapabilityProvider implements IEventSubscriber,
        IProcessor, IElectricMachine, INBTSerializable<CompoundTag> {
    private final BlockEntity blockEntity;
    private final Voltage voltage;
    private Machine machine;
    private final WrapperItemHandler handler;
    private final LazyOptional<IItemHandler> itemHandlerCap;

    public BatteryBox(BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        this.voltage = RecipeProcessor.getBlockVoltage(blockEntity);
        this.handler = new WrapperItemHandler(1);
        handler.setFilter(0, this::allowItem);
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
        var electric = machine.getNetwork().orElseThrow()
                .getComponent(AllNetworks.ELECTRIC_COMPONENT);
        var delta = (long) Math.floor(electric.getBufferFactor() * voltage.value);
        var stack = handler.getStackInSlot(0);
        if (stack.getItem() instanceof BatteryItem battery) {
            battery.charge(stack, delta);
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
        var stack = handler.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof BatteryItem battery)) {
            return 0;
        }
        return Math.min(voltage.value, battery.getPowerLevel(stack));
    }

    @Override
    public double getPowerCons() {
        var stack = handler.getStackInSlot(0);
        if (stack.isEmpty() || !(stack.getItem() instanceof BatteryItem battery)) {
            return 0;
        }
        return Math.min(voltage.value, battery.capacity - battery.getPowerLevel(stack));
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD,
                $ -> machine = AllCapabilities.MACHINE.get(blockEntity));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.ELECTRIC_MACHINE.get() ||
                cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        } else if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return ItemHelper.serializeItemHandler(handler);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(handler, tag);
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "battery_box", BatteryBox::new);
    }
}
