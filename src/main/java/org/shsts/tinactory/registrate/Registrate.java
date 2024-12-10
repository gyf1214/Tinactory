package org.shsts.tinactory.registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.XBuilderBase;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntityTypeBuilder;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;
import org.shsts.tinactory.registrate.builder.ItemBuilder;
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
import java.util.function.Function;
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

    private class SimpleBlockBuilder<U extends Block>
        extends BlockBuilder<U, Registrate, SimpleBlockBuilder<U>> {
        public SimpleBlockBuilder(String id, Function<BlockBehaviour.Properties, U> factory) {
            super(Registrate.this, id, Registrate.this, factory);
        }
    }

    public <U extends Block> BlockBuilder<U, Registrate, ?> block(String id,
        Function<BlockBehaviour.Properties, U> factory) {
        return new SimpleBlockBuilder<>(id, factory);
    }

    public <T extends SmartBlockEntity, P, U extends SmartEntityBlock<T>> EntityBlockBuilder<T, U, P> entityBlock(
        P parent, String id, EntityBlockBuilder.Factory<T, U> factory) {
        return new EntityBlockBuilder<>(this, id, parent, factory);
    }

    private class SimpleItemBuilder<U extends Item>
        extends ItemBuilder<U, Registrate, SimpleItemBuilder<U>> {

        protected SimpleItemBuilder(String id, Function<Item.Properties, U> factory) {
            super(Registrate.this, id, Registrate.this, factory);
            onCreateObject.add(registrate::trackItem);
        }
    }

    public <U extends Item> ItemBuilder<U, Registrate, ?> item(String id,
        Function<Item.Properties, U> factory) {
        return new SimpleItemBuilder<>(id, factory);
    }

    public <U extends SmartBlockEntity, P> BlockEntityTypeBuilder<U, P> blockEntityType(P parent, String id,
        BlockEntityTypeBuilder.Factory<U> factory) {
        return new BlockEntityTypeBuilder<>(this, id, parent, factory);
    }

    private class SimpleBlockEntityBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
        extends BlockEntityBuilder<T, U, Registrate> {
        private final String id;
        private final BlockEntityTypeBuilder.Factory<T> blockEntityFactory;
        private final EntityBlockBuilder.Factory<T, U> blockFactory;

        private SimpleBlockEntityBuilder(String id,
            BlockEntityTypeBuilder.Factory<T> blockEntityFactory,
            EntityBlockBuilder.Factory<T, U> blockFactory) {
            super(Registrate.this);
            this.id = id;
            this.blockEntityFactory = blockEntityFactory;
            this.blockFactory = blockFactory;
        }

        @Override
        protected BlockEntityTypeBuilder<T, BlockEntityBuilder<T, U, Registrate>> createBlockEntityBuilder() {
            return Registrate.this.blockEntityType(this, id, blockEntityFactory);
        }

        @Override
        protected EntityBlockBuilder<T, U, BlockEntityBuilder<T, U, Registrate>> createBlockBuilder() {
            return Registrate.this.entityBlock(this, id, blockFactory);
        }
    }

    public <T extends SmartBlockEntity,
        U extends SmartEntityBlock<T>> BlockEntityBuilder<T, U, Registrate> blockEntity(
        String id, BlockEntityTypeBuilder.Factory<T> blockEntityFactory,
        EntityBlockBuilder.Factory<T, U> blockFactory) {
        return new SimpleBlockEntityBuilder<>(id, blockEntityFactory, blockFactory);
    }

    public <U extends SmartEntityBlock<
        SmartBlockEntity>> BlockEntityBuilder<SmartBlockEntity, U, Registrate> blockEntity(
        String id, EntityBlockBuilder.Factory<SmartBlockEntity, U> blockFactory) {
        return (new SimpleBlockEntityBuilder<>(id, SmartBlockEntity::new, blockFactory))
            .entityClass(SmartBlockEntity.class);
    }

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

    public void trackItem(Item item) {
        var loc = item.getRegistryName();
        assert loc != null;
        trackedObjects.put(TrackedType.ITEM, item, loc.toString());
        trackTranslation(item.getDescriptionId());
    }

    public <V> Map<V, String> getTracked(TrackedType<V> type) {
        return trackedObjects.getObjects(type);
    }
}
