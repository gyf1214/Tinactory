package org.shsts.tinactory.registrate.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.Transformer1;
import org.shsts.tinactory.core.common.XBuilderBase;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.core.recipe.SmartRecipeBuilder;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeEntry<T extends SmartRecipe<?>, B extends XBuilderBase<?, ?, B>>
    extends RegistryEntry<RecipeType<T>> {
    private final SmartRecipeBuilder.Factory<T, B> builderFactory;
    @Nullable
    private SmartRecipeSerializer<T, B> serializer;
    private final String prefix;
    private final Transformer1<B> defaults;
    public final Class<T> clazz;

    public RecipeTypeEntry(Registrate registrate, String id, Supplier<RecipeType<T>> supplier,
        SmartRecipeBuilder.Factory<T, B> builderFactory, String prefix, Class<T> clazz,
        Transformer1<B> defaults) {
        super(registrate.modid, id, supplier);
        this.builderFactory = builderFactory;
        this.prefix = prefix;
        this.clazz = clazz;
        this.defaults = defaults;
    }

    public SmartRecipeSerializer<T, B> getSerializer() {
        assert serializer != null;
        return serializer;
    }

    public void setSerializer(SmartRecipeSerializer<T, B> value) {
        serializer = value;
    }

    public B getBuilder(ResourceLocation loc) {
        return builderFactory.create(IRecipeDataConsumer.EMPTY, this, loc);
    }

    private B getBuilder(IRecipeDataConsumer consumer, ResourceLocation loc) {
        return builderFactory.create(consumer, this, loc);
    }

    private B addRecipe(IRecipeDataConsumer consumer, ResourceLocation loc) {
        return getBuilder(consumer, loc).transform(defaults);
    }

    public B recipe(IRecipeDataConsumer consumer, ResourceLocation loc) {
        var modid = consumer.getModId();
        var id = loc.getPath();
        if (!modid.equals(loc.getNamespace())) {
            id = loc.getNamespace() + "/" + id;
        }
        id = prefix + "/" + id;
        return addRecipe(consumer, new ResourceLocation(modid, id));
    }

    public B recipe(IRecipeDataConsumer consumer, String id) {
        return recipe(consumer, new ResourceLocation(consumer.getModId(), id));
    }

    public B recipe(IRecipeDataConsumer consumer, IForgeRegistryEntry<?> item) {
        var loc = item.getRegistryName();
        assert loc != null;
        return recipe(consumer, loc);
    }

    public B recipe(IRecipeDataConsumer consumer, RegistryEntry<?> item) {
        return recipe(consumer, item.loc);
    }

    public B recipe(IRecipeDataConsumer consumer, IEntry<?> item) {
        return recipe(consumer, item.loc());
    }
}