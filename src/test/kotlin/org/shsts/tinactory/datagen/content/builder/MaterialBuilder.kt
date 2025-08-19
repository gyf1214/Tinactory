package org.shsts.tinactory.datagen.content.builder

import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.data.recipes.ShapelessRecipeBuilder
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.crafting.Ingredient
import net.minecraftforge.client.model.generators.ItemModelProvider
import org.shsts.tinactory.content.AllMaterials
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllRecipes.has
import org.shsts.tinactory.content.AllTags.TOOL_FILE
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_MORTAR
import org.shsts.tinactory.content.AllTags.TOOL_SAW
import org.shsts.tinactory.content.AllTags.TOOL_SCREW
import org.shsts.tinactory.content.AllTags.TOOL_SCREWDRIVER
import org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.MaterialSet
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.Models.VOID_TEX
import org.shsts.tinactory.datagen.content.Models.basicItem
import org.shsts.tinactory.datagen.content.Models.oreBlock
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.bender
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extruder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.fluidSolidifier
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.polarizer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.wiremill
import org.shsts.tinactory.datagen.content.model.IconSet
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext
import java.util.Optional
import java.util.function.Supplier
import kotlin.math.round

class MaterialBuilder(private val material: MaterialSet, private val icon: IconSet) {
    companion object {
        private val TOOL_HANDLE_TEX = mapOf(
            "hammer" to "handle_hammer",
            "mortar" to "mortar_base",
            "file" to "handle_file",
            "saw" to "handle_saw",
            "screwdriver" to "handle_screwdriver",
            "wire_cutter" to "wire_cutter_base")

        fun material(name: String, icon: IconSet, block: MaterialBuilder.() -> Unit) {
            val mat = getMaterial(name)
            val builder = MaterialBuilder(mat, icon)
            builder.block()
            builder.build()
        }
    }

    private val name = material.name
    private var hasProcess = false

    private fun <U : Item> toolModel(ctx: IEntryDataContext<Item, U, ItemModelProvider>, sub: String) {
        val category = sub.substring("tool/".length)
        val handle = Optional.ofNullable(TOOL_HANDLE_TEX[category])
            .map { gregtech("items/tools/$it") }
            .orElse(VOID_TEX)
        val head = gregtech("items/tools/$category")
        basicItem(ctx, handle, head)
    }

    private fun newItem(sub: String, tag: TagKey<Item>, entry: Supplier<out Item>) {
        val builder = DATA_GEN.item(material.loc(sub), entry).tag(tag)
        if (sub.startsWith("tool/")) {
            builder.model { toolModel(it, sub) }
        } else if (sub == "wire") {
            builder.model(Models::wireItem)
        } else if (sub == "pipe") {
            builder.model(Models::pipeItem)
        } else if (sub == "raw") {
            builder.model { basicItem(it, modLoc("items/material/raw")) }
        } else {
            builder.model { icon.itemModel(it, sub) }
        }
        builder.build()
    }

    private fun buildItem(sub: String) {
        val prefixTag = AllMaterials.tag(sub)
        val tag = material.tag(sub)
        DATA_GEN.tag(tag, prefixTag)

        if (material.isAlias(sub)) {
            // do nothing for alias
            return
        }

        if (material.hasTarget(sub)) {
            // simply add tag for existing tag
            DATA_GEN.tag(material.target(sub), tag)
            return
        }

        val entry = material.entry(sub)
        if (entry is IEntry<out Item>) {
            // build item data for new item
            newItem(sub, tag, entry)
        } else {
            // simple add tag for existing item
            DATA_GEN.tag(entry, tag)
        }
    }

    private fun buildOre() {
        val variant = material.oreVariant()
        val tierTag = variant.mineTier.tag!!
        DATA_GEN.block(material.blockLoc("ore"), material.blockEntry("ore"))
            .blockState { oreBlock(it, variant) }
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .tag(tierTag)
            .drop(material.entry("raw"))
            .build()
    }

    private inner class CraftingBuilder(
        val result: String, val amount: Int) {
        val patterns = mutableListOf<String>()
        val inputs = mutableListOf<TagKey<Item>>()
        val tools = mutableListOf<TagKey<Item>>()
        var valid = material.hasItem(result)

        fun pattern(vararg vals: String) {
            patterns.addAll(vals)
        }

        @Suppress("UNCHECKED_CAST")
        fun input(vararg vals: Any) {
            for (input in vals) {
                if (input is TagKey<*>) {
                    inputs.add(input as TagKey<Item>)
                } else if (input is String) {
                    if (material.hasItem(input)) {
                        inputs.add(material.tag(input))
                    } else {
                        valid = false
                    }
                } else {
                    throw IllegalArgumentException()
                }
            }
        }

        fun tool(vararg vals: TagKey<Item>) {
            tools.addAll(vals)
        }

        private fun buildVanilla() {
            DATA_GEN.vanillaRecipe {
                val builder = ShapedRecipeBuilder.shaped(material.item(result), amount)
                for (pattern in patterns) {
                    builder.pattern(pattern)
                }
                for ((i, input) in inputs.withIndex()) {
                    builder.define('A' + i, input)
                }
                builder.unlockedBy("has_material", has(inputs[0]))
                builder
            }
        }

        private fun buildTools() {
            toolCrafting(name, result, amount) {
                for (pattern in patterns) {
                    pattern(pattern)
                }
                for ((i, input) in inputs.withIndex()) {
                    define('A' + i, input)
                }
                toolTag(*tools.toTypedArray())
            }
        }

        fun build() {
            if (!valid) {
                return
            }
            if (tools.isEmpty()) {
                buildVanilla()
            } else {
                buildTools()
            }
        }
    }

