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
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.DisplayInputRecipe;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.LaserEngravingRecipe;
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
    public static final IRecipeType<LaserEngravingRecipe.Builder> LASER_ENGRAVER;
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
    public static final IRecipeType<ProcessingRecipe.Builder> LATHE;
    public static final IRecipeType<ProcessingRecipe.Builder> CUTTER;
    public static final IRecipeType<ProcessingRecipe.Builder> EXTRUDER;
    public static final IRecipeType<ProcessingRecipe.Builder> EXTRACTOR;
    public static final IRecipeType<ProcessingRecipe.Builder> FLUID_SOLIDIFIER;
    public static final IRecipeType<ProcessingRecipe.Builder> ELECTROLYZER;
    public static final IRecipeType<ChemicalReactorRecipe.Builder> CHEMICAL_REACTOR;
    public static final IRecipeType<ProcessingRecipe.Builder> STEAM_TURBINE;
    public static final IRecipeType<ProcessingRecipe.Builder> GAS_TURBINE;
    public static final IRecipeType<ProcessingRecipe.Builder> COMBUSTION_GENERATOR;
    public static final IRecipeType<BlastFurnaceRecipe.Builder> BLAST_FURNACE;
    public static final IRecipeType<ProcessingRecipe.Builder> VACUUM_FREEZER;
    public static final IRecipeType<ProcessingRecipe.Builder> DISTILLATION;
    public static final IRecipeType<ProcessingRecipe.Builder> AUTOFARM;
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
            .defaults($ -> $.amperage(0.25d).workTicks(200L).defaultInputItem(0))
            .register();

        ASSEMBLER = REGISTRATE.recipeType("assembler", AssemblyRecipe.Builder::new)
            .recipeClass(AssemblyRecipe.class)
            .serializer(AssemblyRecipe.SERIALIZER)
            .defaults($ -> $.amperage(0.375d).defaultInputItem(0).defaultInputFluid(1).defaultOutputItem(2))
            .register();

        LASER_ENGRAVER = REGISTRATE.recipeType("laser_engraver", LaserEngravingRecipe.Builder::new)
            .recipeClass(LaserEngravingRecipe.class)
            .serializer(LaserEngravingRecipe.SERIALIZER)
            .defaults($ -> $.amperage(0.625d).defaultOutputItem(2))
            .register();

        CIRCUIT_ASSEMBLER = processing("circuit_assembler")
            .defaults($ -> $.amperage(0.25d).defaultInputItem(0).defaultInputFluid(1).defaultOutputItem(2))
            .register();

        STONE_GENERATOR = processing("stone_generator")
            .defaults($ -> $.amperage(0.125d).workTicks(20L).defaultOutputItem(0).defaultOutputFluid(1))
            .register();

        ORE_ANALYZER = REGISTRATE.recipeType("ore_analyzer", OreAnalyzerRecipe.Builder::new)
            .recipeClass(OreAnalyzerRecipe.class)
            .serializer(OreAnalyzerRecipe.SERIALIZER)
            .defaults($ -> $.amperage(0.125d).workTicks(32L).transform(AllRecipes::simpleDefaults))
            .register();

        MACERATOR = displayInput("macerator")
            .defaults($ -> $.amperage(0.25d).transform(AllRecipes::simpleDefaults))
            .register();

        ORE_WASHER = displayInput("ore_washer")
            .defaults($ -> $.amperage(0.125d).defaultInputItem(0).defaultInputFluid(1))
            .register();

        CENTRIFUGE = displayInput("centrifuge")
            .defaults($ -> $.amperage(0.5d).transform(AllRecipes::fullDefaults))
            .register();

        THERMAL_CENTRIFUGE = displayInput("thermal_centrifuge")
            .defaults($ -> $.voltage(Voltage.LV).workTicks(400L).amperage(1d).defaultInputItem(0))
            .register();

        SIFTER = displayInput("sifter")
            .defaults($ -> $.amperage(0.25d).transform(AllRecipes::simpleDefaults))
            .register();

        ALLOY_SMELTER = processing("alloy_smelter")
            .defaults($ -> $.amperage(0.75d).defaultInputItem(0).defaultOutputItem(1).defaultOutputFluid(2))
            .register();

        MIXER = processing("mixer")
            .defaults($ -> $.amperage(0.5d).transform(AllRecipes::fullDefaults))
            .register();

        POLARIZER = simpleProcessing("polarizer", 0.25d);
        WIREMILL = simpleProcessing("wiremill", 0.25d);
        BENDER = simpleProcessing("bender", 0.25d);
        LATHE = simpleProcessing("lathe", 0.375d);

        CUTTER = processing("cutter")
            .defaults($ -> $.amperage(0.375d).defaultInputItem(0).defaultInputFluid(1).defaultOutputItem(2))
            .register();

        EXTRUDER = simpleProcessing("extruder", 0.625d);

        EXTRACTOR = displayInput("extractor")
            .defaults($ -> $.amperage(0.5d).defaultInputItem(0).defaultOutputItem(1).defaultOutputFluid(2))
            .register();

        FLUID_SOLIDIFIER = processing("fluid_solidifier")
            .defaults($ -> $.amperage(0.25d).defaultInputFluid(0).defaultOutputItem(1))
            .register();

        ELECTROLYZER = displayInput("electrolyzer")
            .defaults($ -> $.amperage(0.75d).transform(AllRecipes::fullDefaults))
            .register();

        CHEMICAL_REACTOR = REGISTRATE.recipeType("chemical_reactor", ChemicalReactorRecipe.Builder::new)
            .recipeClass(ChemicalReactorRecipe.class)
            .serializer(ChemicalReactorRecipe.SERIALIZER)
            .defaults($ -> $.amperage(0.375d).transform(AllRecipes::fullDefaults))
            .register();

        STEAM_TURBINE = processing("steam_turbine", GeneratorRecipe::builder)
            .defaults($ -> $.amperage(1d).defaultInputFluid(0).defaultOutputFluid(1))
            .register();

        GAS_TURBINE = processing("gas_turbine", GeneratorRecipe::builder)
            .defaults($ -> $.amperage(1d).defaultInputFluid(0))
            .register();

        COMBUSTION_GENERATOR = processing("combustion_generator", GeneratorRecipe::builder)
            .defaults($ -> $.amperage(1d).defaultInputFluid(0))
            .register();

        BLAST_FURNACE = REGISTRATE.recipeType("blast_furnace", BlastFurnaceRecipe.Builder::new)
            .recipeClass(BlastFurnaceRecipe.class)
            .serializer(BlastFurnaceRecipe.SERIALIZER)
            .defaults($ -> $.amperage(4d).transform(AllRecipes::fullDefaults))
            .register();

        VACUUM_FREEZER = processing("vacuum_freezer")
            .defaults($ -> $.amperage(1.5d).transform(AllRecipes::fullDefaults))
            .register();

        DISTILLATION = processing("distillation", DistillationRecipe::builder)
            .defaults($ -> $.amperage(2.5d).defaultInputFluid(0).defaultOutputFluid(1).defaultOutputItem(2))
            .register();

        AUTOFARM = processing("autofarm")
            .defaults($ -> $.amperage(0.25d).defaultInputItem(0).defaultInputFluid(1).defaultOutputItem(3))
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

    private static IRecipeTypeBuilder<ProcessingRecipe, ProcessingRecipe.Builder, IRegistrate> processing(
        String id, IRecipeType.BuilderFactory<ProcessingRecipe.Builder> builderFactory) {
        return REGISTRATE.recipeType(id, builderFactory)
            .recipeClass(ProcessingRecipe.class)
            .serializer(ProcessingRecipe.SERIALIZER);
    }

    private static IRecipeType<ProcessingRecipe.Builder> simpleProcessing(String id, double amperage) {
        return processing(id)
            .defaults($ -> $.amperage(amperage).transform(AllRecipes::simpleDefaults))
            .register();
    }

    private static <S extends ProcessingRecipe.BuilderBase<?, S>> S simpleDefaults(S builder) {
        return builder.defaultInputItem(0).defaultOutputItem(1);
    }

    private static <S extends ProcessingRecipe.BuilderBase<?, S>> S fullDefaults(S builder) {
        return builder.defaultInputItem(0).defaultInputFluid(1)
            .defaultOutputItem(2).defaultOutputFluid(3);
    }

    public static void init() {}
}
