package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.DisplayInputRecipe;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.SmartRecipeBuilder;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.RecipeTypeBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final RecipeTypeEntry<ToolRecipe, ToolRecipe.Builder> TOOL_CRAFTING;
    public static final RecipeTypeEntry<ResearchRecipe, ResearchRecipe.Builder> RESEARCH_BENCH;
    public static final RecipeTypeEntry<AssemblyRecipe, AssemblyRecipe.Builder> ASSEMBLER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> CIRCUIT_ASSEMBLER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> STONE_GENERATOR;
    public static final RecipeTypeEntry<OreAnalyzerRecipe, OreAnalyzerRecipe.Builder> ORE_ANALYZER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> MACERATOR;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> ORE_WASHER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> CENTRIFUGE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> THERMAL_CENTRIFUGE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> SIFTER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> ALLOY_SMELTER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> MIXER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> POLARIZER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> WIREMILL;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> BENDER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> COMPRESSOR;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> LATHE;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> CUTTER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> EXTRACTOR;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> FLUID_SOLIDIFIER;
    public static final RecipeTypeEntry<ProcessingRecipe, ProcessingRecipe.Builder> STEAM_TURBINE;
    public static final RecipeTypeEntry<BlastFurnaceRecipe, BlastFurnaceRecipe.Builder> BLAST_FURNACE;
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
            .defaults($ -> $.amperage(0.25d).workTicks(200L))
            .register();

        ASSEMBLER = REGISTRATE.recipeType("assembler", AssemblyRecipe.SERIALIZER)
            .clazz(AssemblyRecipe.class)
            .builder(AssemblyRecipe.Builder::new)
            .defaults($ -> $.amperage(0.375d))
            .register();

        CIRCUIT_ASSEMBLER = processing("circuit_assembler")
            .defaults($ -> $.amperage(0.25d))
            .register();

        STONE_GENERATOR = processing("stone_generator")
            .defaults($ -> $.amperage(0.125d).workTicks(20L))
            .register();

        ORE_ANALYZER = REGISTRATE.recipeType("ore_analyzer", OreAnalyzerRecipe.SERIALIZER)
            .clazz(OreAnalyzerRecipe.class)
            .builder(OreAnalyzerRecipe.Builder::new)
            .defaults($ -> $.amperage(0.125d).workTicks(32L))
            .register();

        MACERATOR = displayInput("macerator")
            .defaults($ -> $.amperage(0.25d))
            .register();

        ORE_WASHER = displayInput("ore_washer")
            .defaults($ -> $.amperage(0.125d))
            .register();

        CENTRIFUGE = displayInput("centrifuge")
            .defaults($ -> $.amperage(0.5d))
            .register();

        THERMAL_CENTRIFUGE = displayInput("thermal_centrifuge")
            .defaults($ -> $.voltage(Voltage.LV)
                .workTicks(400L)
                .amperage(1d))
            .register();

        SIFTER = displayInput("sifter")
            .defaults($ -> $.amperage(0.25d))
            .register();

        ALLOY_SMELTER = processing("alloy_smelter")
            .defaults($ -> $.amperage(0.75d))
            .register();

        MIXER = processing("mixer")
            .defaults($ -> $.amperage(0.5d))
            .register();

        POLARIZER = processing("polarizer")
            .defaults($ -> $.amperage(0.25d))
            .register();

        WIREMILL = processing("wiremill")
            .defaults($ -> $.amperage(0.25d))
            .register();

        BENDER = processing("bender")
            .defaults($ -> $.amperage(0.25d))
            .register();

        COMPRESSOR = processing("compressor")
            .defaults($ -> $.amperage(0.5d))
            .register();

        LATHE = processing("lathe")
            .defaults($ -> $.amperage(0.375d))
            .register();

        CUTTER = processing("cutter")
            .defaults($ -> $.amperage(0.375d))
            .register();

        EXTRACTOR = displayInput("extractor")
            .defaults($ -> $.amperage(0.5d))
            .register();

        FLUID_SOLIDIFIER = processing("fluid_solidifier")
            .defaults($ -> $.amperage(0.25d))
            .register();

        STEAM_TURBINE = processing("steam_turbine", GeneratorRecipe::builder)
            .defaults($ -> $.autoVoid().amperage(1d).workTicks(100))
            .register();

        BLAST_FURNACE = REGISTRATE.recipeType("blast_furnace", BlastFurnaceRecipe.SERIALIZER)
            .clazz(BlastFurnaceRecipe.class)
            .builder(BlastFurnaceRecipe.Builder::new)
            .defaults($ -> $.amperage(4d))
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

    private static RecipeTypeBuilder<ProcessingRecipe, ProcessingRecipe.Builder, Registrate> processing(String id) {
        return processing(id, ProcessingRecipe.Builder::new);
    }

    private static RecipeTypeBuilder<ProcessingRecipe, ProcessingRecipe.Builder, Registrate> displayInput(String id) {
        return processing(id, DisplayInputRecipe::builder);
    }

    private static RecipeTypeBuilder<ProcessingRecipe, ProcessingRecipe.Builder, Registrate> processing(
        String id, SmartRecipeBuilder.Factory<ProcessingRecipe, ProcessingRecipe.Builder> builderFactory) {
        return REGISTRATE.recipeType(id, ProcessingRecipe.SERIALIZER)
            .clazz(ProcessingRecipe.class)
            .builder(builderFactory);
    }

    public static void init() {}
}
