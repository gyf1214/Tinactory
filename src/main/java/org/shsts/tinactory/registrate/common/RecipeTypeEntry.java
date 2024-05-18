package org.shsts.tinactory.registrate.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.common.SmartRecipeSerializer;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeEntry<T extends SmartRecipe<?, T>, B> extends RegistryEntry<RecipeType<T>> {
    private final Registrate registrate;
    private final SmartRecipeBuilder.Factory<T, B> builderFactory;
    @Nullable
    private SmartRecipeSerializer<T, B> serializer;
    private final String prefix;
    private final Transformer<B> defaultTransformer;
    public final Class<T> clazz;

    public RecipeTypeEntry(Registrate registrate, String id, Supplier<RecipeType<T>> supplier,
                           SmartRecipeBuilder.Factory<T, B> builderFactory, String prefix, Class<T> clazz,
                           Transformer<B> defaultTransformer) {
        super(registrate.modid, id, supplier);
        this.registrate = registrate;
        this.builderFactory = builderFactory;
        this.prefix = prefix;
        this.clazz = clazz;
        this.defaultTransformer = defaultTransformer;
    }

    public SmartRecipeSerializer<T, B> getSerializer() {
        assert serializer != null;
        return serializer;
    }

    public void setSerializer(SmartRecipeSerializer<T, B> value) {
        serializer = value;
    }

    public B getBuilder(ResourceLocation loc) {
        return builderFactory.create(registrate, this, loc);
    }

    public B getBuilder(Registrate registrate, ResourceLocation loc) {
        return builderFactory.create(registrate, this, loc);
    }

    private B addRecipe(ResourceLocation loc) {
        return defaultTransformer.apply(getBuilder(loc));
    }

    private B addRecipe(Registrate registrate, ResourceLocation loc) {
        return defaultTransformer.apply(getBuilder(registrate, loc));
    }

    public B recipe(ResourceLocation loc) {
        return addRecipe(ModelGen.prepend(loc, prefix));
    }

    public B recipe(String id) {
        return recipe(new ResourceLocation(registrate.modid, id));
    }

    public B recipe(Item item) {
        var loc = item.getRegistryName();
        assert loc != null;
        return recipe(loc);
    }

    public B recipe(Registrate registrate, ResourceLocation loc) {
        return addRecipe(registrate, ModelGen.prepend(loc, prefix));
    }

    public B recipe(Registrate registrate, String id) {
        return recipe(registrate, new ResourceLocation(registrate.modid, id));
    }
}
