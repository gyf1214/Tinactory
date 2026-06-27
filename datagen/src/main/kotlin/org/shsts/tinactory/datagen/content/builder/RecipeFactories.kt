package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe
import org.shsts.tinactory.content.recipe.CleanRecipe
import org.shsts.tinactory.content.recipe.GeneratorRecipe
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.core.recipe.MarkerRecipe
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.recipe.ResearchRecipe
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType

typealias ProcessingRecipeFactoryBase<R, B> = RecipeFactory<R, B>
typealias ProcessingRecipeFactory = RecipeFactory<ProcessingRecipe, SimpleProcessingBuilder>
typealias ResearchRecipeFactory = RecipeFactory<ResearchRecipe, ResearchRecipeBuilder>
typealias CleanRecipeFactory = RecipeFactory<CleanRecipe, CleanRecipeBuilder>
typealias GeneratorRecipeFactory = RecipeFactory<GeneratorRecipe, GeneratorRecipeBuilder>
typealias BlastFurnaceRecipeFactory = RecipeFactory<BlastFurnaceRecipe, BlastFurnaceBuilder>
typealias OreAnalyzerRecipeFactory = RecipeFactory<OreAnalyzerRecipe, OreAnalyzerRecipeBuilder>
typealias ChemicalRecipeFactory = RecipeFactory<ChemicalReactorRecipe, ChemicalRecipeBuilder>
typealias MarkerFactory = RecipeFactory<MarkerRecipe, MarkerBuilder>

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

    private fun processing(name: String, defaults: SimpleProcessingBuilder.() -> Unit):
        RecipeFactory<ProcessingRecipe, SimpleProcessingBuilder> {
        val recipeType = REGISTRATE.getRecipeType(name) as IRecipeType<ProcessingRecipe>
        return RecipeFactory(recipeType, ::SimpleProcessingBuilder, defaults)
    }

    private fun clean(name: String, defaults: CleanRecipeBuilder.() -> Unit):
        CleanRecipeFactory {
        val recipeType = REGISTRATE.getRecipeType(name) as IRecipeType<CleanRecipe>
        return RecipeFactory(recipeType, ::CleanRecipeBuilder, defaults)
    }

    private fun generator(name: String, defaults: GeneratorRecipeBuilder.() -> Unit):
        GeneratorRecipeFactory {
        val recipeType = REGISTRATE.getRecipeType(name) as IRecipeType<GeneratorRecipe>
        return RecipeFactory(recipeType, ::GeneratorRecipeBuilder, defaults)
    }

    private fun simpleProcessing(name: String,
        defaults: SimpleProcessingBuilder.() -> Unit) =
        processing(name, defaults)

    private fun assembly(name: String, defaults: SimpleAssemblyRecipeBuilder.() -> Unit):
        AssemblyRecipeFactory {
        val recipeType = REGISTRATE.getRecipeType(name) as IRecipeType<AssemblyRecipe>
        return AssemblyRecipeFactory(recipeType, defaults)
    }

    fun rocket(block: ResearchRecipeFactory.() -> Unit) {
        val recipeType = REGISTRATE.getRecipeType("rocket") as IRecipeType<ResearchRecipe>
        RecipeFactory(recipeType, ::ResearchRecipeBuilder) {
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

    fun laserEngraver(block: CleanRecipeFactory.() -> Unit) {
        clean("laser_engraver") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.625
        }.block()
    }

    fun circuitAssembler(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("circuit_assembler") {
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
        simpleProcessing("macerator") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun oreWasher(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("ore_washer") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.125
        }.block()
    }

    fun centrifuge(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("centrifuge") {
            fullDefaults()
            amperage = 0.5
        }.block()
    }

    fun thermalCentrifuge(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("thermal_centrifuge") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 1.0
            voltage(Voltage.LV)
        }.block()
    }

    fun sifter(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("sifter") {
            simpleDefaults()
            amperage = 0.25
        }.block()
    }

    fun alloySmelter(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("alloy_smelter") {
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
        simpleProcessing("mixer") {
            fullDefaults()
            amperage = 0.5
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun polarizer(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("polarizer") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun wiremill(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("wiremill") {
            simpleDefaults()
            amperage = 0.25
        }.block()
    }

    fun bender(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("bender") {
            simpleDefaults()
            amperage = 0.25
        }.block()
    }

    fun lathe(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("lathe") {
            defaultInputItem = 0
            defaultOutputItem = 2
            amperage = 0.375
        }.block()
    }

    fun cutter(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("cutter") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.375
        }.block()
    }

    fun extruder(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("extruder") {
            simpleDefaults()
            amperage = 0.625
        }.block()
    }

    fun extractor(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("extractor") {
            defaultInputItem = 0
            defaultOutputItem = 2
            defaultOutputFluid = 3
            amperage = 0.5
        }.block()
    }

    fun fluidSolidifier(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("fluid_solidifier") {
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }

    fun electrolyzer(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("electrolyzer") {
            fullDefaults()
            amperage = 0.75
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun chemicalReactor(block: ChemicalRecipeFactory.() -> Unit) {
        val recipeType = REGISTRATE.getRecipeType("chemical_reactor") as IRecipeType<ChemicalReactorRecipe>
        RecipeFactory(recipeType, ::ChemicalRecipeBuilder) {
            fullDefaults()
            amperage = 0.375
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun arcFurnace(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("arc_furnace") {
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
        val recipeType = REGISTRATE.getRecipeType("blast_furnace") as IRecipeType<BlastFurnaceRecipe>
        RecipeFactory(recipeType, ::BlastFurnaceBuilder) {
            fullDefaults()
            amperage = 4.0
        }.apply {
            defaultItemSub = "dust"
            block()
        }
    }

    fun vacuumFreezer(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("vacuum_freezer") {
            fullDefaults()
            amperage = 1.5
        }.block()
    }

    fun distillation(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("distillation") {
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
        simpleProcessing("autofarm") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 3
            amperage = 0.25
        }.block()
    }

    fun pyrolyseOven(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("pyrolyse_oven") {
            fullDefaults()
            amperage = 2.0
        }.apply {
            defaultItemSub = "primary"
            block()
        }
    }

    fun implosionCompressor(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("implosion_compressor") {
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
        simpleProcessing("oil_cracking") {
            defaultInputFluid = 0
            defaultOutputFluid = 2
            amperage = 2.5
        }.block()
    }

    fun fusionReactor(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("fusion_reactor") {
            defaultInputFluid = 0
            defaultOutputFluid = 1
            amperage = 0.75
        }.block()
    }

    fun marker(block: MarkerFactory.() -> Unit) {
        val recipeType = REGISTRATE.getRecipeType("marker") as IRecipeType<MarkerRecipe>
        RecipeFactory(recipeType, ::MarkerBuilder).block()
    }
}