    private fun crafting(result: String, amount: Int = 1, block: CraftingBuilder.() -> Unit) {
        val builder = CraftingBuilder(result, amount)
        builder.block()
        builder.build()
    }

    private fun crafting(result: String, input: String, tool: TagKey<Item>, amount: Int = 1) {
        crafting(result, amount) {
            input(input)
            tool(tool)
        }
    }

    private inner class ProcessBuilder(val voltage: Voltage, val factor: Double) {
        private fun ticks(value: Long) = round(value * factor).toLong()

        private fun valid(result: String, vararg inputs: String) = material.hasItem(result) &&
            inputs.all { material.hasItem(it) && material.loc(it) != material.loc(result) }

        private fun ProcessingRecipeFactory.process(result: String, amount: Int,
            input: String, workTicks: Long, suffix: String = "", inputAmount: Int = 1,
            voltage: Voltage = this@ProcessBuilder.voltage,
            block: ProcessingRecipeBuilder<ProcessingRecipe.Builder>.() -> Unit = {}) {
            if (!valid(result, input)) {
                return
            }

            outputMaterial(material, result, amount, suffix) {
                inputMaterial(material, input, inputAmount)
                voltage(voltage)
                workTicks(ticks(workTicks))
                block()
            }
        }

        private fun macerate(input: String, result: String, amount: Int) {
            macerator {
                process(result, amount, input, 128, "_from_$input")
            }
        }

        private fun macerate(input: String, amount: Int) {
            macerate(input, "dust", amount)
        }

        private fun macerate(input: String) {
            macerate(input, "dust", 1)
        }

        private fun macerateTiny(input: String, amount: Int) {
            macerate(input, "dust_tiny", amount)
        }

        private fun macerates() {
            macerate("primary")
            macerateTiny("nugget", 1)
            macerate("magnetic")
            macerateTiny("wire", 4)
            macerateTiny("wire_fine", 1)
            macerateTiny("ring", 2)
            macerate("plate")
            macerateTiny("foil", 2)
            macerateTiny("stick", 4)
            macerateTiny("screw", 1)
            macerateTiny("bolt", 1)
            macerate("gear")
            macerate("rotor", 4)
            macerate("pipe", 3)
            macerate("gem_flawless", 8)
            macerate("gem_exquisite", 16)
        }

        private fun molten(input: String, amount: Float, solidify: Boolean = true, result: String = "molten") {
            if (!material.hasItem(input) || !material.hasFluid(result)) {
                return
            }

            val v = if (material.hasItem("sheet")) voltage else Voltage.fromRank(voltage.rank + 1)

            extractor {
                outputMaterial(material, result, amount) {
                    inputMaterial(material, input)
                    voltage(v)
                    workTicks(ticks(160))
                }
            }

            if (solidify) {
                fluidSolidifier {
                    outputMaterial(material, input) {
                        inputMaterial(material, result, amount)
                        voltage(v)
                        workTicks(ticks(80))
                    }
                }
            }
        }

        private fun moltens() {
            molten("primary", 1f)
            molten("nugget", 1f / 9f)
            molten("magnetic", 0.5f, false)
            molten("wire", 0.5f, false)
            molten("wire_fine", 0.125f, false)
            molten("ring", 0.25f)
            molten("plate", 1f)
            molten("foil", 0.25f, false)
            molten("stick", 0.5f)
            molten("screw", 1f / 9f, false)
            molten("bolt", 0.125f)
            molten("gear", 2f)
            molten("rotor", 4.25f)
            molten("pipe", 3f)
        }

        private fun extrude(result: String, outCount: Int, inCount: Int) {
            val input: String
            val v: Voltage
            if (material.hasItem("sheet")) {
                input = "sheet"
                v = voltage
            } else {
                input = "ingot"
                v = Voltage.fromRank(voltage.rank + 1)
            }

            extruder {
                process(result, outCount, input, 96 * inCount.toLong(),
                    inputAmount = inCount, voltage = v)
            }
        }

        private fun extrudes() {
            extrude("stick", 2, 1)
            extrude("plate", 1, 1)
            extrude("foil", 4, 1)
            extrude("ring", 4, 1)
            extrude("wire", 2, 1)
            extrude("bolt", 8, 1)
            extrude("gear", 1, 2)
            extrude("rotor", 1, 5)
            extrude("pipe", 1, 3)
        }

        fun build() {
            polarizer {
                process("magnetic", 1, "stick", 40)
            }
            wiremill {
                process("wire", 2, "ingot", 48)
                process("wire_fine", 4, "wire", 64)
                process("ring", 1, "stick", 64)
            }
            bender {
                process("plate", 1, "ingot", 72)
                process("foil", 4, "plate", 40)
                process("foil", 4, "sheet", 40)
            }
            lathe {
                process("stick", 1, "ingot", 64)
                process("screw", 1, "bolt", 16)
                process("lens", 1, "gem_exquisite", 600)
            }
            cutter {
                process("bolt", 4, "stick", 64) {
                    inputMaterial("water", "liquid", 0.05)
                }
                process("gem", 8, "gem_flawless", 480) {
                    inputMaterial("water", "liquid", 0.8)
                }
            }

            macerates()
            moltens()
            extrudes()

            hasProcess = true
        }
    }

