package org.shsts.tinactory.registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.core.common.SimpleFluid;
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
    public static final IRegistry<ComponentType<?>> COMPONENT_TYPES;

    public static final IEntryHandler<IEvent<?>> EVENTS;
    public static final IEntryHandler<Block> BLOCKS;
    public static final IEntryHandler<Item> ITEMS;
    public static final IEntryHandler<Fluid> FLUIDS;
    public static final IEntryHandler<Feature<?>> FEATURES;
    public static final IEntryHandler<ForgeWorldPreset> WORLD_TYPES;

    static {
        SCHEDULINGS = REGISTRATE.registry("scheduling", IScheduling.class)
            .onBake(SchedulingManager::onBake)
            .register();
        COMPONENT_TYPES = REGISTRATE.<ComponentType<?>>genericRegistry("component_type", ComponentType.class)
            .onBake((registry, stage) -> ComponentType.onBake(registry))
            .register();

        EVENTS = REGISTRATE.getHandler(EVENT_REGISTRY_KEY, IEvent.class);
        BLOCKS = REGISTRATE.getHandler(ForgeRegistries.BLOCKS);
        ITEMS = REGISTRATE.getHandler(ForgeRegistries.ITEMS);
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
