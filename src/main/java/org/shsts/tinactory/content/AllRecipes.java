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
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe;
import org.shsts.tinactory.content.recipe.CleanRecipe;
import org.shsts.tinactory.content.recipe.DistillationRecipe;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllRecipes {
    public static final IRecipeType<ToolRecipe.Builder> TOOL_CRAFTING;
    public static final IRecipeType<ResearchRecipe.Builder> RESEARCH_BENCH;
    public static final IRecipeType<AssemblyRecipe.Builder> ASSEMBLER;
    public static final IRecipeType<CleanRecipe.Builder> LASER_ENGRAVER;
    public static final IRecipeType<ProcessingRecipe.Builder> CIRCUIT_ASSEMBLER;
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
    public static final IRecipeType<ProcessingRecipe.Builder> ARC_FURNACE;
    public static final IRecipeType<ProcessingRecipe.Builder> STEAM_TURBINE;
    public static final IRecipeType<ProcessingRecipe.Builder> GAS_TURBINE;
    public static final IRecipeType<ProcessingRecipe.Builder> COMBUSTION_GENERATOR;
    public static final IRecipeType<BlastFurnaceRecipe.Builder> BLAST_FURNACE;
    public static final IRecipeType<ProcessingRecipe.Builder> VACUUM_FREEZER;
    public static final IRecipeType<ProcessingRecipe.Builder> DISTILLATION;
    public static final IRecipeType<ProcessingRecipe.Builder> AUTOFARM;
    public static final IRecipeType<ProcessingRecipe.Builder> PYROLYSE_OVEN;
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
            .register();

        ASSEMBLER = REGISTRATE.recipeType("assembler", AssemblyRecipe.Builder::new)
            .recipeClass(AssemblyRecipe.class)
            .serializer(AssemblyRecipe.SERIALIZER)
            .register();

        LASER_ENGRAVER = REGISTRATE.recipeType("laser_engraver", CleanRecipe.Builder::new)
            .recipeClass(CleanRecipe.class)
            .serializer(CleanRecipe.SERIALIZER)
            .register();

        CIRCUIT_ASSEMBLER = processing("circuit_assembler");

        ORE_ANALYZER = REGISTRATE.recipeType("ore_analyzer", OreAnalyzerRecipe.Builder::new)
            .recipeClass(OreAnalyzerRecipe.class)
            .serializer(OreAnalyzerRecipe.SERIALIZER)
            .register();

        MACERATOR = displayInput("macerator");
        ORE_WASHER = displayInput("ore_washer");
        CENTRIFUGE = displayInput("centrifuge");
        THERMAL_CENTRIFUGE = displayInput("thermal_centrifuge");
        SIFTER = displayInput("sifter");
        ALLOY_SMELTER = processing("alloy_smelter");
        MIXER = processing("mixer");
        POLARIZER = processing("polarizer");
        WIREMILL = processing("wiremill");
        BENDER = processing("bender");
        LATHE = processing("lathe");
        CUTTER = processing("cutter");
        EXTRUDER = processing("extruder");
        EXTRACTOR = displayInput("extractor");
        FLUID_SOLIDIFIER = processing("fluid_solidifier");
        ELECTROLYZER = displayInput("electrolyzer");

        CHEMICAL_REACTOR = REGISTRATE.recipeType("chemical_reactor", ChemicalReactorRecipe.Builder::new)
            .recipeClass(ChemicalReactorRecipe.class)
            .serializer(ChemicalReactorRecipe.SERIALIZER)
            .register();

        ARC_FURNACE = processing("arc_furnace");
        STEAM_TURBINE = processing("steam_turbine", GeneratorRecipe::builder);
        GAS_TURBINE = processing("gas_turbine", GeneratorRecipe::builder);
        COMBUSTION_GENERATOR = processing("combustion_generator", GeneratorRecipe::builder);

        BLAST_FURNACE = REGISTRATE.recipeType("blast_furnace", BlastFurnaceRecipe.Builder::new)
            .recipeClass(BlastFurnaceRecipe.class)
            .serializer(BlastFurnaceRecipe.SERIALIZER)
            .register();

        VACUUM_FREEZER = processing("vacuum_freezer");
        DISTILLATION = processing("distillation", DistillationRecipe::builder);
        AUTOFARM = processing("autofarm");
        PYROLYSE_OVEN = processing("pyrolyse_oven");

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

    private static IRecipeType<ProcessingRecipe.Builder> processing(String id) {
        return processing(id, ProcessingRecipe.Builder::new);
    }

    private static IRecipeType<ProcessingRecipe.Builder> displayInput(String id) {
        return processing(id, DisplayInputRecipe::builder);
    }

    public static void init() {}
}
