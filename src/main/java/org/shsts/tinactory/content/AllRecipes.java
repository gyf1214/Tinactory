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
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.builder.IRecipeTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final IRecipeType<ToolRecipe.Builder> TOOL_CRAFTING;
    public static final IRecipeType<ResearchRecipe.Builder> RESEARCH_BENCH;
    public static final IRecipeType<AssemblyRecipe.Builder> ASSEMBLER;
    public static final IRecipeType<ProcessingRecipe.Builder> LASER_ENGRAVER;
    public static final IRecipeType<ProcessingRecipe.Builder> CIRCUIT_ASSEMBLER;
    public static final IRecipeType<ProcessingRecipe.Builder> STONE_GENERATOR;
    public static final IRecipeType<OreAnalyzerRecipe.Builder> ORE_ANALYZER;
    public static final IRecipeType<ProcessingRecipe.Builder> MACERATOR;
    public static final IRecipeType<ProcessingRecipe.Builder> ORE_WASHER;
    public static final IRecipeType<ProcessingRecipe.Builder> CENTRIFUGE;
    public static final IRecipeType<ProcessingRecipe.Builder> THERMAL_CENTRIFUGE;
    public static final IRecipeType<ProcessingRecipe.Builder> SIFTER;
    public static final IRecipeType<ProcessingRecipe.Builder> ALLOY_SMELTER;
    public static final IRecipeType<ProcessingRecipe.Builder> MIXER;
    public static final IRecipeType<ProcessingRecipe.Builder> POLARIZER;
    public static final IRecipeType<ProcessingRecipe.Builder> WIREMILL;
    public static final IRecipeType<ProcessingRecipe.Builder> BENDER;
    public static final IRecipeType<ProcessingRecipe.Builder> COMPRESSOR;
    public static final IRecipeType<ProcessingRecipe.Builder> LATHE;
    public static final IRecipeType<ProcessingRecipe.Builder> CUTTER;
    public static final IRecipeType<ProcessingRecipe.Builder> EXTRUDER;
    public static final IRecipeType<ProcessingRecipe.Builder> EXTRACTOR;
    public static final IRecipeType<ProcessingRecipe.Builder> FLUID_SOLIDIFIER;
    public static final IRecipeType<ProcessingRecipe.Builder> ELECTROLYZER;
    public static final IRecipeType<AssemblyRecipe.Builder> CHEMICAL_REACTOR;
    public static final IRecipeType<ProcessingRecipe.Builder> STEAM_TURBINE;
    public static final IRecipeType<BlastFurnaceRecipe.Builder> BLAST_FURNACE;
    public static final IRecipeType<ProcessingRecipe.Builder> VACUUM_FREEZER;
    // Recipes only used to mark input for recipe book purpose
    public static final IRecipeType<MarkerRecipe.Builder> MARKER;

    static {
        TOOL_CRAFTING = REGISTRATE.vanillaRecipeType("tool_crafting", ToolRecipe.Builder::new)
            .recipeClass(ToolRecipe.class)
            .serializer(ToolRecipe.SERIALIZER)
            .register();

        RESEARCH_BENCH = REGISTRATE.recipeType("research_bench", ResearchRecipe.Builder::new)
            .recipeClass(ResearchRecipe.class)
            .serializer(ResearchRecipe.SERIALIZER)
            .defaults($ -> $.amperage(0.25d).workTicks(200L))
            .register();

        ASSEMBLER = assembly("assembler")
            .defaults($ -> $.amperage(0.375d))
            .register();

        LASER_ENGRAVER = processing("laser_engraver")
            .defaults($ -> $.amperage(0.625d))
            .register();

        CIRCUIT_ASSEMBLER = processing("circuit_assembler")
            .defaults($ -> $.amperage(0.25d))
            .register();

        STONE_GENERATOR = processing("stone_generator")
            .defaults($ -> $.amperage(0.125d).workTicks(20L))
            .register();

        ORE_ANALYZER = REGISTRATE.recipeType("ore_analyzer", OreAnalyzerRecipe.Builder::new)
            .recipeClass(OreAnalyzerRecipe.class)
            .serializer(OreAnalyzerRecipe.SERIALIZER)
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
            .defaults($ -> $.voltage(Voltage.LV).workTicks(400L).amperage(1d))
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

        EXTRUDER = processing("extruder")
            .defaults($ -> $.amperage(0.625d))
            .register();

        EXTRACTOR = displayInput("extractor")
            .defaults($ -> $.amperage(0.5d))
            .register();

        FLUID_SOLIDIFIER = processing("fluid_solidifier")
            .defaults($ -> $.amperage(0.25d))
            .register();

        ELECTROLYZER = displayInput("electrolyzer")
            .defaults($ -> $.amperage(0.5d))
            .register();

        CHEMICAL_REACTOR = assembly("chemical_reactor")
            .defaults($ -> $.amperage(0.25d))
            .register();

        STEAM_TURBINE = processing("steam_turbine", GeneratorRecipe::builder)
            .defaults($ -> $.amperage(1d).workTicks(100))
            .register();

        BLAST_FURNACE = REGISTRATE.recipeType("blast_furnace", BlastFurnaceRecipe.Builder::new)
            .recipeClass(BlastFurnaceRecipe.class)
            .serializer(BlastFurnaceRecipe.SERIALIZER)
            .defaults($ -> $.amperage(4d))
            .register();

        VACUUM_FREEZER = processing("vacuum_freezer")
            .defaults($ -> $.amperage(0.75d))
            .register();

        MARKER = REGISTRATE.recipeType("marker", MarkerRecipe.Builder::new)
            .recipeClass(MarkerRecipe.class)
            .serializer(MarkerRecipe.SERIALIZER)
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

    private static IRecipeTypeBuilder<ProcessingRecipe,
        ProcessingRecipe.Builder, IRegistrate> processing(String id) {
        return processing(id, ProcessingRecipe.Builder::new);
    }

    private static IRecipeTypeBuilder<ProcessingRecipe,
        ProcessingRecipe.Builder, IRegistrate> displayInput(String id) {
        return processing(id, DisplayInputRecipe::builder);
    }

    private static IRecipeTypeBuilder<AssemblyRecipe,
        AssemblyRecipe.Builder, IRegistrate> assembly(String id) {
        return REGISTRATE.recipeType(id, AssemblyRecipe.Builder::new)
            .recipeClass(AssemblyRecipe.class)
            .serializer(AssemblyRecipe.SERIALIZER);
    }

    private static IRecipeTypeBuilder<ProcessingRecipe, ProcessingRecipe.Builder, IRegistrate> processing(
        String id, IRecipeType.BuilderFactory<ProcessingRecipe.Builder> builderFactory) {
        return REGISTRATE.recipeType(id, builderFactory)
            .recipeClass(ProcessingRecipe.class)
            .serializer(ProcessingRecipe.SERIALIZER);
    }

    public static void init() {}
}
