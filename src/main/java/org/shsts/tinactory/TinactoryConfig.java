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

    public TinactoryConfig(ForgeConfigSpec.Builder builder) {
        builder.push("logistics");
        this.fluidSlotSize = builder.comment("Default size of a fluid slot.")
                .defineInRange("fluid_slot_size", 16000, 0, Integer.MAX_VALUE);
        builder.pop();

        builder.push("machine");
        this.primitiveWorkSpeed = builder.comment("Work speed multiplier of primitive machines")
                .defineInRange("primitive_work_speed", 0.25d, 0d, 1d);
        builder.pop();

        builder.push("network");
        this.networkConnectDelay = builder.comment("Delay in ticks when network reconnects")
                .defineInRange("connect_delay", 5, 0, Integer.MAX_VALUE);
        this.networkMaxConnectsPerTick = builder.comment("Max connection iteration for network reconnects per tick")
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
