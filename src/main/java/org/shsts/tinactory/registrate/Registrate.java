package org.shsts.tinactory.registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.XBuilderBase;
import org.shsts.tinactory.registrate.builder.RecipeTypeBuilder;
import org.shsts.tinactory.registrate.builder.RegistryBuilderWrapper;
import org.shsts.tinactory.registrate.builder.RegistryEntryBuilder;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.common.SmartRegistry;
import org.shsts.tinactory.registrate.handler.RecipeTypeHandler;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.shsts.tinactory.registrate.handler.RegistryHandler;
import org.shsts.tinactory.registrate.handler.RenderTypeHandler;
import org.shsts.tinactory.registrate.handler.RendererHandler;
import org.shsts.tinactory.registrate.handler.TintHandler;
import org.shsts.tinactory.registrate.tracking.TrackedObjects;
import org.shsts.tinactory.registrate.tracking.TrackedType;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Registrate {
    public final String modid;

    private final Map<ResourceLocation, RegistryEntryHandler<?>> registryEntryHandlers = new HashMap<>();

    // Registry
    public final RegistryHandler registryHandler;

    // Registry Entries
    public final RegistryEntryHandler<Block> blockHandler;
    public final RegistryEntryHandler<Item> itemHandler;
    public final RegistryEntryHandler<BlockEntityType<?>> blockEntityHandler;

    // Others
    public final RecipeTypeHandler recipeTypeHandler;

    // Client
    public final RenderTypeHandler renderTypeHandler;
    public final RendererHandler rendererHandler;
    public final TintHandler tintHandler;

    private final TrackedObjects trackedObjects;

    public Registrate(String modid) {
        this.modid = modid;

        this.registryHandler = new RegistryHandler(this);
        this.blockHandler = forgeHandler(ForgeRegistries.BLOCKS);
        this.itemHandler = forgeHandler(ForgeRegistries.ITEMS);
        this.blockEntityHandler = forgeHandler(ForgeRegistries.BLOCK_ENTITIES);

        this.recipeTypeHandler = new RecipeTypeHandler(this);

        this.renderTypeHandler = new RenderTypeHandler();
        this.rendererHandler = new RendererHandler();
        this.tintHandler = new TintHandler();

        this.trackedObjects = new TrackedObjects();
    }

    public <T extends IForgeRegistryEntry<T>> void putHandler(ResourceLocation loc,
        RegistryEntryHandler<T> handler) {
        registryEntryHandlers.put(loc, handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>> RegistryEntryHandler<T> forgeHandler(IForgeRegistry<T> registry) {
        return (RegistryEntryHandler<T>) registryEntryHandlers.computeIfAbsent(
            registry.getRegistryName(),
            loc -> RegistryEntryHandler.forge(registry));
    }

    public void register(IEventBus modEventBus) {
        // mod BUS
        modEventBus.addListener(registryHandler::onNewRegistry);
        for (var handler : registryEntryHandlers.values()) {
            handler.addListener(modEventBus);
        }
        recipeTypeHandler.addListeners(modEventBus);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(renderTypeHandler::onClientSetup);
    }

    public void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(tintHandler::onRegisterBlockColors);
        modEventBus.addListener(tintHandler::onRegisterItemColors);
        modEventBus.addListener(rendererHandler::onRegisterRenderers);
        modEventBus.addListener(this::onClientSetup);
    }

    // builders

    public <T extends IForgeRegistryEntry<T>> RegistryBuilderWrapper<T, Registrate> registry(
        String id, Class<T> clazz) {
        return new RegistryBuilderWrapper<>(this, id, clazz, this);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>> SmartRegistry<T> simpleRegistry(String id, Class<?> clazz) {
        return (new RegistryBuilderWrapper<>(this, id, (Class<T>) clazz, this)).register();
    }

    private class SimpleRegistryEntryBuilder<T extends IForgeRegistryEntry<T>, U extends T>
        extends RegistryEntryBuilder<T, U, Registrate, SimpleRegistryEntryBuilder<T, U>> {
        private final Supplier<U> factory;

        public SimpleRegistryEntryBuilder(RegistryEntryHandler<T> handler, String id, Supplier<U> factory) {
            super(Registrate.this, handler, id, Registrate.this);
            this.factory = factory;
        }

        @Override
        protected U createObject() {
            return factory.get();
        }
    }

    public <T extends IForgeRegistryEntry<T>, U extends T> RegistryEntry<U> registryEntry(
        String id, SmartRegistry<T> registry, Supplier<U> factory) {
        return (new SimpleRegistryEntryBuilder<>(registry.getHandler(), id, factory)).register();
    }

    public <A> RegistryEntry<Event<A>> event(String id) {
        return registryEntry(id, AllRegistries.EVENT, Event::new);
    }

    public <A, R> RegistryEntry<ReturnEvent<A, R>> returnEvent(String id, R defaultRet) {
        return registryEntry(id, AllRegistries.EVENT, () -> new ReturnEvent<>(defaultRet));
    }

    public <T extends SmartRecipe<?>,
        B extends XBuilderBase<?, ?, B>> RecipeTypeBuilder<T, B, Registrate> recipeType(
        String id, SmartRecipeSerializer.Factory<T, B> serializer) {
        return new RecipeTypeBuilder<>(this, id, this, serializer);
    }

    public void trackTranslation(String key) {
        trackedObjects.put(TrackedType.LANG, key, key);
    }

    public void trackBlock(Block block) {
        var loc = block.getRegistryName();
        assert loc != null;
        trackedObjects.put(TrackedType.BLOCK, block, loc.toString());
        trackTranslation(block.getDescriptionId());
    }

    public <V> Map<V, String> getTracked(TrackedType<V> type) {
        return trackedObjects.getObjects(type);
    }
}
