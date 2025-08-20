package org.shsts.tinactory.datagen.content.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import org.shsts.tinactory.Tinactory.REGISTRATE
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe
import org.shsts.tinactory.core.recipe.AssemblyRecipe
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.recipe.ToolRecipe
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

typealias ProcessingRecipeFactoryBase<B> = RecipeFactory<B, ProcessingRecipeBuilder<B>>
typealias ProcessingRecipeFactory = ProcessingRecipeFactoryBase<ProcessingRecipe.Builder>
typealias BlastFurnaceRecipeFactory = ProcessingRecipeFactoryBase<BlastFurnaceRecipe.Builder>
typealias AssemblyRecipeFactory = RecipeFactory<AssemblyRecipe.Builder, AssemblyRecipeBuilder>

object RecipeFactories {
    fun vanilla(replace: Boolean = false, block: VanillaRecipeFactory.() -> Unit) {
        VanillaRecipeFactory(replace).apply(block)
    }

    fun toolCrafting(loc: ResourceLocation, block: ToolRecipe.Builder.() -> Unit) {
        TOOL_CRAFTING.recipe(DATA_GEN, loc).apply {
            block()
            build()
        }
    }

    fun toolCrafting(result: ItemLike, amount: Int = 1, block: ToolRecipe.Builder.() -> Unit) {
        toolCrafting(result.asItem().registryName!!) {
            result({ result }, amount)
            block()
        }
    }

    fun toolCrafting(name: String, sub: String, amount: Int = 1, block: ToolRecipe.Builder.() -> Unit) {
        val mat = getMaterial(name)
        toolCrafting(mat.loc(sub)) {
            result(mat.entry(sub), amount)
            block()
        }
    }

    fun toolShapeless(from: ItemLike, to: ItemLike, tool: TagKey<Item>, amount: Int = 1) {
        toolCrafting(to, amount) {
            pattern("#")
            define('#') { from }
            toolTag(tool)
        }
    }

    fun toolShapeless(from: TagKey<Item>, to: ItemLike, tool: TagKey<Item>, amount: Int = 1) {
        toolCrafting(to, amount) {
            pattern("#")
            define('#', from)
            toolTag(tool)
        }
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

    fun assembler(block: AssemblyRecipeFactory.() -> Unit) {
        val recipeType = REGISTRATE.getRecipeType<AssemblyRecipe.Builder>("assembler")
        RecipeFactory(recipeType, ::AssemblyRecipeBuilder) {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.375
        }.block()
    }

    fun stoneGenerator(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("stone_generator") {
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
            simpleDefaults()
            amperage = 1.0
            voltage(Voltage.LV)
            workTicks(400)
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
            defaultOutputItem = 1
            defaultOutputFluid = 2
            amperage = 0.5
        }.block()
    }

    fun mixer(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("mixer") {
            fullDefaults()
            amperage = 0.5
        }.block()
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
            simpleDefaults()
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
            defaultOutputItem = 1
            defaultOutputFluid = 2
            amperage = 0.5
        }.block()
    }

    fun fluidSolidifier(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("fluid_solidifier") {
            defaultInputFluid = 0
            defaultOutputItem = 1
            amperage = 0.25
        }.block()
    }

    fun blastFurnace(block: BlastFurnaceRecipeFactory.() -> Unit) {
        processing<BlastFurnaceRecipe.Builder>("blast_furnace") {
            fullDefaults()
            amperage = 4.0
        }.block()
    }

    fun vacuumFreezer(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("vacuum_freezer") {
            fullDefaults()
            amperage = 1.5
        }.block()
    }

    fun autofarm(block: ProcessingRecipeFactory.() -> Unit) {
        simpleProcessing("autofarm") {
            defaultInputItem = 0
            defaultInputFluid = 1
            defaultOutputItem = 2
            amperage = 0.25
        }.block()
    }
}
