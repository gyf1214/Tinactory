package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;

import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionReactor extends ProcessingRuntime {
    private final Properties properties;
    private boolean hasPendingRecipe = false;
    private boolean charging = false;
    private double startupEnergy = 0d;

    public record Properties(long startEnergyFactor, double chargeAmperage, double decayRate) {
        public static Properties fromJson(JsonObject jo) {
            return new Properties(
                GsonHelper.getAsLong(jo, "startEnergyFactor"),
                GsonHelper.getAsDouble(jo, "chargeAmperage"),
                GsonHelper.getAsDouble(jo, "decayRate"));
        }
    }

    public FusionReactor(ProcessingRuntime.Properties runtimeProperties, Properties properties) {
        super(runtimeProperties);
        this.properties = properties;
    }

    public static Function<ProcessingRuntime.Properties, ProcessingRuntime> factory(Properties properties) {
        return runtimeProperties -> new FusionReactor(runtimeProperties, properties);
    }

    private Optional<Voltage> voltage() {
        return machine().map($ -> ((MultiblockInterface) $).voltage);
    }

    private double startupCapacity() {
        return voltage()
            .map($ -> Math.scalb((double) properties.startEnergyFactor, $.rank))
            .orElse(0d);
    }

    private double chargeRate() {
        return voltage()
            .map($ -> (double) $.value * properties.chargeAmperage)
            .orElse(0d);
    }

    private boolean isFull() {
        return startupEnergy >= startupCapacity();
    }

    private boolean canCharge() {
        return hasPendingRecipe || hasCurrentRecipe();
    }

    @Override
    protected <T> boolean gateRecipe(IRecipeProcessor<T> processor, IMachine machine, T recipe) {
        hasPendingRecipe = true;
        return isFull();
    }

    @Override
    protected void beforeWorkBegin() {
        startupEnergy = startupCapacity();
        setChanged();
    }

    @Override
    public void onPreWork() {
        var needCharge = !isFull();
        hasPendingRecipe = false;
        if (needCharge) {
            onContainerChange();
        }
        super.onPreWork();
        charging = needCharge && canCharge();
    }

    @Override
    public void onWorkTick(double partial) {
        var energyBefore = startupEnergy;
        if (charging) {
            startupEnergy = Math.min(startupCapacity(), startupEnergy + chargeRate() * partial);
            if (isFull()) {
                onContainerChange();
            }
        } else if (!canCharge()) {
            startupEnergy *= 1d - properties.decayRate;
            if (startupEnergy < 1d) {
                startupEnergy = 0d;
            }
        }
        if (startupEnergy != energyBefore) {
            setChanged();
        }
        super.onWorkTick(partial);
    }

    @Override
    public boolean isWorking(double partial) {
        return charging || super.isWorking(partial);
    }

    @Override
    public ElectricMachineType machineType() {
        return charging ? ElectricMachineType.CONSUMER : super.machineType();
    }

    @Override
    public double powerCons() {
        if (!charging) {
            return super.powerCons();
        }
        return Math.min(chargeRate(), Math.max(0d, startupCapacity() - startupEnergy));
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = super.serializeNBT();
        tag.putDouble("startupEnergy", startupEnergy);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        super.deserializeNBT(tag);
        startupEnergy = tag.getDouble("startupEnergy");
    }
}
