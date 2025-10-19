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
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.multiblock.Multiblock;

import static org.shsts.tinactory.content.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;

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
            var height = (int) ctx.getProperty("height");
            output = block.voltage * 3 * height;
            capacity = block.capacity * 9 * height;
        } else {
            ctx.setFailed();
        }
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {}

    @Override
    public double getProgress() {
        return (double) power / (double) capacity;
    }

    @Override
    public long getVoltage() {
        return getInterface().map($ -> $.voltage.value).orElse(0L);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return ElectricMachineType.BUFFER;
    }

    @Override
    public double getPowerGen() {
        return Math.min(power, output);
    }

    @Override
    public double getPowerCons() {
        return Math.min(capacity - power, output);
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
