package org.shsts.tinactory.datagen.builder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import org.shsts.tinactory.datagen.context.RegistryDataContext;
import org.shsts.tinycorelib.datagen.api.IDataGen;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.shsts.tinactory.datagen.DataGen._DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ItemDataBuilder<U extends Item, P> extends
    TrackedDataBuilder<Item, U, P, ItemDataBuilder<U, P>> {
    @Nullable
    private Consumer<RegistryDataContext<Item, U, ItemModelProvider>> model = null;

    public ItemDataBuilder(IDataGen dataGen, P parent, ResourceLocation loc, Supplier<U> item) {
        super(dataGen, parent, loc, _DATA_GEN.itemTrackedCtx, item);
    }

    public ItemDataBuilder<U, P> model(Consumer<RegistryDataContext<Item, U, ItemModelProvider>> cons) {
        this.model = cons;
        return this;
    }

    @SafeVarargs
    public final ItemDataBuilder<U, P> tag(TagKey<Item>... tags) {
        callbacks.add(() -> dataGen.tag(object, List.of(tags)));
        return this;
    }

    @Override
    protected void doRegister() {
        assert model != null;
        xDataGen.itemModelHandler.addModelCallback(loc, object, model);
    }
}
