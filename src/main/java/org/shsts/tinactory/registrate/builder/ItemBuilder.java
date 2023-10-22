package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.registrate.DistLazy;
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
public class ItemBuilder<U extends Item, P extends IItemParent, S extends ItemBuilder<U, P, S>>
        extends RegistryEntryBuilder<Item, U, P, S> {
    protected final Function<Item.Properties, U> factory;
    protected Transformer<Item.Properties> properties = $ -> $;
    @Nullable
    protected Consumer<RegistryDataContext<Item, U, ItemModelProvider>> modelCallback = null;
    @Nullable
    protected DistLazy<ItemColor> tint = null;

    public ItemBuilder(Registrate registrate, String id, P parent,
                       Function<Item.Properties, U> factory) {
        super(registrate, registrate.itemHandler, id, parent);
        this.factory = factory;
    }

    public S properties(Transformer<Item.Properties> trans) {
        this.properties = this.properties.chain(trans);
        return self();
    }

    public S model(Consumer<RegistryDataContext<Item, U, ItemModelProvider>> cons) {
        this.modelCallback = cons;
        return self();
    }

    public S tint(DistLazy<ItemColor> color) {
        this.tint = color;
        return self();
    }

    public S tint(int... colors) {
        return tint(() -> () -> ($, index) -> index < colors.length ? colors[index] : 0xFFFFFF);
    }

    public S tag(ResourceLocation... loc) {
        this.onCreateEntry.add(entry ->
                this.registrate.itemTagsHandler.addTags(entry, loc));
        return self();
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        if (this.modelCallback != null) {
            this.addDataCallback(this.registrate.itemModelHandler, this.modelCallback);
        }
        var tint = this.tint;
        if (this.tint != null) {
            this.onCreateObject.add(item -> tint.runOnDist(Dist.CLIENT, () -> itemColor ->
                    this.registrate.tintHandler.addItemColor(item, itemColor)));
        }
        return super.createEntry();
    }

    @Override
    public U createObject() {
        var defaultTab = this.parent.getDefaultCreativeModeTab();
        var properties = new Item.Properties();
        if (defaultTab != null) {
            properties = properties.tab(defaultTab);
        }
        properties = this.parent.getDefaultItemProperties().apply(properties);
        properties = this.properties.apply(properties);
        return this.factory.apply(properties);
    }
}
