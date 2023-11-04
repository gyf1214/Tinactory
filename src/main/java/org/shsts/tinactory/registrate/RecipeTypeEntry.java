package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

import static org.shsts.tinactory.model.ModelGen.prepend;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeEntry<T extends Recipe<?>, B> extends RegistryEntry<RecipeType<? super T>> {
    private final Registrate registrate;
    private final SmartRecipeBuilder.Factory<T, B> builderFactory;
    @Nullable
    private RecipeSerializer<T> serializer;
    private final String prefix;

    public RecipeTypeEntry(Registrate registrate, String id,
                           Supplier<RecipeType<? super T>> supplier,
                           SmartRecipeBuilder.Factory<T, B> builderFactory, String prefix) {
        super(registrate.modid, id, supplier);
        this.registrate = registrate;
        this.builderFactory = builderFactory;
        this.prefix = prefix;
    }

    public RecipeSerializer<T> getSerializer() {
        assert this.serializer != null;
        return this.serializer;
    }

    public void setSerializer(RecipeSerializer<T> serializer) {
        this.serializer = serializer;
    }

    @SuppressWarnings("unchecked")
    public RecipeType<T> getProperType() {
        return (RecipeType<T>) this.get();
    }

    public B recipe(ResourceLocation loc) {
        return this.builderFactory.create(this.registrate, this, loc);
    }

    public B recipe(ItemLike item) {
        var loc = item.asItem().getRegistryName();
        assert loc != null;
        return this.recipe(prepend(loc, this.prefix));
    }

    public B recipe(String id) {
        return this.recipe(prepend(new ResourceLocation(id), this.prefix));
    }

    public B modRecipe(ItemLike item) {
        var loc = item.asItem().getRegistryName();
        assert loc != null;
        return this.modRecipe(loc.getNamespace() + "/" + loc.getPath());
    }

    public B modRecipe(String id) {
        return this.recipe(prepend(new ResourceLocation(this.modid, id), this.prefix));
    }
}
