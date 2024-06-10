package org.shsts.tinactory.datagen.content;

import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.MarkerRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.recipe.AssemblyRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.core.recipe.ToolRecipe;
import org.shsts.tinactory.registrate.common.RecipeFactory;

import static org.shsts.tinactory.datagen.DataGen.REGISTRATE;

public final class RecipeTypes {
    public static final RecipeFactory<ToolRecipe.Builder> TOOL;
    public static final RecipeFactory<ResearchRecipe.Builder> RESEARCH;
    public static final RecipeFactory<AssemblyRecipe.Builder> ASSEMBLER;
    public static final RecipeFactory<ProcessingRecipe.Builder> STONE_GENERATOR;
    public static final RecipeFactory<OreAnalyzerRecipe.Builder> ORE_ANALYZER;
    public static final RecipeFactory<ProcessingRecipe.Builder> MACERATOR;
    public static final RecipeFactory<ProcessingRecipe.Builder> ORE_WASHER;
    public static final RecipeFactory<ProcessingRecipe.Builder> CENTRIFUGE;
    public static final RecipeFactory<ProcessingRecipe.Builder> THERMAL_CENTRIFUGE;
    public static final RecipeFactory<ProcessingRecipe.Builder> ALLOY_SMELTER;
    public static final RecipeFactory<GeneratorRecipe.Builder> STEAM_TURBINE;
    public static final RecipeFactory<ProcessingRecipe.Builder> BLAST_FURNACE;
    public static final RecipeFactory<MarkerRecipe.Builder> MARKER;

    static {
        TOOL = AllRecipes.TOOL.factory(REGISTRATE);
        RESEARCH = AllRecipes.RESEARCH.factory(REGISTRATE);
        ASSEMBLER = AllRecipes.ASSEMBLER.factory(REGISTRATE);
        STONE_GENERATOR = AllRecipes.STONE_GENERATOR.factory(REGISTRATE);
        ORE_ANALYZER = AllRecipes.ORE_ANALYZER.factory(REGISTRATE);
        MACERATOR = AllRecipes.MACERATOR.factory(REGISTRATE);
        ORE_WASHER = AllRecipes.ORE_WASHER.factory(REGISTRATE);
        CENTRIFUGE = AllRecipes.CENTRIFUGE.factory(REGISTRATE);
        THERMAL_CENTRIFUGE = AllRecipes.THERMAL_CENTRIFUGE.factory(REGISTRATE);
        ALLOY_SMELTER = AllRecipes.ALLOY_SMELTER.factory(REGISTRATE);
        STEAM_TURBINE = AllRecipes.STEAM_TURBINE.factory(REGISTRATE);
        BLAST_FURNACE = AllRecipes.BLAST_FURNACE.factory(REGISTRATE);
        MARKER = AllRecipes.MARKER.factory(REGISTRATE);
    }
}
