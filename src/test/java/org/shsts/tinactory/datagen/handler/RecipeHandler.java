package org.shsts.tinactory.datagen.handler;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.datagen.DataGen;

import java.util.function.Consumer;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeHandler extends DataHandler<RecipeProvider> {
    public RecipeHandler(DataGen dataGen) {
        super(dataGen);
    }

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

        @Override
        public String getName() {
            return "Recipes: " + dataGen.modid;
        }
    }

    @Override
    protected RecipeProvider createProvider(GatherDataEvent event) {
        return new Provider(event);
    }

    public void registerRecipe(Supplier<FinishedRecipe> recipe) {
        addCallback(prov -> ((Provider) prov).addRecipe(recipe.get()));
    }

    public void registerRecipe(Consumer<Consumer<FinishedRecipe>> recipe) {
        addCallback(prov -> recipe.accept(((Provider) prov)::addRecipe));
    }
}
