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
import net.minecraft.world.level.block.Blocks;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.content.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RecipeTypeEntry<ToolRecipe, ToolRecipe.Builder> TOOL;
    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> STONE_GENERATOR;

    static {
        TOOL = REGISTRATE.recipeType("tool", ToolRecipe.SERIALIZER)
                .prefix("tool_recipe")
                .builder(ToolRecipe.Builder::new)
                .register();

        STONE_GENERATOR = REGISTRATE.recipeType("stone", ProcessingRecipe.SIMPLE_SERIALIZER)
                .prefix("processing/stone_generator")
                .builder(ProcessingRecipe.SimpleBuilder::new)
                .register();

        // disable add wooden tools
        REGISTRATE.nullRecipe(Items.WOODEN_AXE);
        REGISTRATE.nullRecipe(Items.WOODEN_HOE);
        REGISTRATE.nullRecipe(Items.WOODEN_PICKAXE);
        REGISTRATE.nullRecipe(Items.WOODEN_SHOVEL);
        REGISTRATE.nullRecipe(Items.WOODEN_SWORD);

        // all wood recipes
        woodRecipes("oak");
        woodRecipes("spruce");
        woodRecipes("birch");
        woodRecipes("jungle");
        woodRecipes("acacia");
        woodRecipes("dark_oak");
        woodRecipes("crimson");
        woodRecipes("warped");

        // sticks
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(Items.STICK, 2)
                .define('#', ItemTags.PLANKS)
                .pattern("#").pattern("#")
                .unlockedBy("has_planks", has(ItemTags.PLANKS)));
        TOOL.modRecipe(Items.STICK)
                .result(Items.STICK, 4)
                .pattern("#").pattern("#")
                .define('#', ItemTags.PLANKS)
                .toolTag(AllTags.TOOL_SAW)
                .build();

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

        // workbench
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(AllBlocks.WORKBENCH.get())
                .pattern("WSW")
                .pattern("SCS")
                .pattern("WSW")
                .define('S', ItemTags.STONE_CRAFTING_MATERIALS)
                .define('W', Items.STICK)
                .define('C', Blocks.CRAFTING_TABLE)
                .unlockedBy("has_cobblestone", has(ItemTags.STONE_CRAFTING_MATERIALS)));

        // tool handle tag
        REGISTRATE.itemTag(Items.STICK, AllTags.TOOL_HANDLE);

        // hammer recipes for stone & gravel
        TOOL.modRecipe(Items.GRAVEL)
                .result(Items.GRAVEL, 1)
                .pattern("#").pattern("#")
                .define('#', ItemTags.STONE_CRAFTING_MATERIALS)
                .toolTag(AllTags.TOOL_HAMMER)
                .build();
        TOOL.modRecipe(Items.FLINT)
                .result(Items.FLINT, 1)
                .pattern("###")
                .define('#', Items.GRAVEL)
                .toolTag(AllTags.TOOL_HAMMER)
                .build();

        // mortar recipes for gravel
        TOOL.modRecipe(Items.SAND)
                .result(Items.SAND, 1)
                .pattern("#")
                .define('#', Items.GRAVEL)
                .toolTag(AllTags.TOOL_MORTAR)
                .build();
    }

    private static void woodRecipes(String prefix) {
        var nether = prefix.equals("crimson") || prefix.equals("warped");

        var planks = REGISTRATE.itemHandler.getEntry(prefix + "_planks");
        var logTag = AllTags.item(prefix + (nether ? "_stems" : "_logs"));
        var wood = prefix + (nether ? "_hyphae" : "_wood");
        var woodStripped = "stripped_" + wood;

        // saw
        TOOL.modRecipe(planks.loc)
                .result(planks, 4)
                .pattern("X")
                .define('X', logTag)
                .toolTag(AllTags.TOOL_SAW)
                .build();
        // disable wood and woodStripped recipes
        // TODO: maybe not necessary
        REGISTRATE.nullRecipe(wood);
        REGISTRATE.nullRecipe(woodStripped);
        // reduce vanilla recipe to 2 planks
        REGISTRATE.vanillaRecipe(() -> ShapelessRecipeBuilder
                .shapeless(planks.get(), 2)
                .requires(logTag)
                .group("planks")
                .unlockedBy("has_logs", has(logTag)));
    }

    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    public static void init() {}
}
