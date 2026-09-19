package org.shsts.tinactory;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.api.network.ISubnetLabel;
import org.shsts.tinactory.content.autocraft.AutocraftComponent;
import org.shsts.tinactory.content.electric.ElectricComponent;
import org.shsts.tinactory.content.logistics.FilterEntry;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.logistics.SignalComponent;
import org.shsts.tinactory.content.logistics.SignalConfig;
import org.shsts.tinactory.content.logistics.StorageDetectorConfig;
import org.shsts.tinactory.core.machine.MachineConfigType;
import org.shsts.tinactory.integration.builder.SchedulingBuilder;
import org.shsts.tinactory.integration.network.ComponentType;
import org.shsts.tinactory.integration.network.NetworkComponent;
import org.shsts.tinactory.integration.network.SubnetLabel;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;

import static org.shsts.tinactory.AllRegistries.COMPONENT_TYPES;
import static org.shsts.tinactory.AllRegistries.MACHINE_CONFIGS;
import static org.shsts.tinactory.AllRegistries.SUBNET_LABELS;
import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllNetworks {
    public static final IEntry<IScheduling> PRE_WORK_SCHEDULING;
    public static final IEntry<IScheduling> WORK_SCHEDULING;
    public static final IEntry<IScheduling> POST_WORK_SCHEDULING;
    public static final IEntry<IScheduling> LOGISTICS_SCHEDULING;
    public static final IEntry<IScheduling> ELECTRIC_SCHEDULING;
    public static final IEntry<IScheduling> PRE_SIGNAL_SCHEDULING;
    public static final IEntry<IScheduling> SIGNAL_READ_SCHEDULING;
    public static final IEntry<IScheduling> SIGNAL_WRITE_SCHEDULING;

    public static final IEntry<ComponentType<ElectricComponent>> ELECTRIC_COMPONENT;
    public static final IEntry<ComponentType<LogisticComponent>> LOGISTIC_COMPONENT;
    public static final IEntry<ComponentType<AutocraftComponent>> AUTOCRAFT_COMPONENT;
    public static final IEntry<ComponentType<SignalComponent>> SIGNAL_COMPONENT;

    public static final IEntry<ISubnetLabel> ELECTRIC_SUBNET;
    public static final IEntry<ISubnetLabel> LOGISTICS_SUBNET;

    public static final IEntry<IMachineConfigType<Component>> MACHINE_NAME;
    public static final IEntry<IMachineConfigType<Boolean>> AUTO_VOID;
    public static final IEntry<IMachineConfigType<ResourceLocation>> TARGET_RECIPE;
    public static final IEntry<IMachineConfigType<Integer>> MACHINE_PARALLEL;
    public static final IEntry<IMachineConfigType<Integer>> STORAGE_PRIORITY;
    public static final IEntry<IMachineConfigType<List<FilterEntry>>> STORAGE_FILTERS;
    public static final IEntry<IMachineConfigType<Boolean>> BATTERY_DISCHARGE;
    public static final IEntry<IMachineConfigType<StorageDetectorConfig>> STORAGE_DETECTOR;
    public static final IEntry<IMachineConfigType<SignalConfig>> SIGNAL_CONFIG;

    static {
        PRE_WORK_SCHEDULING = scheduling("machine/pre_work").register();
        WORK_SCHEDULING = scheduling("machine/work").after(PRE_WORK_SCHEDULING).register();
        POST_WORK_SCHEDULING = scheduling("machine/post_work").after(WORK_SCHEDULING).register();
        LOGISTICS_SCHEDULING = scheduling("logistics").before(PRE_WORK_SCHEDULING).register();
        ELECTRIC_SCHEDULING = scheduling("electric")
            .after(PRE_WORK_SCHEDULING)
            .before(WORK_SCHEDULING)
            .register();
        PRE_SIGNAL_SCHEDULING = scheduling("signal/pre").register();
        SIGNAL_READ_SCHEDULING = scheduling("signal/read")
            .after(POST_WORK_SCHEDULING)
            .register();
        SIGNAL_WRITE_SCHEDULING = scheduling("signal/write")
            .before(LOGISTICS_SCHEDULING)
            .after(PRE_SIGNAL_SCHEDULING)
            .register();

        ELECTRIC_COMPONENT = componentType("electric", ElectricComponent.class, ElectricComponent::new);
        LOGISTIC_COMPONENT = componentType("logistics", LogisticComponent.class, LogisticComponent::new);
        AUTOCRAFT_COMPONENT = componentType("autocraft", AutocraftComponent.class, AutocraftComponent::new);
        SIGNAL_COMPONENT = componentType("signal", SignalComponent.class, SignalComponent::new);

        ELECTRIC_SUBNET = subnetLabel("electric");
        LOGISTICS_SUBNET = subnetLabel("logistics");

        MACHINE_NAME = legacyConfig("name", ComponentSerialization.CODEC);
        AUTO_VOID = legacyConfig("auto_void", Codec.BOOL, "void");
        TARGET_RECIPE = legacyConfig("target_recipe", ResourceLocation.CODEC, "targetRecipe");
        MACHINE_PARALLEL = legacyConfig("parallel", Codec.INT);
        STORAGE_PRIORITY = legacyConfig("storage_priority", Codec.INT, "priority");
        STORAGE_FILTERS = legacyConfig("storage_filters", FilterEntry.CODEC.listOf(), "filter");
        BATTERY_DISCHARGE = legacyConfig("battery_discharge", Codec.BOOL, "discharge");
        STORAGE_DETECTOR = REGISTRATE.registryEntry(MACHINE_CONFIGS.getHandler(), "storage_detector",
            StorageDetectorConfig::configType);
        SIGNAL_CONFIG = legacyConfig("signal", SignalConfig.CODEC);
    }

    public static void init() {}

    private static <T extends NetworkComponent> IEntry<ComponentType<T>> componentType(
        String id, Class<T> clazz, NetworkComponent.Factory<T> factory) {
        return REGISTRATE.registryEntry(COMPONENT_TYPES.getHandler(), id,
            () -> new ComponentType<>(clazz, factory));
    }

    private static SchedulingBuilder<IRegistrate> scheduling(String id) {
        return new SchedulingBuilder<>(REGISTRATE, REGISTRATE, id);
    }

    private static IEntry<ISubnetLabel> subnetLabel(String id) {
        return REGISTRATE.registryEntry(SUBNET_LABELS.getHandler(), id, SubnetLabel::new);
    }

    private static <T> IEntry<IMachineConfigType<T>> legacyConfig(String id, Codec<T> codec, String legacyKey) {
        return REGISTRATE.registryEntry(MACHINE_CONFIGS.getHandler(), id,
            () -> new MachineConfigType<>(codec, legacyKey));
    }

    private static <T> IEntry<IMachineConfigType<T>> legacyConfig(String id, Codec<T> codec) {
        return legacyConfig(id, codec, id);
    }
}
