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
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final RecipeTypeEntry<ToolRecipe, ToolRecipe.Builder> TOOL;
    public static final RecipeTypeEntry<ResearchRecipe, ResearchRecipe.Builder> RESEARCH;
    public static final RecipeTypeEntry<AssemblyRecipe, AssemblyRecipe.Builder> ASSEMBLER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> STONE_GENERATOR;
    public static final RecipeTypeEntry<AssemblyRecipe, AssemblyRecipe.Builder> ORE_ANALYZER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> MACERATOR;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> ORE_WASHER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> CENTRIFUGE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> THERMAL_CENTRIFUGE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> ALLOY_SMELTER;

    static {
        TOOL = REGISTRATE.recipeType("tool", ToolRecipe.SERIALIZER)
                .clazz(ToolRecipe.class)
                .builder(ToolRecipe.Builder::new)
                .register();

        RESEARCH = REGISTRATE.recipeType("research", ResearchRecipe.SERIALIZER)
                .clazz(ResearchRecipe.class)
                .builder(ResearchRecipe.Builder::new)
                .defaults($ -> $.amperage(0.25d))
                .register();

        ASSEMBLER = REGISTRATE.assemblyRecipeType("assembler")
                .defaults($ -> $.amperage(0.375d))
                .register();

        STONE_GENERATOR = REGISTRATE.processingRecipeType("stone_generator")
                .defaults($ -> $.amperage(0.125d).workTicks(40))
                .register();

        ORE_ANALYZER = REGISTRATE.assemblyRecipeType("ore_analyzer")
                .defaults($ -> $.amperage(0.125d).workTicks(32))
                .register();

        MACERATOR = REGISTRATE.processingRecipeType("macerator")
                .defaults($ -> $.amperage(0.25d))
                .register();

        ORE_WASHER = REGISTRATE.processingRecipeType("ore_washer")
                .defaults($ -> $
                        .inputFluid(1, Fluids.WATER, 1000)
                        .outputItem(3, AllMaterials.STONE.entry("dust"), 1)
                        .amperage(0.125d))
                .register();

        CENTRIFUGE = REGISTRATE.processingRecipeType("centrifuge")
                .defaults($ -> $.amperage(0.5d))
                .register();

        THERMAL_CENTRIFUGE = REGISTRATE.processingRecipeType("thermal_centrifuge")
                .defaults($ -> $
                        .voltage(Voltage.LV)
                        .workTicks(400)
                        .amperage(1.5d))
                .register();

        ALLOY_SMELTER = REGISTRATE.processingRecipeType("alloy_smelter")
                .defaults($ -> $.amperage(0.75d))
                .register();
    }

    public static void initRecipes() {
        AllMaterials.initRecipes();
        vanillaRecipes();
        primitiveRecipes();
        AllItems.initRecipes();
    }

    private static void vanillaRecipes() {
        // disable wooden and iron tools
        REGISTRATE.nullRecipe(Items.WOODEN_AXE);
        REGISTRATE.nullRecipe(Items.WOODEN_HOE);
        REGISTRATE.nullRecipe(Items.WOODEN_PICKAXE);
        REGISTRATE.nullRecipe(Items.WOODEN_SHOVEL);
        REGISTRATE.nullRecipe(Items.WOODEN_SWORD);
        REGISTRATE.nullRecipe(Items.IRON_AXE);
        REGISTRATE.nullRecipe(Items.IRON_HOE);
        REGISTRATE.nullRecipe(Items.IRON_PICKAXE);
        REGISTRATE.nullRecipe(Items.IRON_SHOVEL);
        REGISTRATE.nullRecipe(Items.IRON_SWORD);

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
        TOOL.recipe(Items.STICK)
                .result(Items.STICK, 4)
                .pattern("#").pattern("#")
                .define('#', ItemTags.PLANKS)
                .toolTag(AllTags.TOOL_SAW)
                .build();
    }

    private static void woodRecipes(String prefix) {
        var nether = prefix.equals("crimson") || prefix.equals("warped");

        var planks = REGISTRATE.itemHandler.getEntry(prefix + "_planks");
        var logTag = AllTags.item(prefix + (nether ? "_stems" : "_logs"));
        var wood = prefix + (nether ? "_hyphae" : "_wood");
        var woodStripped = "stripped_" + wood;

        // saw
        TOOL.recipe(planks.loc)
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

    private static void primitiveRecipes() {
        // workbench
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(AllBlockEntities.WORKBENCH.getBlock())
                .pattern("WSW")
                .pattern("SCS")
                .pattern("WSW")
                .define('S', AllMaterials.STONE.tag("block"))
                .define('W', Items.STICK)
                .define('C', Blocks.CRAFTING_TABLE)
                .unlockedBy("has_cobblestone", has(AllMaterials.STONE.tag("block"))));

        // primitive stone generator
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(AllBlockEntities.STONE_GENERATOR.getPrimitive())
                .pattern("WLW")
                .pattern("L L")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .unlockedBy("has_planks", has(ItemTags.PLANKS)));

        // primitive ore analyzer
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(AllBlockEntities.ORE_ANALYZER.getPrimitive())
                .pattern("WLW")
                .pattern("LFL")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .define('F', AllMaterials.FLINT.tag("primary"))
                .unlockedBy("has_flint", has(AllMaterials.FLINT.tag("primary"))));

        // primitive ore washer
        REGISTRATE.vanillaRecipe(() -> ShapedRecipeBuilder
                .shaped(AllBlockEntities.ORE_WASHER.getPrimitive())
                .pattern("WLW")
                .pattern("LFL")
                .pattern("WLW")
                .define('W', ItemTags.PLANKS)
                .define('L', ItemTags.LOGS)
                .define('F', Items.WATER_BUCKET)
                .unlockedBy("has_water_bucket", has(Items.WATER_BUCKET)));

        // generate cobblestone
        STONE_GENERATOR.recipe(Items.COBBLESTONE)
                .outputItem(0, Items.COBBLESTONE, 1)
                .primitive().power(1).workTicks(40)
                .build();
    }

    public static InventoryChangeTrigger.TriggerInstance has(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    public static InventoryChangeTrigger.TriggerInstance has(Item item) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(item).build());
    }

    private static InventoryChangeTrigger.TriggerInstance inventoryTrigger(ItemPredicate... predicates) {
        return new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY,
                MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates);
    }

    public static void init() {}
}
