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
    public final ConfigValue<List<? extends Double>> machineResistanceFactor;
    public final ConfigValue<List<? extends Double>> cableResistanceFactor;
    public final ConfigValue<Double> workFactorExponent;
    public final ConfigValue<Double> blastFurnaceTempFactor;
    public final ConfigValue<Double> cleanroomAmperage;
    public final ConfigValue<Double> cleanroomBaseClean;
    public final ConfigValue<Double> cleanroomBaseDecay;
    public final ConfigValue<Double> cleanroomOpenDecay;
    public final ConfigValue<Integer> networkConnectDelay;
    public final ConfigValue<Integer> networkMaxConnectsPerTick;
    public final ConfigValue<Integer> multiblockCheckCycle;

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
            .defineList("worker_size", List.of(8, 8, 16, 16, 32), validator);
        workerDelay = builder.comment("Worker delays for logistics component")
            .defineList("worker_delay", List.of(40, 40, 20, 20, 10), validator);
        workerStack = builder.comment("Worker item stacks for logistics component")
            .defineList("worker_stack", List.of(4, 16, 64, 64, 128), validator);
        workerFluidStack = builder.comment("Worker fluid stacks for logistics component")
            .defineList("worker_fluid_stack", List.of(1000, 4000, 16000, 16000, 32000), validator);
        builder.pop();

        builder.push("machine");
        primitiveWorkSpeed = builder.comment("Work speed multiplier of primitive machines")
            .defineInRange("primitive_work_speed", 0.25, 0d, 1d);
        machineResistanceFactor = builder.comment("Machine resistance factor")
            .defineList("machine_resistance_factor", List.of(0.01d, 0.02d, 0.02d, 0.04d, 0.04d, 0.08d), validator);
        cableResistanceFactor = builder.comment("Cable resistance factor")
            .defineList("cable_resistance_factor", List.of(0.01d), validator);
        workFactorExponent = builder.comment("Work factor exponent")
            .defineInRange("work_factor_exponent", 2d, 0d, Double.POSITIVE_INFINITY);
        blastFurnaceTempFactor = builder.comment("Temperature factor for blast furnace")
            .defineInRange("blast_furnace_temp_factor", 1000d, 0d, Double.POSITIVE_INFINITY);
        cleanroomAmperage = builder.comment("Cleanroom amperage usage")
            .defineInRange("cleanroom_amperage", 0.125d, 0d, Double.POSITIVE_INFINITY);
        cleanroomBaseClean = builder.comment("Cleanroom base clean speed in ULV")
            .defineInRange("cleanroom_base_clean", 1e-4d, 0d, 1d);
        cleanroomBaseDecay = builder.comment("Cleanroom base decay")
            .defineInRange("cleanroom_base_decay", 1e-4d, 0d, 1d);
        cleanroomOpenDecay = builder.comment("Cleanroom decay when open")
            .defineInRange("cleanroom_open_decay", 0.01d, 0d, 1d);
        builder.pop();

        builder.push("network");
        networkConnectDelay = builder.comment("Delay in ticks when network reconnects")
            .defineInRange("connect_delay", 5, 0, Integer.MAX_VALUE);
        networkMaxConnectsPerTick = builder.comment("Max connection iteration for network reconnects per tick")
            .defineInRange("max_connects", 100, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("multiblock");
        multiblockCheckCycle = builder.comment("Interval for multiblock to do check")
            .defineInRange("check_cycle", 20, 1, Integer.MAX_VALUE);
        builder.pop();
    }

    public static final TinactoryConfig CONFIG;
    public static final ForgeConfigSpec CONFIG_SPEC;

    static {
        var pair = new ForgeConfigSpec.Builder().configure(TinactoryConfig::new);

        CONFIG = pair.getKey();
        CONFIG_SPEC = pair.getValue();
    }

    public static <T> T listConfig(ConfigValue<List<? extends T>> config, int idx) {
        var list = config.get();
        assert !list.isEmpty();
        return list.get(Math.min(list.size() - 1, idx));
    }
}
