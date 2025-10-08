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
    public final ConfigValue<Integer> chestSize;
    public final ConfigValue<Integer> tankSize;
    public final ConfigValue<List<? extends Integer>> logisticWorkerSize;
    public final ConfigValue<List<? extends Integer>> logisticWorkerDelay;
    public final ConfigValue<List<? extends Integer>> logisticWorkerStack;
    public final ConfigValue<List<? extends Integer>> logisticWorkerFluidStack;
    public final ConfigValue<Integer> bytesPerItem;
    public final ConfigValue<Integer> bytesPerItemType;
    public final ConfigValue<Integer> bytesPerFluid;
    public final ConfigValue<Integer> bytesPerFluidType;
    public final ConfigValue<Double> logisticWorkerAmperage;
    public final ConfigValue<Double> electricStorageAmperage;
    public final ConfigValue<Double> primitiveWorkSpeed;
    public final ConfigValue<List<? extends Double>> machineResistanceFactor;
    public final ConfigValue<Double> workFactorExponent;
    public final ConfigValue<Double> blastFurnaceTempFactor;
    public final ConfigValue<Double> cleanroomAmperage;
    public final ConfigValue<Double> cleanroomBaseClean;
    public final ConfigValue<Double> cleanroomBaseDecay;
    public final ConfigValue<Double> cleanroomOpenDecay;
    public final ConfigValue<Double> lithographyCleannessFactor;
    public final ConfigValue<Integer> networkConnectDelay;
    public final ConfigValue<Integer> networkMaxConnectsPerTick;
    public final ConfigValue<Integer> multiblockCheckCycle;

    public TinactoryConfig(ForgeConfigSpec.Builder builder) {
        builder.push("logistics");
        fluidSlotSize = builder.comment("Default size of a fluid slot.")
            .defineInRange("fluid_slot_size", 16000, 0, Integer.MAX_VALUE);
        chestSize = builder.comment("Size of the electric chest")
            .defineInRange("chest_size", 1024, 1, Integer.MAX_VALUE);
        tankSize = builder.comment("Size of the electric tank")
            .defineInRange("tank_size", 256000, 1, Integer.MAX_VALUE);

        Predicate<Object> validator = i -> ((Number) i).doubleValue() > 0d;
        logisticWorkerSize = builder.comment("Logistic Worker sizes")
            .defineList("logistic_worker_size", List.of(8, 8, 16, 16, 32), validator);
        logisticWorkerDelay = builder.comment("Logistic Worker delays")
            .defineList("logistic_worker_delay", List.of(40, 40, 20, 20, 10), validator);
        logisticWorkerStack = builder.comment("Logistic Worker item stacks per cycle")
            .defineList("logistic_worker_stack", List.of(4, 16, 64, 64, 128), validator);
        logisticWorkerFluidStack = builder.comment("Logistic Worker fluid stacks per cycle")
            .defineList("logistic_worker_fluid_stack", List.of(1000, 4000, 16000, 16000, 32000), validator);

        bytesPerItem = builder.comment("Bytes used per item by digital storage")
            .defineInRange("bytes_per_item", 256, 1, Integer.MAX_VALUE);
        bytesPerItemType = builder.comment("Bytes used per item type by digital storage")
            .defineInRange("bytes_per_item_type", 4096, 1, Integer.MAX_VALUE);
        bytesPerFluid = builder.comment("Bytes used per fluid by digital storage")
            .defineInRange("bytes_per_fluid", 1, 1, Integer.MAX_VALUE);
        bytesPerFluidType = builder.comment("Bytes used per fluid type by digital storage")
            .defineInRange("bytes_per_fluid_type", 4096, 1, Integer.MAX_VALUE);

        logisticWorkerAmperage = builder.comment("Amperage usage on Logistic Worker")
            .defineInRange("logistic_worker_amperage", 0.125d, 0d, Double.POSITIVE_INFINITY);
        electricStorageAmperage = builder.comment("Amperage usage on Electric Storage")
            .defineInRange("electric_storage_amperage", 0.125d, 0d, Double.POSITIVE_INFINITY);
        builder.pop();

        builder.push("machine");
        primitiveWorkSpeed = builder.comment("Work speed multiplier of primitive machines")
            .defineInRange("primitive_work_speed", 0.25d, 0d, 1d);
        machineResistanceFactor = builder.comment("Machine resistance factor")
            .defineList("machine_resistance_factor", List.of(0.05d, 0.1d, 0.1d, 0.2d, 0.2d, 0.4d), validator);
        workFactorExponent = builder.comment("Work factor exponent")
            .defineInRange("work_factor_exponent", 2d, 0d, Double.POSITIVE_INFINITY);
        blastFurnaceTempFactor = builder.comment("Temperature factor for blast furnace")
            .defineInRange("blast_furnace_temp_factor", 1000d, 0d, Double.POSITIVE_INFINITY);
        cleanroomAmperage = builder.comment("Cleanroom amperage usage")
            .defineInRange("cleanroom_amperage", 0.125d, 0d, Double.POSITIVE_INFINITY);
        cleanroomBaseClean = builder.comment("Cleanroom base clean speed in ULV")
            .defineInRange("cleanroom_base_clean", 5e-5d, 0d, 1d);
        cleanroomBaseDecay = builder.comment("Cleanroom base decay")
            .defineInRange("cleanroom_base_decay", 1e-4d, 0d, 1d);
        cleanroomOpenDecay = builder.comment("Cleanroom decay when open")
            .defineInRange("cleanroom_open_decay", 0.01d, 0d, 1d);
        lithographyCleannessFactor = builder.comment("Cleanness factor in lithography")
            .defineInRange("lithography_cleanness_factory", 2d, 0d, Double.POSITIVE_INFINITY);
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
