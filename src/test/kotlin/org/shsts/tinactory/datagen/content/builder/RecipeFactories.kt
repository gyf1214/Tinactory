package org.shsts.tinactory.datagen.content.builder

import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe
import org.shsts.tinactory.content.recipe.ChemicalReactorRecipe
import org.shsts.tinactory.content.recipe.CleanRecipe
import org.shsts.tinactory.content.recipe.GeneratorRecipe
import org.shsts.tinactory.content.recipe.MarkerRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.recipe.ResearchRecipe
import org.shsts.tinactory.core.recipe.ToolRecipe

typealias SimpleProcessingBuilder = ProcessingRecipeBuilder<ProcessingRecipe.Builder>
typealias ProcessingRecipeFactoryBase<B> = RecipeFactory<B, ProcessingRecipeBuilder<B>>
typealias ProcessingRecipeFactory = ProcessingRecipeFactoryBase<ProcessingRecipe.Builder>
typealias BlastFurnaceRecipeFactory = ProcessingRecipeFactoryBase<BlastFurnaceRecipe.Builder>
typealias ChemicalRecipeFactory = RecipeFactory<ChemicalReactorRecipe.Builder, ChemicalRecipeBuilder>
typealias MarkerFactory = RecipeFactory<MarkerRecipe.Builder, MarkerBuilder>

object RecipeFactories {
    fun vanilla(replace: Boolean = false, block: VanillaRecipeFactory.() -> Unit) {
        VanillaRecipeFactory(replace).apply(block)
    }

    fun toolCrafting(block: ToolRecipeFactory.() -> Unit) {
        ToolRecipeFactory().apply(block)
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>> processing(name: String,
        defaults: ProcessingRecipeBuilder<B>.() -> Unit):
        RecipeFactory<B, ProcessingRecipeBuilder<B>> {

        val recipeType = REGISTRATE.getRecipeType<B>(name)
        return RecipeFactory(recipeType, ::ProcessingRecipeBuilder, defaults)
    }

    private fun simpleProcessing(name: String,
        defaults: ProcessingRecipeBuilder<ProcessingRecipe.Builder>.() -> Unit) =
        processing(name, defaults)

    private fun assembly(name: String, defaults: AssemblyRecipeBuilder.() -> Unit):
        AssemblyRecipeFactory {
        val recipeType = REGISTRATE.getRecipeType<AssemblyRecipe.Builder>(name)
        return AssemblyRecipeFactory(recipeType, defaults)
    }

    fun rocket(block: ProcessingRecipeFactoryBase<ResearchRecipe.Builder>.() -> Unit) {
        processing<ResearchRecipe.Builder>("rocket") {
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

    fun laserEngraver(block: ProcessingRecipeFactoryBase<CleanRecipe.Builder>.() -> Unit) {
        processing<CleanRecipe.Builder>("laser_engraver") {
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

    fun stoneGenerator(block: AssemblyRecipeFactory.() -> Unit) {
        assembly("stone_generator") {
            defaultOutputItem = 0
            defaultOutputFluid = 1
            amperage = 0.125
            workTicks(20)
        }.block()
    }

    fun macerator(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("macerator") {
            simpleDefaults()
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
            simpleDefaults()
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
        val recipeType = REGISTRATE.getRecipeType<ChemicalReactorRecipe.Builder>("chemical_reactor")
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

    fun steamTurbine(block: ProcessingRecipeFactoryBase<GeneratorRecipe.Builder>.() -> Unit) {
        processing<GeneratorRecipe.Builder>("steam_turbine") {
            defaultInputFluid = 0
            defaultOutputFluid = 1
            amperage = 1.0
        }.block()
    }

    fun gasTurbine(block: ProcessingRecipeFactoryBase<GeneratorRecipe.Builder>.() -> Unit) {
        processing<GeneratorRecipe.Builder>("gas_turbine") {
            defaultInputFluid = 0
            amperage = 1.0
        }.block()
    }

    fun combustionGenerator(block: ProcessingRecipeFactoryBase<GeneratorRecipe.Builder>.() -> Unit) {
        processing<GeneratorRecipe.Builder>("combustion_generator") {
            defaultInputFluid = 0
            amperage = 1.0
        }.block()
    }

    fun blastFurnace(block: BlastFurnaceRecipeFactory.() -> Unit) {
        processing<BlastFurnaceRecipe.Builder>("blast_furnace") {
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

    fun autoclave(block: ProcessingRecipeFactoryBase<CleanRecipe.Builder>.() -> Unit) {
        processing<CleanRecipe.Builder>("autoclave") {
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

    fun marker(block: MarkerFactory.() -> Unit) {
        val recipeType = REGISTRATE.getRecipeType<MarkerRecipe.Builder>("marker")
        RecipeFactory(recipeType, ::MarkerBuilder).block()
    }

    fun ToolRecipe.Builder.define(ch: Char, mat: String, sub: String) {
        define(ch, getMaterial(mat).tag(sub))
    }
}
