package org.shsts.tinactory.compat.jei.ingredient;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.shsts.tinactory.api.tech.IClientTechManager;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.integration.tech.TechManagers;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;
import org.slf4j.Logger;

import java.util.Collection;

import static org.shsts.tinactory.AllRecipes.PROCESSING_TYPES;
import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TechIngredientIndex {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record CacheKey(ResourceLocation tech, IRecipeType<?> recipeType) {}

    private final Multimap<CacheKey, IEntry<? extends ResearchRecipe>> byRequiredTech =
        ArrayListMultimap.create();
    private boolean recipeInitialized;

    private TechIngredientIndex(boolean recipeInitialized) {
        this.recipeInitialized = recipeInitialized;
        rebuildIfInitialized();
    }

    private void rebuildIfInitialized() {
        var techManager = TechManagers.client();
        if (recipeInitialized && techManager.techInitialized()) {
            rebuild(techManager);
        }
    }

    @SuppressWarnings("unchecked")
    private void rebuild(IClientTechManager techManager) {
        byRequiredTech.clear();

        LOGGER.debug("start rebuilding tech recipe cache");
        var recipeManager = CORE.clientRecipeManager();
        for (var info : PROCESSING_TYPES.values()) {
            var type = info.recipeType();
            if (!ResearchRecipe.class.isAssignableFrom(type.recipeClass())) {
                continue;
            }
            for (var recipe : recipeManager.getAllRecipesFor((IRecipeType<? extends ResearchRecipe>) type)) {
                var target = techManager.techByKey(recipe.get().target);
                if (target.isEmpty()) {
                    LOGGER.warn("skip unknown tech={}, recipe={}", recipe.get().target, recipe.loc());
                    continue;
                }
                for (var depend : target.get().getDepends()) {
                    var key = techManager.key(depend);
                    if (key.isEmpty()) {
                        LOGGER.warn("skip unknown dependency={}, child={}", depend, recipe.get().target);
                        continue;
                    }
                    byRequiredTech.put(new CacheKey(key.get(), type), recipe);
                }
            }
        }
        LOGGER.debug("finished rebuilding tech recipe cache, {} entries", byRequiredTech.size());
    }

    @SubscribeEvent
    public void onRecipeUpdate(RecipesUpdatedEvent event) {
        recipeInitialized = true;
        rebuildIfInitialized();
    }

    @SubscribeEvent
    public void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        byRequiredTech.clear();
        recipeInitialized = false;
    }

    public Collection<IEntry<? extends ResearchRecipe>> getRecipesByRequiredTech(
        ResourceLocation tech, IRecipeType<?> recipeType) {
        return byRequiredTech.get(new CacheKey(tech, recipeType));
    }

    private void onTechInit() {
        rebuildIfInitialized();
    }

    private void register() {
        NeoForge.EVENT_BUS.register(this);
        TechManagers.client().onTechInit(this::onTechInit);
    }

    private static TechIngredientIndex instance = null;
    private static final Object lock = new Object();

    public static TechIngredientIndex getInstance(boolean recipeInitialized) {
        var needInit = false;
        synchronized (lock) {
            if (instance == null) {
                instance = new TechIngredientIndex(recipeInitialized);
                needInit = true;
            }
        }
        if (needInit) {
            instance.register();
        }
        return instance;
    }
}
