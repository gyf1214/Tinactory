package org.shsts.tinactory;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class TinactoryConfig {
    public final ConfigValue<Integer> fluidSlotSize;
    public final ConfigValue<Double> primitiveWorkSpeed;
    public final ConfigValue<Integer> networkConnectDelay;
    public final ConfigValue<Integer> networkMaxConnectsPerTick;
    public final ConfigValue<Integer> initialWorkerSize;
    public final ConfigValue<Integer> initialWorkerDelay;
    public final ConfigValue<Integer> initialWorkerStack;
    public final ConfigValue<Integer> initialWorkerFluidStack;

    public TinactoryConfig(ForgeConfigSpec.Builder builder) {
        builder.push("logistics");
        fluidSlotSize = builder.comment("Default size of a fluid slot.")
                .defineInRange("fluid_slot_size", 16000, 0, Integer.MAX_VALUE);
        initialWorkerSize = builder.comment("Initial worker size for logistics component")
                .defineInRange("initial_worker_size", 1, 0, Integer.MAX_VALUE);
        initialWorkerDelay = builder.comment("Initial worker delay for logistics component")
                .defineInRange("initial_worker_delay", 40, 1, Integer.MAX_VALUE);
        initialWorkerStack = builder.comment("Initial worker stack for logistics component")
                .defineInRange("initial_worker_stack", 1, 1, Integer.MAX_VALUE);
        initialWorkerFluidStack = builder.comment("Initial worker fluid stack for logistics component")
                .defineInRange("initial_worker_fluid_stack", 1000, 1, Integer.MAX_VALUE);
        builder.pop();

        builder.push("machine");
        primitiveWorkSpeed = builder.comment("Work speed multiplier of primitive machines")
                .defineInRange("primitive_work_speed", 0.25, 0d, 1d);
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
