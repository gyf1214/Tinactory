package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe
import org.shsts.tinactory.content.recipe.CleanRecipe
import org.shsts.tinactory.content.recipe.GeneratorRecipe
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinycorelib.api.recipe.IRecipe
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

typealias ProcessingRecipeFactoryBase<R, B> = RecipeFactory<R, B>
typealias ProcessingRecipeFactory = RecipeFactory<ProcessingRecipe, SimpleProcessingBuilder>
typealias CleanRecipeFactory = RecipeFactory<CleanRecipe, CleanRecipeBuilder>
typealias GeneratorRecipeFactory = RecipeFactory<GeneratorRecipe, GeneratorRecipeBuilder>
typealias BlastFurnaceRecipeFactory = RecipeFactory<BlastFurnaceRecipe, BlastFurnaceBuilder>
typealias OreAnalyzerRecipeFactory = RecipeFactory<OreAnalyzerRecipe, OreAnalyzerRecipeBuilder>
typealias ChemicalRecipeFactory = RecipeFactory<ChemicalReactorRecipe, ChemicalRecipeBuilder>

object RecipeFactories {
    fun vanilla(replace: Boolean = false, block: VanillaRecipeFactory.() -> Unit) {
        VanillaRecipeFactory(replace).apply(block)
    }

    fun toolCrafting(block: ToolRecipeFactory.() -> Unit) {
        ToolRecipeFactory().apply(block)
    }

    fun boiler(block: BoilerRecipeFactory.() -> Unit) {
        BoilerRecipeFactory().apply(block)
    }

    private fun <R : IRecipe<*>> recipeType(name: String): IRecipeType<R> {
        return REGISTRATE.getRecipeType(name)
    }

    private fun processing(name: String, defaults: SimpleProcessingBuilder.() -> Unit):
        RecipeFactory<ProcessingRecipe, SimpleProcessingBuilder> {
        return RecipeFactory(recipeType(name), ::SimpleProcessingBuilder, defaults)
    }

    private fun clean(name: String, defaults: CleanRecipeBuilder.() -> Unit):
        CleanRecipeFactory {
        return RecipeFactory(recipeType(name), ::CleanRecipeBuilder, defaults)
    }

    private fun generator(name: String, defaults: GeneratorRecipeBuilder.() -> Unit):
        GeneratorRecipeFactory {
        return RecipeFactory(recipeType(name), ::GeneratorRecipeBuilder, defaults)
    }

    private fun assembly(name: String, defaults: SimpleAssemblyRecipeBuilder.() -> Unit):
        AssemblyRecipeFactory {
        return AssemblyRecipeFactory(recipeType(name), defaults)
    }

    fun research(block: ResearchRecipeFactory.() -> Unit) {
        ResearchRecipeFactory(recipeType("research_bench")) {
            defaultInputItem = 0
            amperage = 0.125
            workTicks(200)
        }.block()
    }

    fun rocket(block: ResearchRecipeFactory.() -> Unit) {
        ResearchRecipeFactory(recipeType("rocket")) {
            defaultInputItem = 0
            defaultInputFluid = 1
            amperage = 0.125
            workTicks(40)
        }.block()
    }

