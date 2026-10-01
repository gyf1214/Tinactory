package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.shsts.tinactory.AllMenus;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.multiblock.IMultiblockCheckCtx;
import org.shsts.tinactory.content.electric.BatteryBoxMode;
import org.shsts.tinactory.content.electric.IBatteryBox;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.multiblock.Multiblock;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinycorelib.api.blockentity.ICapabilityBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;

import static org.shsts.tinactory.AllCapabilities.ELECTRIC_MACHINE;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllNetworks.BATTERY_MODE;
import static org.shsts.tinactory.AllNetworks.ELECTRIC_COMPONENT;
import static org.shsts.tinactory.content.electric.BatteryBox.MODE_DEFAULT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PowerSubstation extends Multiblock implements IBatteryBox,
    IElectricMachine, INBTSerializable<CompoundTag> {
    private long output = 0L;
    private long capacity = 0L;
    private long power = 0L;

    public PowerSubstation(BlockEntity blockEntity, Builder<?> builder) {
        super(blockEntity, builder);
    }

    @Override
    protected void doCheckStructure(IMultiblockCheckCtx<BlockState> ctx) {
        super.doCheckStructure(ctx);
        if (ctx.hasProperty("height") && ctx.hasProperty("power") &&
            ctx.getProperty("power") instanceof PowerBlock block) {
            var height = (int) ctx.getProperty("height") - 2;
            output = block.voltage.value * 3 * height;
            capacity = block.capacity * 9 * height;
        } else {
            ctx.setFailed();
        }
    }

    @Override
    public IMenuType menu(MultiblockInterface machine) {
        return AllMenus.BATTERY_BOX;
    }

    private BatteryBoxMode mode() {
        if (multiblockInterface == null) {
            return MODE_DEFAULT;
        }
        return multiblockInterface.config().get(BATTERY_MODE).orElse(MODE_DEFAULT);
    }

    @Override
    public void onPreWork() {}

    @Override
    public void onWorkTick(double partial) {
        var currentMode = mode();
        var factor = getInterface()
            .flatMap(IMachine::network)
            .map($ -> {
                var electric = $.getComponent(ELECTRIC_COMPONENT.get());
                return switch (currentMode) {
                    case BUFFER -> electric.getBufferFactor();
                    case CHARGE -> electric.getWorkFactor();
                    case DISCHARGE -> -1d;
                };
            })
            .orElse(0d);
        var sign = MathUtil.compare(factor);
        if (sign == 0) {
            return;
        }

        var cap = sign > 0 ? getPowerCons() : getPowerGen();
        power = Math.clamp(power + (long) Math.floor(cap * factor), 0, capacity);
        blockEntity.setChanged();
    }

    @Override
    public long powerLevel() {
        return Math.clamp(power, 0, capacity);
    }

    @Override
    public long powerCapacity() {
        return capacity;
    }

    @Override
    public long getVoltage() {
        return getInterface().map($ -> $.voltage.value).orElse(0L);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return switch (mode()) {
            case BUFFER -> ElectricMachineType.BUFFER;
            case CHARGE -> ElectricMachineType.CONSUMER;
            case DISCHARGE -> ElectricMachineType.GENERATOR;
        };
    }

    @Override
    public double getPowerGen() {
        if (mode() == BatteryBoxMode.CHARGE) {
            return 0d;
        }
        return Math.min(powerLevel(), output);
    }

    @Override
    public double getPowerCons() {
        return mode() == BatteryBoxMode.DISCHARGE ? 0 : Math.min(capacity - powerLevel(), output);
    }

    @Override
    public void attachCapability(ICapabilityBuilder builder) {
        builder.attach(PROCESSOR, this);
        builder.attach(ELECTRIC_MACHINE, this);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        tag.putLong("power", power);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        power = tag.getLong("power");
    }
}
