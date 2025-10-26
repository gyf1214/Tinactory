package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.core.multiblock.Multiblock;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.content.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.content.electric.BatteryBox.DISCHARGE_DEFAULT;
import static org.shsts.tinactory.content.electric.BatteryBox.DISCHARGE_KEY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PowerSubstation extends Multiblock implements IProcessor, IElectricMachine,
    INBTSerializable<CompoundTag> {
    private long output = 0L;
    private long capacity = 0L;
    private long power = 0L;

    public PowerSubstation(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    @Override
    protected void doCheckMultiblock(CheckContext ctx) {
        super.doCheckMultiblock(ctx);
        if (ctx.hasProperty("height") && ctx.hasProperty("power") &&
            ctx.getProperty("power") instanceof PowerBlock block) {
            var height = (int) ctx.getProperty("height") - 2;
            output = block.voltage * 3 * height;
            capacity = block.capacity * 9 * height;
        } else {
            ctx.setFailed();
        }
    }

    private long getPower() {
        return MathUtil.clamp(power, 0, capacity);
    }

    @Override
    public IMenuType menu(IMachine machine) {
        return AllMenus.BATTERY_BOX;
    }

    private boolean isDischarge() {
        if (multiblockInterface == null) {
            return false;
        }
        return multiblockInterface.config().getBoolean(DISCHARGE_KEY, DISCHARGE_DEFAULT);
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        double factor;
        if (isDischarge()) {
            factor = -1;
        } else {
            factor = getInterface()
                .flatMap(IMachine::network)
                .map($ -> $.getComponent(ELECTRIC_COMPONENT.get()).getBufferFactor())
                .orElse(0d);
        }
        var sign = MathUtil.compare(factor);
        if (sign == 0) {
            return;
        }

        var cap = sign > 0 ? getPowerCons() : getPowerGen();
        power = MathUtil.clamp(power + (long) Math.floor(cap * factor), 0, capacity);
        blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        return (double) getPower() / (double) capacity;
    }

    @Override
    public long getVoltage() {
        return getInterface().map($ -> $.voltage.value).orElse(0L);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return isDischarge() ? ElectricMachineType.GENERATOR : ElectricMachineType.BUFFER;
    }

    @Override
    public double getPowerGen() {
        return Math.min(getPower(), output);
    }

    @Override
    public double getPowerCons() {
        return isDischarge() ? 0 : Math.min(capacity - getPower(), output);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == PROCESSOR.get() || cap == ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putLong("power", power);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        power = tag.getLong("power");
    }
}
