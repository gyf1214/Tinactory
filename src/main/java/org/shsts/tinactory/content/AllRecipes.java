package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.recipe.NullRecipe;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RecipeTypeEntry<NullRecipe, NullRecipe.Builder> NULL;
    public static final RecipeTypeEntry<ToolRecipe, ToolRecipe.Builder> TOOL;
    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> STONE_GENERATOR;

    static {
        NULL = REGISTRATE.recipeType("null", NullRecipe.SERIALIZER)
                .builder(NullRecipe.Builder::new)
                .existingType(() -> RecipeType.CRAFTING)
                .register();

        TOOL = REGISTRATE.recipeType("tool", ToolRecipe.SERIALIZER)
                .prefix("tool_recipe")
                .builder(ToolRecipe.Builder::new)
                .register();

        STONE_GENERATOR = REGISTRATE.recipeType("stone", ProcessingRecipe.SIMPLE_SERIALIZER)
                .prefix("processing/stone_generator")
                .builder(ProcessingRecipe.SimpleBuilder::new)
                .register();

        // disable add wooden tools
        NULL.recipe(Items.WOODEN_AXE);
        NULL.recipe(Items.WOODEN_HOE);
        NULL.recipe(Items.WOODEN_PICKAXE);
        NULL.recipe(Items.WOODEN_SHOVEL);
        NULL.recipe(Items.WOODEN_SWORD);

        // all wood recipes
        woodRecipes("oak");
        woodRecipes("spruce");
        woodRecipes("birch");
        woodRecipes("jungle");
        woodRecipes("acacia");
        woodRecipes("dark_oak");
        woodRecipes("crimson");
        woodRecipes("warped");

        // primitive stone generator
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(AllBlocks.PRIMITIVE_STONE_GENERATOR.get())
                .pattern("WLW")
                .pattern("L L")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS)));

        // generate cobblestone
        STONE_GENERATOR.modRecipe(Items.COBBLESTONE)
                .output(0, Items.COBBLESTONE, 1)
                .workTicks(40)
                .build();
    }

    private static void woodRecipes(String prefix) {
        var nether = prefix.equals("crimson") || prefix.equals("warped");

        var planks = REGISTRATE.itemHandler.getEntry(prefix + "_planks");
        var logTag = AllTags.item(prefix + (nether ? "_stems" : "_logs"));
        var wood = prefix + (nether ? "_hyphae" : "_wood");
        var woodStripped = "stripped_" + wood;

        // saw
        TOOL.modRecipe("saw/" + planks.id)
                .result(planks, 4)
                .pattern("X")
                .define('X', logTag)
                .damage(100)
                .toolTag(AllTags.TOOL_SAW)
                .build();
        // disable wood and woodStripped recipes
        // TODO: maybe not necessary
        NULL.recipe(wood).build();
        NULL.recipe(woodStripped).build();
        // reduce vanilla recipe to 2 planks
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(planks.get(), 2)
                .requires(logTag)
                .group("planks")
                .unlockedBy("has_logs", has(logTag)));
    }

    private static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    public static void init() {}
}
