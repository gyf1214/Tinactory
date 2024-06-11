package org.shsts.tinactory.datagen.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.datagen.DataGen;
import org.shsts.tinactory.datagen.context.RegistryDataContext;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemDataBuilder<U extends Item, P> extends
        TrackedDataBuilder<Item, U, P, ItemDataBuilder<U, P>> {
    @Nullable
    private Consumer<RegistryDataContext<Item, U, ItemModelProvider>> model = null;

    public ItemDataBuilder(DataGen dataGen, P parent, ResourceLocation loc, Supplier<U> item) {
        super(dataGen, parent, loc, dataGen.itemTrackedCtx, item);
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
