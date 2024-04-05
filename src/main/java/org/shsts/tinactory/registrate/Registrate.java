package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.common.CapabilityProviderType;
import org.shsts.tinactory.core.common.Event;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.SmartEntityBlock;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.ComponentType;
import org.shsts.tinactory.core.recipe.NullRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntitySetBuilder;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;
import org.shsts.tinactory.registrate.builder.ItemBuilder;
import org.shsts.tinactory.registrate.builder.RecipeTypeBuilder;
import org.shsts.tinactory.registrate.builder.RegistryBuilderWrapper;
import org.shsts.tinactory.registrate.builder.RegistryEntryBuilder;
import org.shsts.tinactory.registrate.builder.SchedulingBuilder;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.CapabilityEntry;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.common.SmartRegistry;
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.handler.BlockStateHandler;
import org.shsts.tinactory.registrate.handler.CapabilityHandler;
import org.shsts.tinactory.registrate.handler.DataHandler;
import org.shsts.tinactory.registrate.handler.DynamicHandler;
import org.shsts.tinactory.registrate.handler.ItemModelHandler;
import org.shsts.tinactory.registrate.handler.LootTableHandler;
import org.shsts.tinactory.registrate.handler.MenuScreenHandler;
import org.shsts.tinactory.registrate.handler.RecipeDataHandler;
import org.shsts.tinactory.registrate.handler.RecipeTypeHandler;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.shsts.tinactory.registrate.handler.RegistryHandler;
import org.shsts.tinactory.registrate.handler.RenderTypeHandler;
import org.shsts.tinactory.registrate.handler.TagsHandler;
import org.shsts.tinactory.registrate.handler.TintHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Registrate {
    public final String modid;

    private final Map<ResourceLocation, RegistryEntryHandler<?>> registryEntryHandlers = new HashMap<>();
    private final List<DataHandler<?>> dataHandlers = new ArrayList<>();
    private final Map<ResourceKey<? extends Registry<?>>, TagsHandler<?>> tagsHandlers = new HashMap<>();

    // Registry
    public final RegistryHandler registryHandler = new RegistryHandler(this);

    // Registry Entries
    public final RegistryEntryHandler<Block> blockHandler = forgeHandler(ForgeRegistries.BLOCKS);
    public final RegistryEntryHandler<Item> itemHandler = forgeHandler(ForgeRegistries.ITEMS);
    public final RegistryEntryHandler<BlockEntityType<?>> blockEntityHandler =
            forgeHandler(ForgeRegistries.BLOCK_ENTITIES);
    public final RegistryEntryHandler<MenuType<?>> menuTypeHandler =
            forgeHandler(ForgeRegistries.CONTAINERS);
    public final RegistryEntryHandler<Fluid> fluidHandler = forgeHandler(ForgeRegistries.FLUIDS);

    // Dynamic
    public final DynamicHandler<Biome> biomeHandler =
            new DynamicHandler<>(Biome.class, OverworldBiomes::theVoid);

    // Others
    public final CapabilityHandler capabilityHandler = new CapabilityHandler(this);
    public final RecipeTypeHandler recipeTypeHandler = new RecipeTypeHandler(this);

    // ModelGen
    public final BlockStateHandler blockStateHandler = new BlockStateHandler(this);
    public final ItemModelHandler itemModelHandler = new ItemModelHandler(this);

    // DataGen
    public final RecipeDataHandler recipeDataHandler = new RecipeDataHandler(this);
    public final LootTableHandler lootTableHandler = new LootTableHandler(this);

    // Client
    public final RenderTypeHandler renderTypeHandler = new RenderTypeHandler();
    public final MenuScreenHandler menuScreenHandler = new MenuScreenHandler();
    public final TintHandler tintHandler = new TintHandler();

    @SuppressWarnings("deprecation")
    public Registrate(String modid) {
        this.modid = modid;
        tagsHandler(Registry.ITEM);
        tagsHandler(Registry.BLOCK);
        putDataHandler(blockStateHandler);
        putDataHandler(itemModelHandler);
        putDataHandler(recipeDataHandler);
        putDataHandler(lootTableHandler);
    }

    public <T extends IForgeRegistryEntry<T>>
    void putHandler(ResourceLocation loc, RegistryEntryHandler<T> handler) {
        registryEntryHandlers.put(loc, handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>>
    RegistryEntryHandler<T> forgeHandler(IForgeRegistry<T> registry) {
        return (RegistryEntryHandler<T>) registryEntryHandlers.computeIfAbsent(
                registry.getRegistryName(),
                loc -> RegistryEntryHandler.forge(registry));
    }

    private <T> void tagsHandler(Registry<T> registry) {
        var handler = new TagsHandler<>(this, registry);
        putDataHandler(handler);
        tagsHandlers.put(registry.key(), handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>> RegistryEntryHandler<T>
    forgeHandler(ResourceLocation loc, Class<T> entryClass, Supplier<IForgeRegistry<T>> registry) {
        return (RegistryEntryHandler<T>) registryEntryHandlers.computeIfAbsent(loc,
                $ -> RegistryEntryHandler.forge(entryClass, registry));
    }

    public <T extends IForgeRegistryEntry<T>> RegistryEntryHandler<T>
    forgeHandler(ResourceKey<Registry<T>> key, Class<T> entryClass, Supplier<IForgeRegistry<T>> registry) {
        return forgeHandler(key.location(), entryClass, registry);
    }

    private void putDataHandler(DataHandler<?> handler) {
        dataHandlers.add(handler);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        for (var handler : dataHandlers) {
            handler.clear();
        }
    }

    public void register(IEventBus modEventBus) {
        // mod BUS
        modEventBus.addListener(registryHandler::onNewRegistry);
        for (var handler : registryEntryHandlers.values()) {
            handler.addListener(modEventBus);
        }
        biomeHandler.addListener(modEventBus);
        for (var handler : dataHandlers) {
            modEventBus.addListener(handler::onGatherData);
        }
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(capabilityHandler::onRegisterEvent);
        modEventBus.addListener(tintHandler::onRegisterBlockColors);
        modEventBus.addListener(tintHandler::onRegisterItemColors);
        recipeTypeHandler.addListeners(modEventBus);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(renderTypeHandler::onClientSetup);
        event.enqueueWork(menuScreenHandler::onClientSetup);
    }

    public void registerClient(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    // builders

    private class SimpleBlockBuilder<U extends Block>
            extends BlockBuilder<U, Registrate, SimpleBlockBuilder<U>> {
        public SimpleBlockBuilder(String id, Function<BlockBehaviour.Properties, U> factory) {
            super(Registrate.this, id, Registrate.this, factory);
        }
    }

    public <U extends Block> BlockBuilder<U, Registrate, ?>
    block(String id, Function<BlockBehaviour.Properties, U> factory) {
        return new SimpleBlockBuilder<>(id, factory);
    }

    public <T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
    EntityBlockBuilder<T, U, Registrate>
    entityBlock(String id, EntityBlockBuilder.Factory<T, U> factory) {
        return entityBlock(this, id, factory);
    }

    public <T extends SmartBlockEntity, P, U extends SmartEntityBlock<T>>
    EntityBlockBuilder<T, U, P>
    entityBlock(P parent, String id, EntityBlockBuilder.Factory<T, U> factory) {
        return new EntityBlockBuilder<>(this, id, parent, factory);
    }

    private class SimpleItemBuilder<U extends Item>
            extends ItemBuilder<U, Registrate, SimpleItemBuilder<U>> {

        protected SimpleItemBuilder(String id, Function<Item.Properties, U> factory) {
            super(Registrate.this, id, Registrate.this, factory);
        }
    }

    public <U extends Item> ItemBuilder<U, Registrate, ?>
    item(String id, Function<Item.Properties, U> factory) {
        return new SimpleItemBuilder<>(id, factory);
    }

    public <U extends SmartBlockEntity> BlockEntityBuilder<U, Registrate>
    blockEntity(String id, BlockEntityBuilder.Factory<U> factory) {
        return new BlockEntityBuilder<>(this, id, this, factory);
    }

    public <U extends SmartBlockEntity, P> BlockEntityBuilder<U, P>
    blockEntity(P parent, String id, BlockEntityBuilder.Factory<U> factory) {
        return new BlockEntityBuilder<>(this, id, parent, factory);
    }

    private class SimpleBlockEntitySetBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
            extends BlockEntitySetBuilder<T, U, BlockEntitySet<T, U>, SimpleBlockEntitySetBuilder<T, U>> {
        private final String id;
        private final BlockEntityBuilder.Factory<T> blockEntityFactory;
        private final EntityBlockBuilder.Factory<T, U> blockFactory;

        private SimpleBlockEntitySetBuilder(String id,
                                            BlockEntityBuilder.Factory<T> blockEntityFactory,
                                            EntityBlockBuilder.Factory<T, U> blockFactory) {
            this.id = id;
            this.blockEntityFactory = blockEntityFactory;
            this.blockFactory = blockFactory;
        }

        @Override
        protected BlockEntityBuilder<T, SimpleBlockEntitySetBuilder<T, U>> createBlockEntityBuilder() {
            return Registrate.this.blockEntity(this, id, blockEntityFactory);
        }

        @Override
        protected EntityBlockBuilder<T, U, SimpleBlockEntitySetBuilder<T, U>> createBlockBuilder() {
            return Registrate.this.entityBlock(this, id, blockFactory);
        }

        @Override
        protected BlockEntitySet<T, U>
        createSet(RegistryEntry<SmartBlockEntityType<T>> blockEntity, RegistryEntry<U> block) {
            return new BlockEntitySet<>(blockEntity, block);
        }
    }

    public <T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
    BlockEntitySetBuilder<T, U, BlockEntitySet<T, U>, ?>
    blockEntitySet(String id, BlockEntityBuilder.Factory<T> blockEntityFactory,
                   EntityBlockBuilder.Factory<T, U> blockFactory) {
        return new SimpleBlockEntitySetBuilder<>(id, blockEntityFactory, blockFactory);
    }

    public void blockState(Consumer<DataContext<BlockStateProvider>> cons) {
        blockStateHandler.addCallback(prov -> cons.accept(new DataContext<>(modid, prov)));
    }

    public void itemModel(Consumer<DataContext<ItemModelProvider>> cons) {
        itemModelHandler.addCallback(prov -> cons.accept(new DataContext<>(modid, prov)));
    }

    @SuppressWarnings("unchecked")
    private <T> TagsHandler<T> getTagsHandler(ResourceKey<? extends Registry<T>> key) {
        assert tagsHandlers.containsKey(key);
        return (TagsHandler<T>) tagsHandlers.get(key);
    }

    @SafeVarargs
    public final <T> void tag(T object, TagKey<T>... tags) {
        Supplier<T> supplier = () -> object;
        tag(supplier, tags);
    }

    @SafeVarargs
    public final <T> void tag(Supplier<? extends T> object, TagKey<T>... tags) {
        assert tags.length > 0;
        getTagsHandler(tags[0].registry()).addTags(object, tags);
    }

    public <T> void tag(TagKey<T> object, TagKey<T> tag) {
        getTagsHandler(tag.registry()).addTag(object, tag);
    }

    public <T extends IForgeRegistryEntry<T>>
    RegistryBuilderWrapper<T, Registrate> registry(String id, Class<T> clazz) {
        return new RegistryBuilderWrapper<>(this, id, clazz, this);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>>
    RegistryBuilderWrapper<T, Registrate> genericRegistry(String id, Class<?> clazz) {
        return new RegistryBuilderWrapper<>(this, id, (Class<T>) clazz, this);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>>
    SmartRegistry<T> simpleRegistry(String id, Class<?> clazz) {
        return (new RegistryBuilderWrapper<>(this, id, (Class<T>) clazz, this)).register();
    }

    public <T extends IForgeRegistryEntry<T>, B extends RegistryEntryBuilder<T, ?, Registrate, B>>
    B registryEntry(String id, SmartRegistry<T> registry,
                    RegistryEntryBuilder.BuilderFactory<T, Registrate, B> builderFactory) {
        return builderFactory.create(this, registry.getHandler(), id, this);
    }

    private class SimpleRegistryEntryBuilder<T extends IForgeRegistryEntry<T>, U extends T>
            extends RegistryEntryBuilder<T, U, Registrate, SimpleRegistryEntryBuilder<T, U>> {
        private final Supplier<U> factory;

        public SimpleRegistryEntryBuilder(RegistryEntryHandler<T> handler, String id, Supplier<U> factory) {
            super(Registrate.this, handler, id, Registrate.this);
            this.factory = factory;
        }

        @Override
        public U createObject() {
            return factory.get();
        }
    }

    public <T extends IForgeRegistryEntry<T>, U extends T>
    RegistryEntry<U> registryEntry(String id, SmartRegistry<T> registry, Supplier<U> factory) {
        return (new SimpleRegistryEntryBuilder<>(registry.getHandler(), id, factory)).register();
    }

    public <T extends IForgeRegistryEntry<T>, U extends T>
    RegistryEntry<U> registryEntry(String id, RegistryEntryHandler<T> handler, Supplier<U> factory) {
        return (new SimpleRegistryEntryBuilder<>(handler, id, factory)).register();
    }

    public RegistryEntry<SimpleFluid> simpleFluid(String id, ResourceLocation stillTexture, int color) {
        return registryEntry(id, fluidHandler, () -> new SimpleFluid(stillTexture, color));
    }

    public RegistryEntry<SimpleFluid> simpleFluid(String id, ResourceLocation stillTexture) {
        return simpleFluid(id, stillTexture, 0xFFFFFFFF);
    }

    public <T> CapabilityEntry<T> capability(Class<T> clazz, CapabilityToken<T> token) {
        return capabilityHandler.register(clazz, token);
    }

    public <T extends BlockEntity> RegistryEntry<CapabilityProviderType<T, ?>>
    capabilityProvider(String id, Function<T, ? extends ICapabilityProvider> factory) {
        return registryEntry(id, AllRegistries.CAPABILITY_PROVIDER_TYPE_REGISTRY, () ->
                CapabilityProviderType.simple(factory));
    }

    public <T extends BlockEntity, B extends Function<T, ICapabilityProvider>>
    RegistryEntry<CapabilityProviderType<T, B>>
    capabilityProvider(String id, Supplier<B> builderFactory) {
        return registryEntry(id, AllRegistries.CAPABILITY_PROVIDER_TYPE_REGISTRY, () ->
                new CapabilityProviderType<>(builderFactory));
    }

    public SchedulingBuilder<Registrate> scheduling(String id) {
        return registryEntry(id, AllRegistries.SCHEDULING_REGISTRY, SchedulingBuilder<Registrate>::new);
    }

    public <T extends Component>
    RegistryEntry<ComponentType<T>> componentType(String id, Class<T> clazz, Component.Factory<T> factory) {
        return registryEntry(id, AllRegistries.COMPONENT_TYPE_REGISTRY,
                () -> new ComponentType<>(clazz, factory));
    }

    public <A> RegistryEntry<Event<A>> event(String id, Class<A> argClazz) {
        return registryEntry(id, AllRegistries.EVENT,
                () -> new Event<>(argClazz));
    }

    public <T extends SmartRecipe<?, T>, B, S extends SmartRecipeSerializer<T, B>>
    RecipeTypeBuilder<T, B, S, Registrate> recipeType(String id, SmartRecipeSerializer.Factory<T, B, S> serializer) {
        return new RecipeTypeBuilder<>(this, id, this, serializer);
    }

    public RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder>
    simpleProcessingRecipeType(String id) {
        return simpleProcessingRecipeType(id, $ -> $);
    }

    public RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder>
    simpleProcessingRecipeType(String id, Transformer<ProcessingRecipe.SimpleBuilder> builderTransformer) {
        return recipeType("processing/" + id, ProcessingRecipe.SIMPLE_SERIALIZER)
                .clazz(ProcessingRecipe.Simple.class)
                .builder(ProcessingRecipe.SimpleBuilder::new)
                .builderTransform(builderTransformer)
                .register();
    }

    public void biome(String... ids) {
        for (var id : ids) {
            biomeHandler.addLocation(new ResourceLocation(modid, id));
        }
    }

    public void nullRecipe(ResourceLocation loc) {
        recipeDataHandler.addCallback(prov -> prov.addRecipe(new NullRecipe(loc)));
    }

    public void nullRecipe(String loc) {
        nullRecipe(new ResourceLocation(loc));
    }

    public void nullRecipe(Item item) {
        var loc = item.getRegistryName();
        assert loc != null;
        nullRecipe(loc);
    }

    public void vanillaRecipe(Supplier<RecipeBuilder> recipe) {
        recipeDataHandler.addCallback(prov -> recipe.get().save(prov::addRecipe));
    }

    public void vanillaRecipe(Supplier<RecipeBuilder> recipe, ResourceLocation loc) {
        recipeDataHandler.addCallback(prov -> recipe.get().save(prov::addRecipe, loc));
    }
}
