package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.recipe.ProcessingStackHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeDisplayRegistry {
    private static final Map<Class<?>, IRecipeDisplayProvider<?>> PROVIDERS = new HashMap<>();
    private static final Map<Class<?>, Optional<IRecipeDisplayProvider<?>>> CACHE = new HashMap<>();

    static {
        register(ProcessingRecipe.class, new IRecipeDisplayProvider<>() {
            @Override
            public Optional<List<Component>> tooltip(ProcessingRecipe recipe) {
                return ProcessingDisplayHelper.tooltip(baseDisplayObject(recipe));
            }

            @Override
            public void render(ProcessingRecipe recipe, PoseStack poseStack, Rect rect, int z) {
                ProcessingDisplayHelper.render(baseDisplayObject(recipe), poseStack, rect, z);
            }
        });
        register(DisplayInputRecipe.class, new IRecipeDisplayProvider<>() {
            @Override
            public Optional<List<Component>> tooltip(DisplayInputRecipe recipe) {
                return ProcessingDisplayHelper.tooltip(displayInputObject(recipe));
            }

            @Override
            public void render(DisplayInputRecipe recipe, PoseStack poseStack, Rect rect, int z) {
                ProcessingDisplayHelper.render(displayInputObject(recipe), poseStack, rect, z);
            }
        });
        register(MarkerRecipe.class, new IRecipeDisplayProvider<>() {
            @Override
            public Optional<List<Component>> tooltip(MarkerRecipe recipe) {
                return Optional.of(List.of((Component) I18n.tr(ProcessingRecipe.getDescriptionId(recipe.loc()))));
            }

            @Override
            public void render(MarkerRecipe recipe, PoseStack poseStack, Rect rect, int z) {
                var displayObject = recipe.displayIngredient()
                    .<IProcessingObject>map(ingredient -> ingredient)
                    .orElseGet(() -> baseDisplayObject(recipe));
                recipe.displayTexture().ifPresentOrElse(
                    tex -> RenderUtil.blit(poseStack, tex, z, rect),
                    () -> ProcessingDisplayHelper.render(displayObject, poseStack, rect, z)
                );
            }
        });
    }

    private RecipeDisplayRegistry() {}

    public static <R extends ProcessingRecipe> void register(Class<R> type, IRecipeDisplayProvider<R> provider) {
        PROVIDERS.put(type, provider);
        CACHE.clear();
    }

    public static Optional<List<Component>> tooltip(ProcessingRecipe recipe) {
        return find(recipe).flatMap(provider -> provider.tooltip(recipe));
    }

    public static void render(ProcessingRecipe recipe, PoseStack poseStack, Rect rect, int z) {
        find(recipe).ifPresent(provider -> provider.render(recipe, poseStack, rect, z));
    }

    @SuppressWarnings("unchecked")
    private static Optional<IRecipeDisplayProvider<ProcessingRecipe>> find(ProcessingRecipe recipe) {
        return (Optional<IRecipeDisplayProvider<ProcessingRecipe>>) (Optional<?>)
            CACHE.computeIfAbsent(recipe.getClass(), RecipeDisplayRegistry::lookup);
    }

    private static Optional<IRecipeDisplayProvider<?>> lookup(Class<?> type) {
        var current = type;
        while (current != null && ProcessingRecipe.class.isAssignableFrom(current)) {
            var provider = PROVIDERS.get(current);
            if (provider != null) {
                return Optional.of(provider);
            }
            current = current.getSuperclass();
        }
        return Optional.empty();
    }

    private static IProcessingObject baseDisplayObject(ProcessingRecipe recipe) {
        if (!recipe.outputs.isEmpty()) {
            return recipe.outputs.stream().min(java.util.Comparator.comparingInt(ProcessingRecipe.Output::port))
                .orElseThrow().result();
        }
        if (!recipe.inputs.isEmpty()) {
            return recipe.inputs.stream().min(java.util.Comparator.comparingInt(ProcessingRecipe.Input::port))
                .orElseThrow().ingredient();
        }
        return ProcessingStackHelper.EMPTY;
    }

    private static IProcessingObject displayInputObject(DisplayInputRecipe recipe) {
        return recipe.inputs.get(0).ingredient();
    }
}
