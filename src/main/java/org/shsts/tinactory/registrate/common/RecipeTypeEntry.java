package org.shsts.tinactory.registrate.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

import static org.shsts.tinactory.content.model.ModelGen.prepend;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeEntry<T extends Recipe<?>, B> extends RegistryEntry<RecipeType<T>> {
    private final Registrate registrate;
    private final SmartRecipeBuilder.Factory<T, B> builderFactory;
    @Nullable
    private RecipeSerializer<T> serializer;
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

    public RecipeSerializer<T> getSerializer() {
        assert serializer != null;
        return serializer;
    }

    public void setSerializer(RecipeSerializer<T> value) {
        serializer = value;
    }

    public B getBuilder(ResourceLocation loc) {
        return builderFactory.create(registrate, this, loc);
    }

    public B getBuilder(Registrate registrate, ResourceLocation loc) {
        return builderFactory.create(registrate, this, loc);
    }

    public B recipe(ResourceLocation loc) {
        return defaultTransformer.apply(getBuilder(loc));
    }

    public B recipe(Registrate registrate, ResourceLocation loc) {
        return defaultTransformer.apply(getBuilder(registrate, loc));
    }

    public B modRecipe(Item item) {
        var loc = item.getRegistryName();
        assert loc != null;
        return modRecipe(loc);
    }

    public B modRecipe(ResourceLocation loc) {
        var id = loc.getNamespace() + "/" + loc.getPath();
        return recipe(prepend(new ResourceLocation(modid, id), prefix));
    }
}
