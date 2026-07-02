package org.shsts.tinactory.content.multiblock;

import com.google.gson.JsonObject;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;

import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FusionRuntime extends ProcessingRuntime {
    private final Properties properties;
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

    public FusionRuntime(ProcessingRuntime.Properties runtimeProperties, Properties properties) {
        super(runtimeProperties);
        this.properties = properties;
    }

    public static Function<ProcessingRuntime.Properties, ProcessingRuntime> factory(Properties properties) {
        return runtimeProperties -> new FusionRuntime(runtimeProperties, properties);
    }

    private Optional<Voltage> voltage() {
        return machine().map($ -> ((MultiblockInterface) $).voltage);
    }

    public double startupCapacity() {
        return voltage()
            .map($ -> Math.scalb((double) properties.startEnergyFactor, $.rank))
            .orElse(0d);
    }

    private double chargeRate() {
        return voltage()
            .map($ -> (double) $.value * properties.chargeAmperage)
            .orElse(0d);
    }

    public double startupEnergy() {
        return startupEnergy;
    }

    @Override
    public void onPreWork() {
        super.onPreWork();
        charging = hasCurrentRecipe() && startupEnergy < startupCapacity();
    }

    @Override
    public void onWorkTick(double partial) {
        var energyBefore = startupEnergy;
        if (charging) {
            startupEnergy = Math.min(startupCapacity(), startupEnergy + chargeRate() * partial);
        } else if (!hasCurrentRecipe()) {
            startupEnergy *= 1d - properties.decayRate;
            if (startupEnergy < 1d) {
                startupEnergy = 0d;
            }
        } else {
            super.onWorkTick(partial);
        }
        if (startupEnergy != energyBefore) {
            setChanged();
        }
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
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = super.serializeNBT(provider);
        tag.putDouble("startupEnergy", startupEnergy);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        startupEnergy = tag.getDouble("startupEnergy");
    }
}
