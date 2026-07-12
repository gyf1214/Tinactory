package org.shsts.tinactory;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.metrics.IMetricsCallback;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.api.network.ISubnetLabel;
import org.shsts.tinactory.integration.common.SimpleFluid;
import org.shsts.tinactory.integration.common.SimpleFluidType;
import org.shsts.tinactory.integration.metrics.MetricsManager;
import org.shsts.tinactory.integration.network.ComponentType;
import org.shsts.tinactory.integration.network.SubnetLabel;
import org.shsts.tinactory.integration.network.WorldNetworkManagers;
import org.shsts.tinycorelib.api.blockentity.IEvent;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRegistry;
import org.shsts.tinycorelib.api.registrate.handler.IEntryHandler;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinycorelib.api.CoreLibKeys.EVENT_REGISTRY_KEY;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRegistries {
    public static final IRegistry<IScheduling> SCHEDULINGS;
    public static final IRegistry<IComponentType<?>> COMPONENT_TYPES;
    public static final IRegistry<ISubnetLabel> SUBNET_LABELS;
    public static final IRegistry<IMetricsCallback> METRICS_CALLBACKS;

    public static final IEntryHandler<IEvent<?>> EVENTS;
    public static final IEntryHandler<Block> BLOCKS;
    public static final IEntryHandler<Item> ITEMS;
    public static final IEntryHandler<FluidType> FLUID_TYPES;
    public static final IEntryHandler<Fluid> FLUIDS;
    public static final IEntryHandler<Feature<?>> FEATURES;
    public static final IEntryHandler<SoundEvent> SOUND_EVENTS;

    static {
        SCHEDULINGS = REGISTRATE.registry("scheduling", IScheduling.class)
            .onBake(WorldNetworkManagers::setSchedulings)
            .register();
        COMPONENT_TYPES = REGISTRATE.<IComponentType<?>>genericRegistry("component_type", IComponentType.class)
            .onBake(ComponentType::onBake)
            .register();
        SUBNET_LABELS = REGISTRATE.registry("subnet_label", ISubnetLabel.class)
            .onBake(SubnetLabel::onBake)
            .register();
        METRICS_CALLBACKS = REGISTRATE.registry(TinactoryKeys.METRICS_CALLBACKS, IMetricsCallback.class)
            .onBake(MetricsManager::onBake)
            .register();

        EVENTS = REGISTRATE.getHandler(EVENT_REGISTRY_KEY);
        BLOCKS = REGISTRATE.getHandler(Registries.BLOCK, BuiltInRegistries.BLOCK);
        ITEMS = REGISTRATE.getHandler(Registries.ITEM, BuiltInRegistries.ITEM);
        FLUID_TYPES = REGISTRATE.getHandler(NeoForgeRegistries.Keys.FLUID_TYPES, NeoForgeRegistries.FLUID_TYPES);
        FLUIDS = REGISTRATE.getHandler(Registries.FLUID, BuiltInRegistries.FLUID);
        FEATURES = REGISTRATE.getHandler(Registries.FEATURE, BuiltInRegistries.FEATURE);
        SOUND_EVENTS = REGISTRATE.getHandler(Registries.SOUND_EVENT, BuiltInRegistries.SOUND_EVENT);
    }

    public static void init() {}

    public static IEntry<SimpleFluid> simpleFluid(String id, ResourceLocation tex,
        int texColor, int displayColor) {
        var translation = "fluid." + TinactoryKeys.ID + "." + id.replace("/", ".");
        var fluidType = REGISTRATE.registryEntry(FLUID_TYPES, id, () -> new SimpleFluidType(translation));
        var fluid = REGISTRATE.registryEntry(FLUIDS, id, () -> new SimpleFluid(fluidType, displayColor));
        if (FMLEnvironment.dist.isClient()) {
            AllClientEvents.registerFluidTex(fluidType, texColor, tex);
        }
        REGISTRATE.trackLang(translation);
        return fluid;
    }
}
