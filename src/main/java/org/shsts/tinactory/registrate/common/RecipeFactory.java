package org.shsts.tinactory.registrate.common;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.shsts.tinactory.core.common.BuilderBase;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeFactory<B extends BuilderBase<?, ?, B>> {
    private final Registrate registrate;
    private final RecipeTypeEntry<?, B> recipeType;

    public RecipeFactory(Registrate registrate, RecipeTypeEntry<?, B> recipeType) {
        this.registrate = registrate;
        this.recipeType = recipeType;
    }

    public B recipe(ResourceLocation loc) {
        return recipeType.recipe(registrate, loc);
    }

    public B recipe(String id) {
        return recipe(new ResourceLocation(registrate.modid, id));
    }

    public B recipe(IForgeRegistryEntry<?> item) {
        var loc = item.getRegistryName();
        assert loc != null;
        return recipe(loc);
    }

    public B recipe(RegistryEntry<?> item) {
        return recipe(item.loc);
    }
}
