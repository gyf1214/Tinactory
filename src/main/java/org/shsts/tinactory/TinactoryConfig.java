package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TinactoryConfig {
    public final ConfigValue<Integer> fluidSlotSize;
    public final ConfigValue<Integer> bytesPerItem;
    public final ConfigValue<Integer> bytesPerItemType;
    public final ConfigValue<Integer> bytesPerFluid;
    public final ConfigValue<Integer> bytesPerFluidType;
    public final ConfigValue<Double> primitiveWorkSpeed;
    public final ConfigValue<List<? extends Double>> machineResistanceFactor;
    public final ConfigValue<Double> workFactorExponent;
    public final ConfigValue<Double> coilTemperatureFactor;
    public final ConfigValue<Integer> networkConnectDelay;
    public final ConfigValue<Integer> networkMaxConnectsPerTick;
    public final ConfigValue<Integer> multiblockCheckCycle;

    public TinactoryConfig(ForgeConfigSpec.Builder builder) {
        builder.push("logistics");
        fluidSlotSize = builder.comment("Default size of a fluid slot.")
            .defineInRange("fluid_slot_size", 16000, 0, Integer.MAX_VALUE);

        bytesPerItem = builder.comment("Bytes used per item by digital storage")
            .defineInRange("bytes_per_item", 256, 1, Integer.MAX_VALUE);
        bytesPerItemType = builder.comment("Bytes used per item type by digital storage")
            .defineInRange("bytes_per_item_type", 4096, 1, Integer.MAX_VALUE);
        bytesPerFluid = builder.comment("Bytes used per fluid by digital storage")
            .defineInRange("bytes_per_fluid", 1, 1, Integer.MAX_VALUE);
        bytesPerFluidType = builder.comment("Bytes used per fluid type by digital storage")
            .defineInRange("bytes_per_fluid_type", 4096, 1, Integer.MAX_VALUE);

        builder.pop();

        builder.push("machine");
        primitiveWorkSpeed = builder.comment("Work speed multiplier of primitive machines")
            .defineInRange("primitive_work_speed", 0.25d, 0d, 1d);
        machineResistanceFactor = builder.comment("Machine resistance factor")
            .defineList("machine_resistance_factor", List.of(0d),
                i -> ((Number) i).doubleValue() >= 0d);
        workFactorExponent = builder.comment("Work factor exponent")
            .defineInRange("work_factor_exponent", 2d, 0d, Double.POSITIVE_INFINITY);
        coilTemperatureFactor = builder.comment("Temperature energy factor for coil machines")
            .defineInRange("coil_temperature_factor", 1000d, 0d, Double.POSITIVE_INFINITY);
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
