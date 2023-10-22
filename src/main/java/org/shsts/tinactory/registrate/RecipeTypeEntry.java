package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.registrate.builder.SmartRecipeBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeEntry<T extends Recipe<?>, B> extends RegistryEntry<RecipeType<? super T>> {
    private final Registrate registrate;
    private final SmartRecipeBuilder.Factory<T, B> builderFactory;
    @Nullable
    private RecipeSerializer<T> serializer;

    public RecipeTypeEntry(Registrate registrate, String id,
                           Supplier<RecipeType<? super T>> supplier,
                           SmartRecipeBuilder.Factory<T, B> builderFactory) {
        super(registrate.modid, id, supplier);
        this.registrate = registrate;
        this.builderFactory = builderFactory;
    }

    public RecipeSerializer<T> getSerializer() {
        assert this.serializer != null;
        return this.serializer;
    }

    public void setSerializer(RecipeSerializer<T> serializer) {
        this.serializer = serializer;
    }

    public B recipe(ResourceLocation loc) {
        return this.builderFactory.create(this.registrate, this, loc);
    }

    public B recipe(String loc) {
        return this.recipe(new ResourceLocation(loc));
    }

    public B modRecipe(String id) {
        return this.recipe(new ResourceLocation(this.modid, id));
    }
}
