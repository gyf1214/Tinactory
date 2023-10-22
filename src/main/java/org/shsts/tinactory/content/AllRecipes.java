package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.recipe.NullRecipe;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RecipeTypeEntry<NullRecipe, NullRecipe.Builder> NULL_RECIPE_TYPE;
    public static final RecipeTypeEntry<ToolRecipe, ToolRecipe.Builder> TOOL_RECIPE_TYPE;

    private static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    static {
        NULL_RECIPE_TYPE = REGISTRATE.recipeType("null", NullRecipe::serializer)
                .builder(NullRecipe.Builder::new)
                .existingType(() -> RecipeType.CRAFTING)
                .register();

        TOOL_RECIPE_TYPE = REGISTRATE.recipeType("tool", ToolRecipe::serializer)
                .builder(ToolRecipe.Builder::new)
                .register();

        NULL_RECIPE_TYPE.recipe("oak_wood").build();

        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(Items.OAK_PLANKS, 2)
                .requires(ItemTags.OAK_LOGS)
                .group("planks")
                .unlockedBy("has_logs", has(ItemTags.OAK_LOGS)));

        TOOL_RECIPE_TYPE.modRecipe("oak_plank")
                .result(Items.OAK_PLANKS, 4)
                .pattern("X")
                .define('X', ItemTags.OAK_LOGS)
                .damage(100)
                .toolTag(AllTags.TOOL_SAW)
                .build();
    }

    public static void init() {}
}