    fun machineProcess(voltage: Voltage, factor: Double = 1.0) {
        ProcessBuilder(voltage, factor).build()
    }

    fun toolProcess(factor: Double = 1.0) {
        // grind dust
        crafting("dust", "primary", TOOL_MORTAR)
        crafting("dust_tiny", "nugget", TOOL_MORTAR)
        // plate
        crafting("plate") {
            pattern("A", "A")
            input("ingot")
            tool(TOOL_HAMMER)
        }
        // foil
        crafting("foil", "plate", TOOL_HAMMER, 2)
        // ring
        crafting("ring", "stick", TOOL_HAMMER)
        crafting("ring", "sheet", TOOL_WIRE_CUTTER)
        // stick
        crafting("stick", "ingot", TOOL_FILE)
        // bolt
        crafting("bolt", "stick", TOOL_SAW, 2)
        // screw
        crafting("screw", "bolt", TOOL_FILE)
        // gear
        crafting("gear") {
            pattern("A", "B", "A")
            input("stick", "plate")
            tool(TOOL_HAMMER, TOOL_WIRE_CUTTER)
        }
        // rotor
        crafting("rotor") {
            pattern("A A", "BC ", "A A")
            input("plate", "stick", "screw")
            tool(TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER)
        }
        // cut wire
        crafting("wire", "plate", TOOL_WIRE_CUTTER)
        // pipe
        crafting("pipe") {
            pattern("AAA")
            input("plate")
            tool(TOOL_HAMMER, TOOL_WRENCH)
        }

        machineProcess(Voltage.LV, factor)
    }

    private fun dustWithTiny() {
        if (material.hasItem("dust") && material.hasItem("dust_tiny")) {
            DATA_GEN.vanillaRecipe {
                ShapelessRecipeBuilder
                    .shapeless(material.item("dust_tiny"), 9)
                    .requires(material.tag("dust"))
                    .unlockedBy("has_dust", has(material.tag("dust")))
            }
            DATA_GEN.vanillaRecipe {
                ShapelessRecipeBuilder
                    .shapeless(material.item("dust"))
                    .requires(Ingredient.of(material.tag("dust_tiny")), 9)
                    .unlockedBy("has_dust_small", has(material.tag("dust_tiny")))
            }
        }
    }

    private fun toolRecipes() {
        crafting("tool/hammer") {
            pattern("AA ", "AAB", "AA ")
            input("primary", TOOL_HANDLE)
        }
        crafting("tool/mortar") {
            pattern(" A ", "BAB", "BBB")
            input("primary", ItemTags.STONE_TOOL_MATERIALS)
        }
        crafting("tool/file") {
            pattern("A", "A", "B")
            input("plate", TOOL_HANDLE)
        }
        crafting("tool/saw") {
            pattern("AAB", "  B")
            input("plate", TOOL_HANDLE)
            tool(TOOL_FILE, TOOL_HAMMER)
        }
        crafting("tool/screwdriver") {
            pattern("  A", " A ", "B  ")
            input("stick", TOOL_HANDLE)
            tool(TOOL_FILE, TOOL_HAMMER)
        }
        crafting("tool/wrench") {
            pattern("A A", " A ", " A ")
            input("plate")
            tool(TOOL_HAMMER)
        }
        crafting("tool/wire_cutter") {
            pattern("A A", " A ", "BCB")
            input("plate", TOOL_HANDLE, TOOL_SCREW)
            tool(TOOL_HAMMER, TOOL_FILE, TOOL_SCREWDRIVER)
        }
    }

    fun build() {
        for (sub in material.itemSubs()) {
            buildItem(sub)
        }
        if (material.hasBlock("ore")) {
            buildOre()
        }
        dustWithTiny()
        toolRecipes()
    }
}