    fun assembler(block: AssemblyRecipeFactory.() -> Unit) {
        assembly("assembler") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.375
        }.block()
    }

    fun oreAnalyzer(block: OreAnalyzerRecipeFactory.() -> Unit) {
        RecipeFactory(recipeType("ore_analyzer"), ::OreAnalyzerRecipeBuilder) {
            simpleDefaults()
            amperage = 0.125
            workTicks(128)
        }.block()
    }

    fun laserEngraver(block: CleanRecipeFactory.() -> Unit) {
        clean("laser_engraver") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.625
        }.block()
    }

    fun circuitAssembler(block: ProcessingRecipeFactory.() -> Unit) {
        processing("circuit_assembler") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun assemblyLine(block: AssemblyRecipeFactory.() -> Unit) {
        assembly("assembly_line") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.75
        }.block()
    }

    fun stoneGenerator(block: AssemblyRecipeFactory.() -> Unit) {
        assembly("stone_generator") {
            defaultOutputItem = 1
            defaultOutputFluid = 2
            amperage = 0.125
            workTicks(20)
        }.block()
    }

    fun macerator(block: ProcessingRecipeFactory.() -> Unit) {
        processing("macerator") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun oreWasher(block: ProcessingRecipeFactory.() -> Unit) {
        processing("ore_washer") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.125
        }.block()
    }

    fun centrifuge(block: ProcessingRecipeFactory.() -> Unit) {
        processing("centrifuge") {
            fullDefaults()
            amperage = 0.5
        }.block()
    }

    fun thermalCentrifuge(block: ProcessingRecipeFactory.() -> Unit) {
        processing("thermal_centrifuge") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 1.0
            voltage(Voltage.LV)
        }.block()
    }

    fun sifter(block: ProcessingRecipeFactory.() -> Unit) {
        processing("sifter") {
            simpleDefaults()
            amperage = 0.25
        }.block()
    }

    fun alloySmelter(block: ProcessingRecipeFactory.() -> Unit) {
        processing("alloy_smelter") {
            defaultInputItem = 0
            defaultOutputItem = 2
            defaultOutputFluid = 3
            amperage = 0.75
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun mixer(block: ProcessingRecipeFactory.() -> Unit) {
        processing("mixer") {
            fullDefaults()
            amperage = 0.5
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun polarizer(block: ProcessingRecipeFactory.() -> Unit) {
        processing("polarizer") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun wiremill(block: ProcessingRecipeFactory.() -> Unit) {
        processing("wiremill") {
            simpleDefaults()
            amperage = 0.25
        }.block()
    }

    fun bender(block: ProcessingRecipeFactory.() -> Unit) {
        processing("bender") {
            simpleDefaults()
            amperage = 0.25
        }.block()
    }

    fun lathe(block: ProcessingRecipeFactory.() -> Unit) {
        processing("lathe") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.375
        }.block()
    }

    fun cutter(block: ProcessingRecipeFactory.() -> Unit) {
        processing("cutter") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.375
        }.block()
    }

    fun extruder(block: ProcessingRecipeFactory.() -> Unit) {
        processing("extruder") {
            simpleDefaults()
            amperage = 0.625
        }.block()
    }

    fun extractor(block: ProcessingRecipeFactory.() -> Unit) {
        processing("extractor") {
            defaultInputItem = 0
            defaultOutputItem = 2
            defaultOutputFluid = 3
            amperage = 0.5
        }.block()
    }

    fun fluidSolidifier(block: ProcessingRecipeFactory.() -> Unit) {
        processing("fluid_solidifier") {
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun electrolyzer(block: ProcessingRecipeFactory.() -> Unit) {
        processing("electrolyzer") {
            fullDefaults()
            amperage = 0.75
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun chemicalReactor(block: ChemicalRecipeFactory.() -> Unit) {
        RecipeFactory(recipeType("chemical_reactor"), ::ChemicalRecipeBuilder) {
            fullDefaults()
            amperage = 0.375
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun arcFurnace(block: ProcessingRecipeFactory.() -> Unit) {
        processing("arc_furnace") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 1.0
        }.apply {
            defaultItemSub = "ingot"
            block()
        }
    }

    fun steamTurbine(block: GeneratorRecipeFactory.() -> Unit) {
        generator("steam_turbine") {
            defaultInputFluid = 0
            defaultOutputFluid = 1
            amperage = 1.0
        }.block()
    }

    fun gasTurbine(block: GeneratorRecipeFactory.() -> Unit) {
        generator("gas_turbine") {
            defaultInputFluid = 0
            amperage = 1.0
        }.block()
    }

    fun combustionGenerator(block: GeneratorRecipeFactory.() -> Unit) {
        generator("combustion_generator") {
            defaultInputFluid = 0
            amperage = 1.0
        }.block()
    }

    fun blastFurnace(block: BlastFurnaceRecipeFactory.() -> Unit) {
        RecipeFactory(recipeType("blast_furnace"), ::BlastFurnaceBuilder) {
            fullDefaults()
            amperage = 4.0
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun vacuumFreezer(block: ProcessingRecipeFactory.() -> Unit) {
        processing("vacuum_freezer") {
            fullDefaults()
            amperage = 1.5
        }.block()
    }

    fun distillation(block: ProcessingRecipeFactory.() -> Unit) {
        processing("distillation") {
            defaultInputFluid = 0
            defaultOutputFluid = 1
            defaultOutputItem = 2
            amperage = 2.5
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun autofarm(block: ProcessingRecipeFactory.() -> Unit) {
        processing("autofarm") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 3
            amperage = 0.25
        }.block()
    }

    fun pyrolyseOven(block: ProcessingRecipeFactory.() -> Unit) {
        processing("pyrolyse_oven") {
            fullDefaults()
            amperage = 2.0
        }.apply {
            defaultItemSub = "primary"
            block()
        }
    }

    fun implosionCompressor(block: ProcessingRecipeFactory.() -> Unit) {
        processing("implosion_compressor") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.125
            workTicks(10)
        }.block()
    }

    fun autoclave(block: CleanRecipeFactory.() -> Unit) {
        clean("autoclave") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 1.5
        }.block()
    }

    fun oilCracking(block: ProcessingRecipeFactory.() -> Unit) {
        processing("oil_cracking") {
            defaultInputFluid = 0
            defaultOutputFluid = 2
            amperage = 2.5
        }.block()
    }

    fun fusionReactor(block: ProcessingRecipeFactory.() -> Unit) {
        processing("fusion_reactor") {
            defaultInputFluid = 0
            defaultOutputFluid = 1
            amperage = 0.75
        }.block()
    }

    fun marker(block: MarkerFactory.() -> Unit) {
        MarkerFactory().block()
    }
}
