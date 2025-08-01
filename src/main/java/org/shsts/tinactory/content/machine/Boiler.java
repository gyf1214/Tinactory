package org.shsts.tinactory.content.machine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IFluidCollection;
import org.shsts.tinactory.api.logistics.IItemCollection;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.List;

import static org.shsts.tinactory.content.AllEvents.CLIENT_LOAD;
import static org.shsts.tinactory.content.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.core.machine.MachineProcessor.PROGRESS_PER_TICK;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Boiler extends CapabilityProvider implements
    IProcessor, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final String ID = "machine/boiler";
    private static final double BASE_HEAT = 20d;
    private static final double BURN_HEAT = 100d;
    private static final double BASE_DECAY = 0.0002d;
    private static final double BASE_BURN = 0.02d;
    private static final double BASE_ABSORB = 0.001d;
    private static final double BURN_EFFICIENCY = 10d;

    private final BlockEntity blockEntity;
    private final double burnSpeed;

    private IItemCollection fuelPort;
    private IFluidCollection waterPort;
    private IFluidCollection outputPort;
    private double heat = BASE_HEAT;
    private long maxBurn = 0L;
    private long currentBurn = 0L;
    private double leftSteam = 0d;

    private Boiler(BlockEntity blockEntity, double burnSpeed) {
        this.blockEntity = blockEntity;
        this.burnSpeed = burnSpeed;
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(
        double burnSpeed) {
        return $ -> $.capability(ID, be -> new Boiler(be, burnSpeed));
    }

    public static double getHeat(IProcessor processor) {
        return ((Boiler) processor).heat;
    }

    private void onLoad() {
        var container = AllCapabilities.CONTAINER.get(blockEntity);
        fuelPort = container.getPort(0, true).asItem();
        waterPort = container.getPort(1, true).asFluid();
        outputPort = container.getPort(2, true).asFluid();

        fuelPort.asItemFilter().setFilters(List.of(item -> ForgeHooks.getBurnTime(item, null) > 0));
        waterPort.setFluidFilter(List.of(fluid -> fluid.getFluid() == WATER.fluid().get()));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), $ -> onLoad());
        eventManager.subscribe(CLIENT_LOAD.get(), $ -> onLoad());
    }

    @Override
    public void onPreWork() {
        if (maxBurn > 0) {
            return;
        }
        var item = fuelPort.extractItem(1, false);
        if (item.isEmpty()) {
            return;
        }
        if (item.hasContainerItem()) {
            fuelPort.insertItem(item.getContainerItem(), false);
        }
        maxBurn = (long) ForgeHooks.getBurnTime(item, null) * PROGRESS_PER_TICK;
        currentBurn = 0L;
        blockEntity.setChanged();
    }

    @Override
    public void onWorkTick(double partial) {
        var decay = Math.max(0, heat - BASE_HEAT) * BASE_DECAY;
        var heat1 = heat - decay;
        if (maxBurn > 0) {
            currentBurn += (long) (burnSpeed * (double) PROGRESS_PER_TICK);
            if (currentBurn >= maxBurn) {
                currentBurn = 0;
                maxBurn = 0;
            }
            heat1 += burnSpeed * BASE_BURN;
        }
        if (heat > BURN_HEAT) {
            var absorb = (heat - BURN_HEAT) * BASE_ABSORB;
            var leftSteam1 = leftSteam + absorb * BURN_EFFICIENCY;
            var amount = (int) Math.floor(leftSteam1);
            var drained = waterPort.drain(new FluidStack(WATER.fluid().get(), amount), true);
            var amount1 = drained.getAmount();
            if (!drained.isEmpty()) {
                waterPort.drain(drained, false);
                outputPort.fill(new FluidStack(WATER.fluid("gas").get(), amount1), false);
                leftSteam1 -= amount1;
            }

            var rebate = Math.floor(leftSteam1);
            leftSteam = leftSteam1 - rebate;
            heat = heat1 - absorb + rebate / BURN_EFFICIENCY;
        } else {
            heat = heat1;
        }
        blockEntity.setChanged();
    }

    @Override
    public double getProgress() {
        return maxBurn <= 0 ? 0d : 1d - ((double) currentBurn / (double) maxBurn);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.putDouble("heat", heat);
        tag.putLong("maxBurn", maxBurn);
        tag.putLong("currentBurn", currentBurn);
        tag.putDouble("leftSteam", leftSteam);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        heat = tag.getDouble("heat");
        maxBurn = tag.getLong("maxBurn");
        currentBurn = tag.getLong("currentBurn");
        leftSteam = tag.getDouble("leftSteam");
    }
}
