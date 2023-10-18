package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.DistLazy;
import org.shsts.tinactory.registrate.IBlockParent;
import org.shsts.tinactory.registrate.IItemParent;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBuilder<U extends Block, P extends IBlockParent & IItemParent, S extends BlockBuilder<U, P, S>>
        extends RegistryEntryBuilder<Block, U, P, S> implements IItemParent {
    @Nullable
    protected Function<BlockBehaviour.Properties, U> factory = null;
    @Nullable
    protected Material material = null;
    protected Transformer<BlockBehaviour.Properties> properties = $ -> $;
    @Nullable
    protected DistLazy<RenderType> renderType = null;

    @Nullable
    protected Consumer<RegistryDataContext<Block, U, BlockStateProvider>> blockStateCallback = null;
    @Nullable
    protected Consumer<RegistryDataContext<Item, ? extends BlockItem, ItemModelProvider>> itemModelCallback = null;

    public BlockBuilder(Registrate registrate, String id, P parent,
                        Function<BlockBehaviour.Properties, U> factory) {
        super(registrate, registrate.blockHandler, id, parent);
        this.factory = factory;
    }

    protected BlockBuilder(Registrate registrate, String id, P parent) {
        super(registrate, registrate.blockHandler, id, parent);
    }

    public S material(Material material) {
        this.material = material;
        return self();
    }

    public S properties(Transformer<BlockBehaviour.Properties> trans) {
        this.properties = this.properties.chain(trans);
        return self();
    }

    public S renderType(DistLazy<RenderType> renderType) {
        this.renderType = renderType;
        return self();
    }

    public S translucent() {
        return this.renderType(() -> RenderType::translucent);
    }

    public S blockState(Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cons) {
        this.blockStateCallback = cons;
        return self();
    }

    public <U1 extends BlockItem> S itemModel(Consumer<RegistryDataContext<Item, U1, ItemModelProvider>> cons) {
        this.itemModelCallback = ctx -> cons.accept(ctx.cast());
        return self();
    }

    public <U1 extends BlockItem> void buildItemModels(ItemBuilder<U1, ?, ?> itemBuilder) {
        var callback = this.itemModelCallback;
        if (callback != null) {
            itemBuilder.model(callback::accept);
        }
        this.itemModelCallback = null;
    }

    private class SimpleBlockItemBuilder<U1 extends BlockItem>
            extends BlockItemBuilder<U1, S, SimpleBlockItemBuilder<U1>> {
        public SimpleBlockItemBuilder(Factory<U1> factory) {
            super(BlockBuilder.this.registrate, BlockBuilder.this.self(), factory);
        }
    }

    public <U1 extends BlockItem> BlockItemBuilder<U1, S, ?> blockItem(BlockItemBuilder.Factory<U1> factory) {
        return new SimpleBlockItemBuilder<>(factory);
    }

    public BlockItemBuilder<BlockItem, S, ?> blockItem() {
        return blockItem(BlockItem::new);
    }

    public S defaultBlockItem() {
        return this.blockItem()
                .defaultModel(ctx -> ctx.provider.withExistingParent(ctx.id,
                        new ResourceLocation(ctx.modid, "block/" + ctx.id)))
                .build();
    }

    @Override
    public RegistryEntry<U> register() {
        if (this.blockStateCallback != null) {
            this.addDataCallback(this.registrate.blockStateHandler, this.blockStateCallback);
        }
        this.blockStateCallback = null;
        return super.register();
    }

    protected U buildBlock(BlockBehaviour.Properties properties) {
        assert this.factory != null;
        return this.factory.apply(properties);
    }

    @Override
    public U buildObject() {
        var material = this.material == null ? this.parent.getDefaultMaterial() : this.material;
        var properties = this.properties.apply(
                this.parent.getDefaultBlockProperties().apply(BlockBehaviour.Properties.of(material)));
        var block = this.buildBlock(properties);
        if (this.renderType != null) {
            this.renderType.runOnDist(Dist.CLIENT, () -> renderType ->
                    this.registrate.renderTypeHandler.setRenderType(block, renderType));
        }
        return block;
    }

    @Nullable
    @Override
    public CreativeModeTab getDefaultCreativeModeTab() {
        return this.parent.getDefaultCreativeModeTab();
    }

    @Override
    public Transformer<Item.Properties> getDefaultItemProperties() {
        return this.parent.getDefaultItemProperties();
    }
}
