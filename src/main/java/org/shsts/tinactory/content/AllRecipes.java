package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final IRecipeType<ToolRecipe.Builder> TOOL_CRAFTING;
    public static final IRecipeType<ProcessingRecipe.Builder> DISTILLATION;
    // Recipes only used to mark input for recipe book purpose
    public static final IRecipeType<MarkerRecipe.Builder> MARKER;

    static {
        TOOL_CRAFTING = REGISTRATE.vanillaRecipeType("tool_crafting", ToolRecipe.Builder::new)
            .recipeClass(ToolRecipe.class)
            .serializer(ToolRecipe.SERIALIZER)
            .register();

        DISTILLATION = processing("distillation", DistillationRecipe::builder);

        MARKER = REGISTRATE.recipeType("marker", MarkerRecipe.Builder::new)
            .recipeClass(MarkerRecipe.class)
            .serializer(MarkerRecipe.SERIALIZER)
            .register();
    }

    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    public static InventoryChangeTrigger.TriggerInstance has(ItemLike item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(item).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
            MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    private static IRecipeType<ProcessingRecipe.Builder> processing(
        String id, IRecipeType.BuilderFactory<ProcessingRecipe.Builder> builderFactory) {
        return REGISTRATE.recipeType(id, builderFactory)
            .recipeClass(ProcessingRecipe.class)
            .serializer(ProcessingRecipe.SERIALIZER)
            .register();
    }

    public static void init() {}
}
