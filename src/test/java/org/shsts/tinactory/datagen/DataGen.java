package org.shsts.tinactory.datagen;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.core.common.SmartRecipe;
import org.shsts.tinactory.core.recipe.IRecipeDataConsumer;
import org.shsts.tinactory.core.recipe.NullRecipe;
import org.shsts.tinactory.datagen.content.Components;
import org.shsts.tinactory.datagen.content.Machines;
import org.shsts.tinactory.datagen.content.Markers;
import org.shsts.tinactory.datagen.content.Materials;
import org.shsts.tinactory.datagen.content.Models;
import org.shsts.tinactory.datagen.content.Technologies;
import org.shsts.tinactory.datagen.content.Veins;
import org.shsts.tinactory.datagen.context.TrackedContext;
import org.shsts.tinactory.datagen.handler.DataHandler;
import org.shsts.tinactory.datagen.handler.LanguageHandler;
import org.shsts.tinactory.datagen.handler.RecipeHandler;
import org.shsts.tinactory.datagen.handler.TechHandler;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.tracking.TrackedType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class DataGen implements IRecipeDataConsumer {
    public final String modid;

    public final TrackedContext<String> langTrackedCtx;

    public final RecipeHandler recipeHandler;
    public final TechHandler techHandler;
    public final LanguageHandler languageHandler;

    private final Registrate registrate;
    private final List<DataHandler<?>> dataHandlers;
    private final Set<TrackedContext<?>> trackedContexts;

    public DataGen(Registrate registrate) {
        this.modid = registrate.modid;
        this.registrate = registrate;
        this.dataHandlers = new ArrayList<>();
        this.trackedContexts = new HashSet<>();

        this.langTrackedCtx = trackedCtx(TrackedType.LANG);

        this.recipeHandler = handler(new RecipeHandler(this));
        this.techHandler = handler(new TechHandler(this));
        this.languageHandler = handler(new LanguageHandler(this, langTrackedCtx));
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

    public void trackLang(String key) {
        langTrackedCtx.trackExtra(key, key);
    }

    public void trackLang(ResourceLocation key) {
        trackLang(key.getNamespace() + '.' + key.getPath().replace('/', '.'));
    }

    @Override
    public String getModId() {
        return modid;
    }

    @Override
    public void registerRecipe(ResourceLocation loc, Supplier<FinishedRecipe> recipe) {
        recipeHandler.registerRecipe(recipe);
    }

    @Override
    public void registerSmartRecipe(ResourceLocation loc, Supplier<SmartRecipe<?>> recipe) {
        recipeHandler.registerSmartRecipe(recipe);
    }

    private <T extends DataHandler<?>> T handler(T dataHandler) {
        dataHandlers.add(dataHandler);
        return dataHandler;
    }

    private <V> TrackedContext<V> trackedCtx(TrackedType<V> type) {
        var ret = new TrackedContext<>(registrate, type);
        trackedContexts.add(ret);
        return ret;
    }

    public void onGatherData(GatherDataEvent event) {
        init();
        for (var handler : dataHandlers) {
            handler.onGatherData(event);
        }
        event.getGenerator().addProvider(new DataProvider() {
            @Override
            public void run(HashCache cache) {
                for (var trackedCtx : trackedContexts) {
                    trackedCtx.postValidate();
                }
            }

            @Override
            public String getName() {
                return "Validation: " + modid;
            }
        });
    }

    public static final DataGen _DATA_GEN = new DataGen(Tinactory._REGISTRATE);

    public static void init() {
        Models.init();
        Technologies.init();
        Materials.init();
        Components.init();
        Machines.init();
        Veins.init();
        Markers.init();
    }
}
