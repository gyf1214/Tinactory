package org.shsts.tinactory.datagen.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.registrate.common.RegistryEntry;
import org.shsts.tinactory.registrate.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemDataBuilder<U extends Item, P> extends TrackedDataBuilder<Item, U, P, ItemDataBuilder<U, P>> {
    @Nullable
    private Consumer<RegistryDataContext<Item, U, ItemModelProvider>> model = null;

    public ItemDataBuilder(DataGen dataGen, P parent, RegistryEntry<U> entry) {
        super(dataGen, parent, dataGen.itemTrackedCtx, entry);
    }

    public ItemDataBuilder<U, P> model(Consumer<RegistryDataContext<Item, U, ItemModelProvider>> cons) {
        this.model = cons;
        return this;
    }

    @SafeVarargs
    public final ItemDataBuilder<U, P> tag(TagKey<Item>... tags) {
        callbacks.add(() -> dataGen.tag(object, tags));
        return this;
    }

    @Override
    protected void doRegister() {
        assert model != null;
        dataGen.itemModelHandler.addModelCallback(loc, object, model);
    }
}
