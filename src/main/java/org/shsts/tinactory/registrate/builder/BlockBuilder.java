package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.DistLazy;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.LocHelper.prepend;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockBuilder<U extends Block, P, S extends BlockBuilder<U, P, S>>
        extends RegistryEntryBuilder<Block, U, P, S> {
    @Nullable
    protected Function<BlockBehaviour.Properties, U> factory = null;
    protected Material material = Material.STONE;
    protected Transformer<BlockBehaviour.Properties> properties = $ -> $;

    @Nullable
    protected BlockItemBuilder<?> blockItemBuilder = null;
    @Nullable
    protected Consumer<RegistryDataContext<Block, U, BlockStateProvider>> blockStateCallback = null;
    @Nullable
    protected DistLazy<BlockColor> tint = null;

    public BlockBuilder(Registrate registrate, String id, P parent,
                        Function<BlockBehaviour.Properties, U> factory) {
        super(registrate, registrate.blockHandler, id, parent);
        this.factory = factory;
        onCreateObject.add(registrate::trackTranslation);
    }

    protected BlockBuilder(Registrate registrate, String id, P parent) {
        super(registrate, registrate.blockHandler, id, parent);
        onCreateObject.add(registrate::trackTranslation);
    }

    public S material(Material value) {
        material = value;
        return self();
    }

    public S properties(Transformer<BlockBehaviour.Properties> trans) {
        properties = properties.chain(trans);
        return self();
    }

    public S renderType(DistLazy<RenderType> renderType) {
        onCreateObject.add(block -> renderType.runOnDist(Dist.CLIENT, () -> type ->
                registrate.renderTypeHandler.setRenderType(block, type)));
        return self();
    }

    public S translucent() {
        return renderType(() -> RenderType::translucent);
    }

    public S tint(DistLazy<BlockColor> value) {
        tint = value;
        return self();
    }

    public S tint(int... colors) {
        return tint(() -> () -> ($1, $2, $3, index) -> index < colors.length ? colors[index] : 0xFFFFFFFF);
    }

    @SafeVarargs
    public final S tag(TagKey<Block>... tags) {
        onCreateObject.add(entry -> registrate.tag(entry, tags));
        return self();
    }

    @Nullable
    private DistLazy<ItemColor> getItemTint() {
        var tint = this.tint;
        return tint == null ? null : () -> {
            var itemColor = tint.getValue();
            return () -> (itemStack, index) -> {
                var item = (BlockItem) itemStack.getItem();
                return itemColor.getColor(item.getBlock().defaultBlockState(), null, null, index);
            };
        };
    }

    public S blockState(Consumer<RegistryDataContext<Block, U, BlockStateProvider>> cons) {
        blockStateCallback = cons;
        return self();
    }

    public class BlockItemBuilder<U1 extends BlockItem> extends ItemBuilder<U1, S, BlockItemBuilder<U1>> {
        @FunctionalInterface
        public interface Factory<U2 extends BlockItem> {
            U2 create(Block block, Item.Properties properties);
        }

        public BlockItemBuilder(Factory<U1> factory) {
            super(BlockBuilder.this.registrate, BlockBuilder.this.id, BlockBuilder.this.self(), properties -> {
                var entry = BlockBuilder.this.entry;
                assert entry != null;
                return factory.create(entry.get(), properties);
            });
            parent.onCreateEntry.add($ -> register());
        }

        @Override
        protected RegistryEntry<U1> createEntry() {
            if (modelCallback == null) {
                modelCallback = ctx -> ctx.provider.withExistingParent(ctx.id,
                        new ResourceLocation(ctx.modid, "block/" + ctx.id));
            }
            if (tint == null) {
                tint = getItemTint();
            }
            return super.createEntry();
        }
    }

    public <U1 extends BlockItem> BlockItemBuilder<U1>
    blockItem(BlockItemBuilder.Factory<U1> factory) {
        assert blockItemBuilder == null;
        var builder = new BlockItemBuilder<>(factory);
        blockItemBuilder = builder;
        return builder;
    }

    public BlockItemBuilder<? extends BlockItem> blockItem() {
        return blockItemBuilder != null ? blockItemBuilder : blockItem(BlockItem::new);
    }

    public S defaultBlockItem(Consumer<RegistryDataContext<Item, ? extends BlockItem, ItemModelProvider>> cons) {
        return blockItem().model(cons::accept).build();
    }

    public S defaultBlockItem() {
        return blockItem().build();
    }

    public S drop(Supplier<? extends Item> item) {
        var loc = prepend(this.loc, "blocks");
        registrate.lootTableHandler.blockLoot(loot -> loot.dropSingle(loc, item.get()));
        return self();
    }

    public S dropSelf() {
        var loc = prepend(this.loc, "blocks");
        onCreateEntry.add(block ->
                registrate.lootTableHandler.blockLoot(loot -> loot.dropSingle(loc, block.get())));
        return self();
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        if (blockStateCallback != null) {
            addDataCallback(registrate.blockStateHandler, blockStateCallback);
        }
        var tint = this.tint;
        if (tint != null) {
            onCreateObject.add(block -> tint.runOnDist(Dist.CLIENT, () -> blockColor ->
                    registrate.tintHandler.addBlockColor(block, blockColor)));
        }
        return super.createEntry();
    }

    @Override
    protected U createObject() {
        assert factory != null;
        return factory.apply(properties.apply(BlockBehaviour.Properties.of(material)));
    }
}
