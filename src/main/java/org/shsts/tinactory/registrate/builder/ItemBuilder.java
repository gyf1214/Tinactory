package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.DistLazy;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemBuilder<U extends Item, P, S extends ItemBuilder<U, P, S>>
        extends RegistryEntryBuilder<Item, U, P, S> {

    protected final Function<Item.Properties, U> factory;
    protected Transformer<Item.Properties> properties = $ -> $.tab(CreativeModeTab.TAB_MISC);
    @Nullable
    protected DistLazy<ItemColor> tint = null;

    public ItemBuilder(Registrate registrate, String id, P parent,
                       Function<Item.Properties, U> factory) {
        super(registrate, registrate.itemHandler, id, parent);
        this.factory = factory;
    }

    public S properties(Transformer<Item.Properties> trans) {
        properties = properties.chain(trans);
        return self();
    }

    public S tint(DistLazy<ItemColor> color) {
        tint = color;
        return self();
    }

    public S tint(int... colors) {
        return tint(() -> () -> ($, index) -> index < colors.length ? colors[index] : 0xFFFFFFFF);
    }

    @Override
    protected RegistryEntry<U> createEntry() {
        var tint = this.tint;
        if (tint != null) {
            onCreateObject.add(item -> tint.runOnDist(Dist.CLIENT, () -> itemColor ->
                    registrate.tintHandler.addItemColor(item, itemColor)));
        }
        return super.createEntry();
    }

    @Override
    protected U createObject() {
        return factory.apply(properties.apply(new Item.Properties()));
    }
}
