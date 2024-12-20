package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TinactoryConfig {
    public final ConfigValue<Integer> fluidSlotSize;
    public final ConfigValue<Integer> baseFluidCellSize;
    public final ConfigValue<Integer> chestSize;
    public final ConfigValue<Integer> tankSize;
    public final ConfigValue<List<? extends Integer>> workerSize;
    public final ConfigValue<List<? extends Integer>> workerDelay;
    public final ConfigValue<List<? extends Integer>> workerStack;
    public final ConfigValue<List<? extends Integer>> workerFluidStack;
    public final ConfigValue<Double> primitiveWorkSpeed;
    public final ConfigValue<Double> machineResistanceFactor;
    public final ConfigValue<Double> cableResistanceFactor;
    public final ConfigValue<Double> workFactorExponent;
    public final ConfigValue<Double> blastFurnaceTempFactor;
    public final ConfigValue<Integer> networkConnectDelay;
    public final ConfigValue<Integer> networkMaxConnectsPerTick;

    public TinactoryConfig(ForgeConfigSpec.Builder builder) {
        builder.push("logistics");
        fluidSlotSize = builder.comment("Default size of a fluid slot.")
            .defineInRange("fluid_slot_size", 16000, 0, Integer.MAX_VALUE);
        baseFluidCellSize = builder.comment("Size of the base fluid cell.")
            .defineInRange("base_fluid_cell_size", 4000, 0, Integer.MAX_VALUE);
        chestSize = builder.comment("Size of the electric chest")
            .defineInRange("chest_size", 1024, 1, Integer.MAX_VALUE);
        tankSize = builder.comment("Size of the electric tank")
            .defineInRange("tank_size", 256000, 1, Integer.MAX_VALUE);

        Predicate<Object> validator = i -> (int) i > 0;
        workerSize = builder.comment("Worker sizes for logistics component")
            .defineList("worker_size", List.of(8, 8, 16, 16, 32, 32), validator);
        workerDelay = builder.comment("Worker delays for logistics component")
            .defineList("worker_delay", List.of(40, 40, 20, 20, 10, 10), validator);
        workerStack = builder.comment("Worker item stacks for logistics component")
            .defineList("worker_stack", List.of(4, 16, 64, 64, 128, 128), validator);
        workerFluidStack = builder.comment("Worker fluid stacks for logistics component")
            .defineList("worker_fluid_stack", List.of(1000, 4000, 16000, 16000, 32000, 32000), validator);
        builder.pop();

        builder.push("machine");
        primitiveWorkSpeed = builder.comment("Work speed multiplier of primitive machines")
            .defineInRange("primitive_work_speed", 0.25, 0d, 1d);
        machineResistanceFactor = builder.comment("Machine resistance factor")
            .defineInRange("machine_resistance_factor", 0.1d, 0d, Double.POSITIVE_INFINITY);
        cableResistanceFactor = builder.comment("Cable resistance factor")
            .defineInRange("cable_resistance_factor", 0.01d, 0d, Double.POSITIVE_INFINITY);
        workFactorExponent = builder.comment("Work factor exponent")
            .defineInRange("work_factor_exponent", 2d, 0d, Double.POSITIVE_INFINITY);
        blastFurnaceTempFactor = builder.comment("Temperature factor for blast furnace")
            .defineInRange("blast_furnace_temp_factor", 1000d, 0d, Double.POSITIVE_INFINITY);
        builder.pop();

        builder.push("network");
        networkConnectDelay = builder.comment("Delay in ticks when network reconnects")
            .defineInRange("connect_delay", 5, 0, Integer.MAX_VALUE);
        networkMaxConnectsPerTick = builder.comment("Max connection iteration for network reconnects per tick")
            .defineInRange("max_connects", 100, 1, Integer.MAX_VALUE);
        builder.pop();
    }

    public static final TinactoryConfig INSTANCE;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        var pair = (new ForgeConfigSpec.Builder()).configure(TinactoryConfig::new);

        INSTANCE = pair.getKey();
        CONFIG_SPEC = pair.getValue();
    }
}
