package org.shsts.tinactory.content.electric;

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
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.tool.BatteryItem;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.integration.logistics.IMenuItemHandler;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.integration.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBox extends CapabilityProvider implements IEventSubscriber,
    IBatteryBox, IElectricMachine, ILayoutProvider, INBTSerializable<CompoundTag> {
    public static final String DISCHARGE_KEY = "discharge";
    public static final boolean DISCHARGE_DEFAULT = false;
    private static final String ID = "battery_box";

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final Voltage voltage;
    private IMachine machine;
    private final WrapperItemHandler items;
    private final LazyOptional<IMenuItemHandler> menuItemHandlerCap;

    public BatteryBox(BlockEntity blockEntity, Layout layout) {
        this.blockEntity = blockEntity;
        this.layout = layout;
        this.voltage = getBlockVoltage(blockEntity);
        var size = layout.slots.size();
        this.items = new WrapperItemHandler(size);
        for (var i = 0; i < size; i++) {
            items.setFilter(i, this::allowItem);
        }
        this.menuItemHandlerCap = IMenuItemHandler.cap(items);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout) {
        return $ -> $.capability(ID, be -> new BatteryBox(be, layout));
    }

    private boolean allowItem(ItemStack stack) {
        return stack.getItem() instanceof BatteryItem batteryItem &&
            batteryItem.voltage == voltage;
    }

    private boolean isDischarge() {
        return machine.config().getBoolean(DISCHARGE_KEY, DISCHARGE_DEFAULT);
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        double factor;
        if (isDischarge()) {
            factor = -1;
        } else {
            factor = machine.network().orElseThrow()
                .getComponent(ELECTRIC_COMPONENT.get())
                .getBufferFactor();
        }
        var sign = MathUtil.compare(factor);
        if (sign == 0) {
            return;
        }
        for (var i = 0; i < items.getSlots(); i++) {
            var stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BatteryItem battery)) {
                continue;
            }
            var cap = Math.min(voltage.value, sign > 0 ?
                battery.capacity - battery.getPower(stack) :
                battery.getPower(stack));
            battery.charge(stack, (long) Math.floor(cap * factor));
        }
        blockEntity.setChanged();
    }

    @Override
    public long powerLevel() {
        var ret = 0L;
        for (var i = 0; i < items.getSlots(); i++) {
            var stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BatteryItem battery)) {
                continue;
            }
            ret += battery.getPower(stack);
        }
        return ret;
    }

    @Override
    public long powerCapacity() {
        var ret = 0L;
        for (var i = 0; i < items.getSlots(); i++) {
            var stack = items.getStackInSlot(i);
            if (stack.isEmpty() || !(stack.getItem() instanceof BatteryItem battery)) {
                continue;
            }
            ret += battery.capacity;
        }
        return ret;
    }

    @Override
    public long getVoltage() {
        return voltage.value;
    }

    @Override
    public ElectricMachineType getMachineType() {
        return isDischarge() ? ElectricMachineType.GENERATOR : ElectricMachineType.BUFFER;
    }

    @Override
    public double getPowerGen() {
        var ret = 0d;
        for (var i = 0; i < items.getSlots(); i++) {
            var stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BatteryItem battery) {
                ret += Math.min(voltage.value, battery.getPower(stack));
            }
        }
        return ret;
    }

    @Override
    public double getPowerCons() {
        if (isDischarge()) {
            return 0;
        }
        var ret = 0d;
        for (var i = 0; i < items.getSlots(); i++) {
            var stack = items.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BatteryItem battery) {
                ret += Math.min(voltage.value, battery.capacity - battery.getPower(stack));
            }
        }
        return ret;
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> machine = MACHINE.get(blockEntity));
        eventManager.subscribe(REMOVED_IN_WORLD.get(), world ->
            StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), items));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ELECTRIC_MACHINE.get() || cap == PROCESSOR.get() ||
            cap == LAYOUT_PROVIDER.get()) {
            return myself();
        } else if (cap == MENU_ITEM_HANDLER.get()) {
            return menuItemHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return StackHelper.serializeItemHandler(items);
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        StackHelper.deserializeItemHandler(items, tag);
    }
}
