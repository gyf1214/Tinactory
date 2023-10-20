package org.shsts.tinactory.registrate.handler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.core.SmartRecipe;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class RecipeDataHandler extends DataHandler<RecipeDataHandler.Provider> {
    public RecipeDataHandler(Registrate registrate) {
        super(registrate);
    }

    public class Provider extends RecipeProvider {
        @Nullable
        private Consumer<FinishedRecipe> cons = null;

        public Provider(GatherDataEvent event) {
            super(event.getGenerator());
        }

        public void addRecipe(SmartRecipe<?, ?> recipe) {
            assert this.cons != null;
            this.cons.accept(recipe.toFinished());
        }

        @Override
        protected void buildCraftingRecipes(Consumer<FinishedRecipe> cons) {
            this.cons = cons;
            RecipeDataHandler.this.register(this);
            this.cons = null;
        }
    }

    @Override
    public void onGatherData(GatherDataEvent event) {
        var prov = new Provider(event);
        event.getGenerator().addProvider(prov);
    }
}
