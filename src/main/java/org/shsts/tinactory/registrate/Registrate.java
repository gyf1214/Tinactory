package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartEntityBlock;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.builder.BlockBuilder;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.builder.EntityBlockBuilder;
import org.shsts.tinactory.registrate.builder.ItemBuilder;
import org.shsts.tinactory.registrate.context.DataContext;
import org.shsts.tinactory.registrate.handler.BlockStateHandler;
import org.shsts.tinactory.registrate.handler.ItemModelHandler;
import org.shsts.tinactory.registrate.handler.RegistryHandler;
import org.shsts.tinactory.registrate.handler.RenderTypeHandler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Registrate implements IBlockParent, IItemParent {
    public final String modid;
    public final RegistryHandler<Block> blockHandler = RegistryHandler.forge(this, ForgeRegistries.BLOCKS);
    public final RegistryHandler<Item> itemHandler = RegistryHandler.forge(this, ForgeRegistries.ITEMS);
    public final RegistryHandler<BlockEntityType<?>> blockEntityHandler =
            RegistryHandler.forge(this, ForgeRegistries.BLOCK_ENTITIES);
    public final BlockStateHandler blockStateHandler = new BlockStateHandler(this);
    public final ItemModelHandler itemModelHandler = new ItemModelHandler(this);
    public final RenderTypeHandler renderTypeHandler = new RenderTypeHandler();

    public Registrate(String modid) {
        this.modid = modid;
    }

    private void onGatherData(GatherDataEvent event) {
        this.blockStateHandler.onGatherData(event);
        this.itemModelHandler.onGatherData(event);
    }

    public void register(IEventBus modEventBus) {
        modEventBus.addGenericListener(Block.class, this.blockHandler::onRegisterEvent);
        modEventBus.addGenericListener(Item.class, this.itemHandler::onRegisterEvent);
        modEventBus.addGenericListener(BlockEntityType.class, this.blockEntityHandler::onRegisterEvent);
        modEventBus.addListener(this::onGatherData);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        this.renderTypeHandler.onClientSetup(event);
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
