package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.content.recipe.NullRecipe;
import org.shsts.tinactory.core.CapabilityProviderType;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;
import org.shsts.tinactory.registrate.builder.ItemBuilder;
import org.shsts.tinactory.registrate.builder.RecipeTypeBuilder;
import org.shsts.tinactory.registrate.builder.RegistryBuilderWrapper;
import org.shsts.tinactory.registrate.builder.RegistryEntryBuilder;
import org.shsts.tinactory.registrate.builder.SchedulingBuilder;
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.handler.BlockStateHandler;
import org.shsts.tinactory.registrate.handler.CapabilityHandler;
import org.shsts.tinactory.registrate.handler.DataHandler;
import org.shsts.tinactory.registrate.handler.ItemModelHandler;
import org.shsts.tinactory.registrate.handler.MenuScreenHandler;
import org.shsts.tinactory.registrate.handler.RecipeDataHandler;
import org.shsts.tinactory.registrate.handler.RecipeTypeHandler;
import org.shsts.tinactory.registrate.handler.RegistryEntryHandler;
import org.shsts.tinactory.registrate.handler.RegistryHandler;
import org.shsts.tinactory.registrate.handler.RenderTypeHandler;
import org.shsts.tinactory.registrate.handler.TagsHandler;
import org.shsts.tinactory.registrate.handler.TintHandler;

import javax.annotation.Nullable;
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
public class Registrate implements IBlockParent, IItemParent {
    public final String modid;

    private final Map<ResourceLocation, RegistryEntryHandler<?>> registryEntryHandlers = new HashMap<>();
    private final List<DataHandler<?>> dataHandlers = new ArrayList<>();

    // Registry
    public final RegistryHandler registryHandler = new RegistryHandler(this);

    // Registry Entries
    public final RegistryEntryHandler<Block> blockHandler = forgeHandler(ForgeRegistries.BLOCKS);
    public final RegistryEntryHandler<Item> itemHandler = forgeHandler(ForgeRegistries.ITEMS);
    public final RegistryEntryHandler<BlockEntityType<?>> blockEntityHandler =
            forgeHandler(ForgeRegistries.BLOCK_ENTITIES);
    public final RegistryEntryHandler<MenuType<?>> menuTypeHandler =
            forgeHandler(ForgeRegistries.CONTAINERS);

    // Others
    public final CapabilityHandler capabilityHandler = new CapabilityHandler(this);
    public final RecipeTypeHandler recipeTypeHandler = new RecipeTypeHandler(this);

    // ModelGen
    public final BlockStateHandler blockStateHandler = new BlockStateHandler(this);
    public final ItemModelHandler itemModelHandler = new ItemModelHandler(this);
    @SuppressWarnings("deprecation")
    public final TagsHandler<Item> itemTagsHandler = new TagsHandler<>(this, Registry.ITEM);

    // DataGen
    public final RecipeDataHandler recipeDataHandler = new RecipeDataHandler(this);

    // Client
    public final RenderTypeHandler renderTypeHandler = new RenderTypeHandler();
    public final MenuScreenHandler menuScreenHandler = new MenuScreenHandler();
    public final TintHandler tintHandler = new TintHandler();

    public Registrate(String modid) {
        this.modid = modid;
        this.putDataHandler(this.blockStateHandler);
        this.putDataHandler(this.itemModelHandler);
        this.putDataHandler(this.itemTagsHandler);
        this.putDataHandler(this.recipeDataHandler);
    }

