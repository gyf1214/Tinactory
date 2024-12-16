package org.shsts.tinactory.datagen;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.recipe.NullRecipe;
import org.shsts.tinactory.datagen.content.Components;
import org.shsts.tinactory.datagen.content.Machines;
import org.shsts.tinactory.datagen.content.Markers;
import org.shsts.tinactory.datagen.content.Materials;
import org.shsts.tinactory.datagen.content.Models;
import org.shsts.tinactory.datagen.content.Technologies;
import org.shsts.tinactory.datagen.content.Veins;
import org.shsts.tinactory.datagen.handler.DataHandler;
import org.shsts.tinactory.datagen.handler.LanguageDataProvider;
import org.shsts.tinactory.datagen.handler.RecipeHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class DataGen {
    public final String modid;

    public final RecipeHandler recipeHandler;

    private final List<DataHandler<?>> dataHandlers;

    public DataGen(String modid) {
        this.modid = modid;
        this.dataHandlers = new ArrayList<>();

        this.recipeHandler = handler(new RecipeHandler(this));
    }

    public DataGen replaceVanillaRecipe(Supplier<RecipeBuilder> recipe) {
        recipeHandler.registerRecipe(cons -> recipe.get().save(cons));
        return this;
    }

    public DataGen vanillaRecipe(Supplier<RecipeBuilder> recipe) {
        return vanillaRecipe(recipe, "");
    }

    public DataGen vanillaRecipe(Supplier<RecipeBuilder> recipe, String suffix) {
        recipeHandler.registerRecipe(cons -> {
            var builder = recipe.get();
            var loc = builder.getResult().getRegistryName();
            assert loc != null;
            var prefix = builder instanceof SimpleCookingRecipeBuilder ? "smelt" : "craft";
            var recipeLoc = new ResourceLocation(modid, prefix + "/" + loc.getPath() + suffix);
            builder.save(cons, recipeLoc);
        });
        return this;
    }

    public DataGen nullRecipe(ResourceLocation loc) {
        recipeHandler.registerRecipe(() -> new NullRecipe(loc));
        return this;
    }

    public DataGen nullRecipe(String loc) {
        return nullRecipe(new ResourceLocation(loc));
    }

    public DataGen nullRecipe(Item item) {
        var loc = item.getRegistryName();
        assert loc != null;
        return nullRecipe(loc);
    }

    private <T extends DataHandler<?>> T handler(T dataHandler) {
        dataHandlers.add(dataHandler);
        return dataHandler;
    }

    public void onGatherData(GatherDataEvent event) {
        for (var handler : dataHandlers) {
            handler.onGatherData(event);
        }
    }

    public static final DataGen _DATA_GEN = new DataGen(Tinactory.ID);

    public static void init() {
        DATA_GEN.addProvider(LanguageDataProvider::new);
        Models.init();
        Technologies.init();
        Materials.init();
        Components.init();
        Machines.init();
        Veins.init();
        Markers.init();
    }
}
