package org.shsts.tinactory.datagen.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.datagen.DataGen;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeHandler extends DataHandler<RecipeProvider> implements IRecipeDataConsumer {
    private class Provider extends RecipeProvider {
        @Nullable
        private Consumer<FinishedRecipe> consumer = null;

        public Provider(GatherDataEvent event) {
            super(event.getGenerator());
        }

        public void addRecipe(FinishedRecipe recipe) {
            assert consumer != null;
            consumer.accept(recipe);
        }

        private void addRecipes() {
            RecipeHandler.this.register(this);
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
            this.consumer = consumer;
            addRecipes();
            this.consumer = null;
        }
    }

    public RecipeHandler(DataGen dataGen) {
        super(dataGen);
    }

    @Override
    protected RecipeProvider createProvider(GatherDataEvent event) {
        return new Provider(event);
    }

    @Override
    public void addRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe) {
        addCallback(prov -> ((Provider) prov).addRecipe(recipe.get()));
    }
}
