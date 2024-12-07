package org.shsts.tinactory.registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.network.SchedulingManager;
import org.shsts.tinactory.registrate.common.SmartRegistry;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRegistry;
import org.shsts.tinycorelib.api.registrate.handler.IEntryHandler;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.Tinactory._REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRegistries {
    public static final SmartRegistry<IScheduling> SCHEDULING_REGISTRY;
    public static final SmartRegistry<Event<?>> EVENT;

    public static final IRegistry<ComponentType<?>> COMPONENT_TYPES;
    public static final IEntryHandler<Fluid> FLUIDS;
    public static final IEntryHandler<Feature<?>> FEATURES;
    public static final IEntryHandler<ForgeWorldPreset> WORLD_TYPES;

    static {
        SCHEDULING_REGISTRY = _REGISTRATE.registry("scheduling", IScheduling.class)
            .onBake(SchedulingManager::onBake)
            .register();
        EVENT = _REGISTRATE.simpleRegistry("event", Event.class);

        COMPONENT_TYPES = REGISTRATE.<ComponentType<?>>genericRegistry("component_type", ComponentType.class)
            .onBake((registry, stage) -> ComponentType.onBake(registry))
            .register();
        FLUIDS = REGISTRATE.getHandler(ForgeRegistries.FLUIDS);
        FEATURES = REGISTRATE.getHandler(ForgeRegistries.FEATURES);
        WORLD_TYPES = REGISTRATE.getHandler(ForgeRegistries.Keys.WORLD_TYPES, ForgeWorldPreset.class);
    }

    public static void init() {}

    public static IEntry<SimpleFluid> simpleFluid(String id, ResourceLocation tex, int color) {
        return REGISTRATE.registryEntry(FLUIDS, id, () -> new SimpleFluid(tex, color));
    }

    public static IEntry<SimpleFluid> simpleFluid(String id, ResourceLocation tex) {
        return simpleFluid(id, tex, 0xFFFFFFFF);
    }
}
