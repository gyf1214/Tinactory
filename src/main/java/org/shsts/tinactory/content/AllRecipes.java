package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
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
    public static final RecipeTypeEntry<ToolRecipe, ToolRecipe.Builder> TOOL_CRAFTING;
    public static final RecipeTypeEntry<ResearchRecipe, ResearchRecipe.Builder> RESEARCH_BENCH;
    public static final RecipeTypeEntry<AssemblyRecipe, AssemblyRecipe.Builder> ASSEMBLER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> STONE_GENERATOR;
    public static final RecipeTypeEntry<OreAnalyzerRecipe, OreAnalyzerRecipe.Builder> ORE_ANALYZER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> MACERATOR;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> ORE_WASHER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> CENTRIFUGE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> THERMAL_CENTRIFUGE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> ALLOY_SMELTER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> POLARIZER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> EXTRACTOR;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> FLUID_SOLIDIFIER;
    public static final RecipeTypeEntry<GeneratorRecipe, GeneratorRecipe.Builder> STEAM_TURBINE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> BLAST_FURNACE;
    // Recipes only used to mark input for recipe book purpose
    public static final RecipeTypeEntry<MarkerRecipe, MarkerRecipe.Builder> MARKER;

    static {
        TOOL_CRAFTING = REGISTRATE.recipeType("tool_crafting", ToolRecipe.SERIALIZER)
                .clazz(ToolRecipe.class)
                .builder(ToolRecipe.Builder::new)
                .register();

        RESEARCH_BENCH = REGISTRATE.recipeType("research_bench", ResearchRecipe.SERIALIZER)
                .clazz(ResearchRecipe.class)
                .builder(ResearchRecipe.Builder::new)
                .defaults($ -> $.amperage(0.25d).workTicks(200))
                .register();

        ASSEMBLER = REGISTRATE.assemblyRecipeType("assembler")
                .defaults($ -> $.amperage(0.375d))
                .register();

        STONE_GENERATOR = REGISTRATE.processingRecipeType("stone_generator")
                .defaults($ -> $.amperage(0.125d).workTicks(20))
                .register();

        ORE_ANALYZER = REGISTRATE.recipeType("ore_analyzer", OreAnalyzerRecipe.SERIALIZER)
                .clazz(OreAnalyzerRecipe.class)
                .builder(OreAnalyzerRecipe.Builder::new)
                .defaults($ -> $.amperage(0.125d).workTicks(32))
                .register();

        MACERATOR = REGISTRATE.processingRecipeType("macerator")
                .defaults($ -> $.amperage(0.25d))
                .register();

        ORE_WASHER = REGISTRATE.processingRecipeType("ore_washer")
                .defaults($ -> $.amperage(0.125d))
                .register();

        CENTRIFUGE = REGISTRATE.processingRecipeType("centrifuge")
                .defaults($ -> $.amperage(0.5d))
                .register();

        THERMAL_CENTRIFUGE = REGISTRATE.processingRecipeType("thermal_centrifuge")
                .defaults($ -> $
                        .voltage(Voltage.LV)
                        .workTicks(400)
                        .amperage(1d))
                .register();

        ALLOY_SMELTER = REGISTRATE.processingRecipeType("alloy_smelter")
                .defaults($ -> $.amperage(0.75d))
                .register();

        POLARIZER = REGISTRATE.processingRecipeType("polarizer")
                .defaults($ -> $.amperage(0.25d).workTicks(40))
                .register();

        EXTRACTOR = REGISTRATE.processingRecipeType("extractor")
                .defaults($ -> $.amperage(0.5d))
                .register();

        FLUID_SOLIDIFIER = REGISTRATE.processingRecipeType("fluid_solidifier")
                .defaults($ -> $.amperage(0.25d))
                .register();

        STEAM_TURBINE = REGISTRATE.recipeType("steam_turbine", GeneratorRecipe.SERIALIZER)
                .clazz(GeneratorRecipe.class)
                .builder(GeneratorRecipe.Builder::new)
                .defaults($ -> $.autoVoid().amperage(1d).workTicks(100))
                .register();

        BLAST_FURNACE = REGISTRATE.processingRecipeType("blast_furnace")
                .register();

        MARKER = REGISTRATE.recipeType("marker", MarkerRecipe.SERIALIZER)
                .clazz(MarkerRecipe.class)
                .builder(MarkerRecipe.Builder::new)
                .register();
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
