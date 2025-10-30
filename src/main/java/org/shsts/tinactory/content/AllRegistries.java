package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.TinactoryKeys;
import org.shsts.tinactory.api.metrics.IMetricsCallback;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.metrics.MetricsManager;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.SchedulingManager;
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
    public static final IRegistry<IMetricsCallback> METRICS_CALLBACKS;

    public static final IEntryHandler<IEvent<?>> EVENTS;
    public static final IEntryHandler<Block> BLOCKS;
    public static final IEntryHandler<Item> ITEMS;
    public static final IEntryHandler<Fluid> FLUIDS;
    public static final IEntryHandler<Feature<?>> FEATURES;
    public static final IEntryHandler<ForgeWorldPreset> WORLD_TYPES;
    public static final IEntryHandler<SoundEvent> SOUND_EVENTS;

    static {
        SCHEDULINGS = REGISTRATE.registry("scheduling", IScheduling.class)
            .onBake((registry, stage) -> SchedulingManager.onBake(registry))
            .register();
        COMPONENT_TYPES = REGISTRATE.<IComponentType<?>>genericRegistry("component_type", IComponentType.class)
            .onBake((registry, stage) -> ComponentType.onBake(registry))
            .register();
        METRICS_CALLBACKS = REGISTRATE.registry(TinactoryKeys.METRICS_CALLBACKS, IMetricsCallback.class)
            .onBake((registry, stage) -> MetricsManager.onBake(registry))
            .register();

        EVENTS = REGISTRATE.getHandler(EVENT_REGISTRY_KEY, IEvent.class);
        BLOCKS = REGISTRATE.getHandler(ForgeRegistries.BLOCKS);
        ITEMS = REGISTRATE.getHandler(ForgeRegistries.ITEMS);
        FLUIDS = REGISTRATE.getHandler(ForgeRegistries.FLUIDS);
        FEATURES = REGISTRATE.getHandler(ForgeRegistries.FEATURES);
        WORLD_TYPES = REGISTRATE.getHandler(ForgeRegistries.Keys.WORLD_TYPES, ForgeWorldPreset.class);
        SOUND_EVENTS = REGISTRATE.getHandler(ForgeRegistries.SOUND_EVENTS);
    }

    public static void init() {}

    public static IEntry<SimpleFluid> simpleFluid(String id, ResourceLocation tex,
        int texColor, int displayColor) {
        var ret = REGISTRATE.registryEntry(FLUIDS, id, () -> new SimpleFluid(tex, texColor, displayColor));
        REGISTRATE.trackLang("fluid." + TinactoryKeys.ID + "." + id.replace("/", "."));
        return ret;
    }
}
