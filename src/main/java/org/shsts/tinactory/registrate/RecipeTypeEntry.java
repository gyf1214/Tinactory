package org.shsts.tinactory.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.core.SmartRecipeSerializer;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeTypeEntry<T extends SmartRecipe<?, T>>
        extends RegistryEntry<RecipeType<? super T>> {
    private final Registrate registrate;
    @Nullable
    private SmartRecipeSerializer<T> serializer;

    public RecipeTypeEntry(Registrate registrate, String id, Supplier<RecipeType<? super T>> supplier) {
        super(registrate.modid, id, supplier);
        this.registrate = registrate;
    }

    public SmartRecipeSerializer<T> getSerializer() {
        assert this.serializer != null;
        return this.serializer;
    }

    public void setSerializer(SmartRecipeSerializer<T> serializer) {
        this.serializer = serializer;
    }
}