    public <T extends IForgeRegistryEntry<T>>
    void putHandler(ResourceLocation loc, RegistryEntryHandler<T> handler) {
        this.registryEntryHandlers.put(loc, handler);
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>>
    RegistryEntryHandler<T> forgeHandler(IForgeRegistry<T> registry) {
        return (RegistryEntryHandler<T>) this.registryEntryHandlers.computeIfAbsent(
                registry.getRegistryName(),
                loc -> RegistryEntryHandler.forge(registry));
    }

    @SuppressWarnings("unchecked")
    public <T extends IForgeRegistryEntry<T>> RegistryEntryHandler<T>
    forgeHandler(ResourceLocation loc, Class<T> entryClass, Supplier<IForgeRegistry<T>> registry) {
        return (RegistryEntryHandler<T>) this.registryEntryHandlers.computeIfAbsent(loc,
                loc1 -> RegistryEntryHandler.forge(loc1, entryClass, registry));
    }

    public <T extends IForgeRegistryEntry<T>> RegistryEntryHandler<T>
    forgeHandler(ResourceKey<Registry<T>> key, Class<T> entryClass, Supplier<IForgeRegistry<T>> registry) {
        return this.forgeHandler(key.location(), entryClass, registry);
    }

    public void putDataHandler(DataHandler<?> handler) {
        this.dataHandlers.add(handler);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        for (var handler : this.dataHandlers) {
            handler.clear();
        }
    }

    public void register(IEventBus modEventBus) {
        // mod BUS
        modEventBus.addListener(this.registryHandler::onNewRegistry);
        for (var handler : this.registryEntryHandlers.values()) {
            handler.addListener(modEventBus);
        }
        for (var handler : this.dataHandlers) {
            modEventBus.addListener(handler::onGatherData);
        }
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this.capabilityHandler::onRegisterEvent);
        modEventBus.addListener(this.tintHandler::onRegisterBlockColors);
        modEventBus.addListener(this.tintHandler::onRegisterItemColors);
        this.recipeTypeHandler.addListeners(modEventBus);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(this.renderTypeHandler::onClientSetup);
        event.enqueueWork(this.menuScreenHandler::onClientSetup);
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

    private class SimpleEntityBlockBuilder<T extends SmartBlockEntity, U extends SmartEntityBlock<T>>
            extends EntityBlockBuilder<T, U, Registrate, SimpleEntityBlockBuilder<T, U>> {
        public SimpleEntityBlockBuilder(String id, Factory<T, U> factory) {
            super(Registrate.this, id, Registrate.this, factory);
        }
    }

    public <T extends SmartBlockEntity, U extends SmartEntityBlock<T>> EntityBlockBuilder<T, U, Registrate, ?>
    entityBlock(String id, EntityBlockBuilder.Factory<T, U> factory) {
        return new SimpleEntityBlockBuilder<>(id, factory);
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

    private class SimpleBlockEntityBuilder<U extends SmartBlockEntity>
            extends BlockEntityBuilder<U, Registrate, SimpleBlockEntityBuilder<U>> {

        public SimpleBlockEntityBuilder(String id, Factory<U> factory) {
            super(Registrate.this, id, Registrate.this, factory);
        }
    }

    public <U extends SmartBlockEntity> BlockEntityBuilder<U, Registrate, ?>
    blockEntity(String id, BlockEntityBuilder.Factory<U> factory) {
        return new SimpleBlockEntityBuilder<>(id, factory);
    }

    public void blockState(Consumer<DataContext<BlockStateProvider>> cons) {
        this.blockStateHandler.addCallback(prov -> cons.accept(new DataContext<>(this.modid, prov)));
    }

    public void itemModel(Consumer<DataContext<ItemModelProvider>> cons) {
        this.itemModelHandler.addCallback(prov -> cons.accept(new DataContext<>(this.modid, prov)));
    }

    public <T extends IForgeRegistryEntry<T>>
    RegistryBuilderWrapper<T, Registrate> registry(String id, Class<T> clazz) {
        return new RegistryBuilderWrapper<>(this, id, clazz, this);
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
            return this.factory.get();
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

    public <T> RegistryEntry<Capability<T>> capability(Class<T> clazz, CapabilityToken<T> token) {
        return this.capabilityHandler.register(clazz, token);
    }

    public <T extends BlockEntity> RegistryEntry<CapabilityProviderType<T, ?>>
    capabilityProvider(String id, Function<T, ? extends ICapabilityProvider> factory) {
        return this.registryEntry(id, AllRegistries.CAPABILITY_PROVIDER_TYPE_REGISTRY, () ->
                CapabilityProviderType.simple(factory));
    }

    public <T extends BlockEntity, B extends Function<T, ICapabilityProvider>>
    RegistryEntry<CapabilityProviderType<T, B>>
    capabilityProvider(String id, Supplier<B> builderFactory) {
        return this.registryEntry(id, AllRegistries.CAPABILITY_PROVIDER_TYPE_REGISTRY, () ->
                new CapabilityProviderType<>(builderFactory));
    }

    public SchedulingBuilder<Registrate> scheduling(String id) {
        return this.registryEntry(id, AllRegistries.SCHEDULING_REGISTRY, SchedulingBuilder<Registrate>::new);
    }

    public <T extends SmartRecipe<?, T>, B, S extends SmartRecipeSerializer<T, B>>
    RecipeTypeBuilder<T, B, S, Registrate> recipeType(String id, SmartRecipeSerializer.Factory<T, B, S> serializer) {
        return new RecipeTypeBuilder<>(this, id, this, serializer);
    }

    public void nullRecipe(ResourceLocation loc) {
        this.recipeDataHandler.addCallback(prov -> prov.addRecipe(new NullRecipe(loc)));
    }

    public void nullRecipe(String loc) {
        this.nullRecipe(new ResourceLocation(loc));
    }

    public void nullRecipe(ItemLike item) {
        var loc = item.asItem().getRegistryName();
        assert loc != null;
        this.nullRecipe(loc);
    }

    public void vanillaRecipe(Supplier<RecipeBuilder> recipe) {
        this.recipeDataHandler.addCallback(prov -> recipe.get().save(prov::addRecipe));
    }

    public void vanillaRecipe(Supplier<RecipeBuilder> recipe, ResourceLocation loc) {
        this.recipeDataHandler.addCallback(prov -> recipe.get().save(prov::addRecipe, loc));
    }

    // defaults

    private Material defaultMaterial = Material.STONE;
    private Transformer<BlockBehaviour.Properties> defaultBlockProperties = $ -> $;

    public void defaultBlockProperties(Material material) {
        this.defaultMaterial = material;
    }

    public void defaultBlockProperties(Transformer<BlockBehaviour.Properties> trans) {
        this.defaultBlockProperties = this.defaultBlockProperties.chain(trans);
    }

    public void resetDefaultBlockProperties(Transformer<BlockBehaviour.Properties> trans) {
        this.defaultBlockProperties = trans;
    }

    public void resetDefaultBlockProperties() {
        this.defaultBlockProperties = $ -> $;
    }

    @Override
    public Material getDefaultMaterial() {
        return defaultMaterial;
    }

    @Override
    public Transformer<BlockBehaviour.Properties> getDefaultBlockProperties() {
        return defaultBlockProperties;
    }

    @Nullable
    private CreativeModeTab defaultCreativeModeTab = null;
    private Transformer<Item.Properties> defaultItemProperties = $ -> $;

    public void defaultItemProperties(Transformer<Item.Properties> trans) {
        this.defaultItemProperties = this.defaultItemProperties.chain(trans);
    }

    public void resetDefaultItemProperties(Transformer<Item.Properties> trans) {
        this.defaultItemProperties = trans;
    }

    public void resetDefaultItemProperties() {
        this.defaultItemProperties = $ -> $;
    }

    public void creativeModeTab(CreativeModeTab tab) {
        this.defaultCreativeModeTab = tab;
    }

    @Nullable
    @Override
    public CreativeModeTab getDefaultCreativeModeTab() {
        return this.defaultCreativeModeTab;
    }

    @Override
    public Transformer<Item.Properties> getDefaultItemProperties() {
        return this.defaultItemProperties;
    }
}
