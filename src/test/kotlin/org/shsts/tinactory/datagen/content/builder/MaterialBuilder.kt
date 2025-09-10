package org.shsts.tinactory.datagen.content.builder

import com.mojang.logging.LogUtils
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Block
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.Tags
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.TOOL_FILE
import org.shsts.tinactory.content.AllTags.TOOL_HAMMER
import org.shsts.tinactory.content.AllTags.TOOL_HANDLE
import org.shsts.tinactory.content.AllTags.TOOL_MORTAR
import org.shsts.tinactory.content.AllTags.TOOL_SAW
import org.shsts.tinactory.content.AllTags.TOOL_SCREW
import org.shsts.tinactory.content.AllTags.TOOL_SCREWDRIVER
import org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.content.AllTags.TOOL_WRENCH
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.material.MaterialSet
import org.shsts.tinactory.core.recipe.ProcessingRecipe
import org.shsts.tinactory.core.util.LocHelper.ae2
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.Models
import org.shsts.tinactory.datagen.content.Models.VOID_TEX
import org.shsts.tinactory.datagen.content.Models.basicItem
import org.shsts.tinactory.datagen.content.Models.oreBlock
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.builder.DataFactories.dataGen
import org.shsts.tinactory.datagen.content.builder.DataFactories.itemData
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.alloySmelter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.autoclave
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.bender
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.blastFurnace
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.centrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.cutter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extractor
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.extruder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.fluidSolidifier
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.lathe
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.macerator
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.mixer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.oreWasher
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.polarizer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.sifter
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.thermalCentrifuge
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vacuumFreezer
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.vanilla
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.wiremill
import org.shsts.tinactory.datagen.content.model.IconSet
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.context.IEntryDataContext
import kotlin.math.round

class MaterialBuilder(private val material: MaterialSet, private val icon: IconSet) {
    companion object {
        private val LOGGER = LogUtils.getLogger()

        private val TOOL_HANDLE_TEX = mapOf(
            "hammer" to "handle_hammer",
            "mortar" to "mortar_base",
            "file" to "handle_file",
            "saw" to "handle_saw",
            "screwdriver" to "handle_screwdriver",
            "wire_cutter" to "wire_cutter_base")

        private val EXISTING_TAGS = mapOf(
            Pair("glowstone", "dust") to Tags.Items.DUSTS_GLOWSTONE,
            Pair("iron", "ingot") to Tags.Items.INGOTS_IRON,
            Pair("iron", "nugget") to Tags.Items.NUGGETS_IRON,
            Pair("gold", "raw") to Tags.Items.RAW_MATERIALS_GOLD,
            Pair("gold", "ingot") to Tags.Items.INGOTS_GOLD,
            Pair("gold", "nugget") to Tags.Items.NUGGETS_GOLD,
            Pair("copper", "ingot") to Tags.Items.INGOTS_COPPER,
            Pair("redstone", "dust") to Tags.Items.DUSTS_REDSTONE,
            Pair("diamond", "gem") to Tags.Items.GEMS_DIAMOND,
            Pair("emerald", "gem") to Tags.Items.GEMS_EMERALD,
            Pair("nether_quartz", "primary") to Tags.Items.GEMS_QUARTZ)

        fun material(name: String, icon: IconSet, block: MaterialBuilder.() -> Unit = {}) {
            MaterialBuilder(getMaterial(name), icon).apply {
                block()
                build()
            }
        }
    }

    private val name = material.name
    private var hasProcess = false
    private var hasOreProcess = false

    private fun <U : Item> toolModel(ctx: IEntryDataContext<Item, U, ItemModelProvider>, sub: String) {
        val category = sub.substring("tool/".length)
        val handle = TOOL_HANDLE_TEX[category]?.let { gregtech("items/tools/$it") } ?: VOID_TEX
        val head = gregtech("items/tools/$category")
        basicItem(ctx, handle, head)
    }

    private fun newItem(sub: String, tag: TagKey<Item>, entry: IEntry<out Item>) {
        itemData(entry) {
            tag(tag)
            if (sub.startsWith("tool/")) {
                model { toolModel(it, sub) }
            } else if (sub == "wire") {
                model(Models::wireItem)
            } else if (sub == "pipe") {
                model(Models::pipeItem)
            } else if (sub == "raw") {
                model { basicItem(it, modLoc("items/material/raw")) }
            } else if (sub == "seed") {
                model { basicItem(it, ae2("items/crystal_seed_nether")) }
            } else {
                model { icon.itemModel(it, sub) }
            }
        }
    }

    private fun buildItem(sub: String) {
        val prefixTag = AllTags.material(sub)
        val tag = material.tag(sub)
        dataGen { tag(tag, prefixTag) }

        if (material.isAlias(sub)) {
            // do nothing for alias
            return
        }

        if (EXISTING_TAGS.containsKey(Pair(name, sub))) {
            dataGen { tag(EXISTING_TAGS.getValue(Pair(name, sub)), tag) }
            return
        }

        val entry = material.entry(sub)
        if (entry is IEntry<out Item>) {
            // build item data for new item
            newItem(sub, tag, entry)
        } else {
            // simple add tag for existing item
            dataGen { tag(entry, tag) }
        }
    }

    private fun buildOre() {
        val variant = material.oreVariant()
        val tierTag = variant.mineTier.tag!!
        blockData(material.blockEntry("ore") as IEntry<out Block>) {
            blockState { oreBlock(it, variant) }
            tag(BlockTags.MINEABLE_WITH_PICKAXE)
            tag(tierTag)
            drop(material.entry("raw"))
        }
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
                when (input) {
                    is TagKey<*> -> inputs.add(input as TagKey<Item>)
                    is String -> {
                        if (material.hasItem(input)) {
                            inputs.add(material.tag(input))
                        } else {
                            valid = false
                        }
                    }

                    else -> throw IllegalArgumentException()
                }
            }
        }

        fun tool(vararg vals: TagKey<Item>) {
            tools.addAll(vals)
        }

        private fun buildVanilla() {
            vanilla {
                shaped(material.item(result), amount) {
                    for (pattern in patterns) {
                        pattern(pattern)
                    }
                    for ((i, input) in inputs.withIndex()) {
                        define('A' + i, input)
                    }
                    unlockedBy("has_ingredient", inputs[0])
                }
            }
        }

        private fun buildTools() {
            toolCrafting {
                result(name, result, amount) {
                    for (pattern in patterns) {
                        pattern(pattern)
                    }
                    for ((i, input) in inputs.withIndex()) {
                        define('A' + i, input)
                    }
                    toolTag(*tools.toTypedArray())
                }
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
        CraftingBuilder(result, amount).apply {
            block()
            build()
        }
    }

    private fun crafting(result: String, input: String, tool: TagKey<Item>, amount: Int = 1) {
        crafting(result, amount) {
            pattern("A")
            input(input)
            tool(tool)
        }
    }

    private inner class ProcessBuilder(val voltage: Voltage, val factor: Double) {
        private fun ticks(value: Long) = round(value * factor).toLong()

        private fun valid(result: String, vararg inputs: String) = material.hasItem(result) &&
            inputs.all { material.hasItem(it) && material.loc(it) != material.loc(result) }

        private fun <B : ProcessingRecipe.BuilderBase<*, B>, RB : ProcessingRecipeBuilder<B>>
            RecipeFactory<B, RB>.process(result: String, input: String,
            workTicks: Long, amount: Int = 1, inputAmount: Int = 1, suffix: String = "",
            voltage: Voltage = this@ProcessBuilder.voltage,
            block: RB.() -> Unit = {}) {
            if (!valid(result, input)) {
                return
            }

            output(material, result, amount, suffix) {
                input(material, input, inputAmount)
                voltage(voltage)
                workTicks(ticks(workTicks))
                block()
            }
        }

        private fun ProcessingRecipeFactory.macerate(input: String,
            amount: Int = 1, output: String = "dust") {
            if (!valid(output, input)) {
                return
            }
            input(material, input) {
                output(material, output, amount)
            }
        }

        private fun ProcessingRecipeFactory.macerateTiny(input: String, amount: Int) {
            macerate(input, amount, "dust_tiny")
        }

        private fun macerates() {
            macerator {
                defaults {
                    voltage(this@ProcessBuilder.voltage)
                    workTicks(ticks(128))
                }
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
        }

        private fun molten(input: String, amount: Float, solidify: Boolean = true, output: String = "molten") {
            if (!material.hasItem(input) || !material.hasFluid(output)) {
                return
            }

            val v = if (material.hasItem("sheet")) voltage else Voltage.fromRank(voltage.rank + 1)

            extractor {
                input(material, input) {
                    output(material, output, amount)
                    voltage(v)
                    workTicks(ticks(160))
                }
            }

            if (solidify) {
                fluidSolidifier {
                    output(material, input) {
                        input(material, output, amount)
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

        private fun extrude(result: String, outAmount: Int, inAmount: Int) {
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
                process(result, input, 96 * inAmount.toLong(), outAmount,
                    inputAmount = inAmount, voltage = v)
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
                process("magnetic", "stick", 40)
            }
            wiremill {
                process("wire", "ingot", 48, 2)
                process("wire_fine", "wire", 64, 4)
                process("ring", "stick", 64)
            }
            bender {
                process("plate", "ingot", 72)
                process("foil", "plate", 40, 4)
                process("foil", "sheet", 40, 4)
            }
            lathe {
                process("stick", "ingot", 64)
                process("screw", "bolt", 16)
                process("lens", "gem_exquisite", 600)
                process("seed", "gem", 256)
            }
            mixer {
                process("seed", "dust", 64, 2) {
                    input(material, "seed")
                }
            }
            cutter {
                process("bolt", "stick", 64, 4) {
                    input("water", amount = 0.05)
                }
                process("gem", "gem_flawless", 480, 8) {
                    input("water", amount = 0.8)
                }
            }
            assembler {
                defaults {
                    input("soldering_alloy", "molten", 0.5)
                    tech(Technologies.SOLDERING)
                }
                process("gear", "plate", 128) {
                    input(material, "stick", 2)
                }
                process("rotor", "ring", 160) {
                    input(material, "plate", 4)
                }
                process("pipe", "plate", 120, inputAmount = 3)
            }
            assembler {
                process("gem_exquisite", "gem_flawless", 400) {
                    input(material, "gem", 4)
                    input(material, "dust", 4)
                    tech(Technologies.MATERIAL_CUTTING)
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

    private fun smelt(from: String, toMat: MaterialSet, to: String) {
        if (material.hasItem(from) && toMat.hasItem(to)) {
            val suffix = if (toMat == material) "" else "_from_${material.name}"
            vanilla {
                smelting(material.tag(from), toMat.item(to), 200, suffix)
            }
        }
    }

    fun smelt(toMat: String, to: String = "ingot") {
        smelt("dust", getMaterial(toMat), to)
    }

    fun smelt() {
        smelt("dust", material, "ingot")
        smelt("dust_tiny", material, "nugget")
    }

    inner class ComposeBuilder<B : ProcessingRecipe.BuilderBase<*, B>>(
        private val factory: ProcessingRecipeFactoryBase<B>,
        private val sub: String, private val suffix: String,
        private val voltage: Voltage, private val workTicks: Long,
        private val decompose: Boolean) {
        private var _builder: ProcessingRecipeBuilder<B>? = null
        private val builder get() = _builder!!
        private var inAmount = 0f
        private var outAmount: Float? = null

        fun amount(value: Number) {
            outAmount = value.toFloat()
        }

        fun component(mat: MaterialSet, amount: Number = 1, sub: String? = null) {
            if (decompose) {
                sub?.let { builder.output(mat, it, amount) } ?: builder.output(mat, amount = amount)
            } else {
                sub?.let { builder.input(mat, it, amount) } ?: builder.input(mat, amount = amount)
            }
            inAmount += amount.toFloat()
        }

        fun component(name: String, amount: Number = 1, sub: String? = null) {
            component(getMaterial(name), amount, sub)
        }

        fun extra(block: B.() -> Unit) {
            builder.extra(block)
        }

        fun build(block: ComposeBuilder<B>.() -> Unit) {
            factory.recipe(material, sub, suffix) {
                _builder = this
                block()
                val amount = outAmount ?: inAmount
                if (decompose) {
                    input(material, sub, amount)
                } else {
                    output(material, sub, amount)
                }
                workTicks((workTicks * amount).toLong())
                voltage(this@ComposeBuilder.voltage)
            }
        }
    }

    private fun <B : ProcessingRecipe.BuilderBase<*, B>> ProcessingRecipeFactoryBase<B>.compose(
        sub: String, voltage: Voltage, workTicks: Long, decompose: Boolean,
        suffix: String = "", block: ComposeBuilder<B>.() -> Unit) {
        ComposeBuilder(this, sub, suffix, voltage, workTicks, decompose).build(block)
    }

    fun centrifuge(voltage: Voltage,
        block: ComposeBuilder<ProcessingRecipe.Builder>.() -> Unit) {
        centrifuge {
            defaultItemSub = "dust"
            compose("dust", voltage, 60, true, block = block)
        }
    }

    fun mix(voltage: Voltage,
        block: ComposeBuilder<ProcessingRecipe.Builder>.() -> Unit) {
        mixer {
            compose("dust", voltage, 20, false, block = block)
        }
        centrifuge(voltage, block)
    }

    fun fluidMix(voltage: Voltage, sub: String = "fluid",
        block: ComposeBuilder<ProcessingRecipe.Builder>.() -> Unit) {
        mixer {
            compose(sub, voltage, 20, false, block = block)
        }
    }

    fun alloyOnly(voltage: Voltage, sub: String = "ingot",
        block: ComposeBuilder<ProcessingRecipe.Builder>.() -> Unit) {
        alloySmelter {
            compose(sub, voltage, 40, false, block = block)
        }
    }

    fun alloy(voltage: Voltage,
        block: ComposeBuilder<ProcessingRecipe.Builder>.() -> Unit) {
        alloyOnly(voltage, block = block)
        val v1 = if (voltage.rank < Voltage.LV.rank) Voltage.LV else voltage
        mix(v1, block)
    }

    fun fluidAlloy(voltage: Voltage,
        block: ComposeBuilder<ProcessingRecipe.Builder>.() -> Unit) {
        alloyOnly(voltage, "fluid", block)
    }

    fun blast(voltage: Voltage, temperature: Int, workTicks: Long, from: MaterialSet = material,
        block: ComposeBuilder<BlastFurnaceRecipe.Builder>.() -> Unit = {}) {
        val sub: String
        if (material.hasItem("ingot_hot")) {
            sub = "ingot_hot"
            vacuumFreezer {
                output(material, "ingot") {
                    input(material, "ingot_hot")
                    voltage(voltage)
                    workTicks(200)
                }
            }
        } else {
            sub = "ingot"
        }
        val suffix = if (from == material) "" else "_from_${from.name}"
        blastFurnace {
            compose(sub, voltage, workTicks, false, suffix) {
                amount(1)
                component(from, sub = "dust")
                extra {
                    temperature(temperature)
                }
                block()
            }
        }
    }

    fun blast(voltage: Voltage, temperature: Int, workTicks: Long, from: String,
        block: ComposeBuilder<BlastFurnaceRecipe.Builder>.() -> Unit = {}) {
        blast(voltage, temperature, workTicks, getMaterial(from), block)
    }

    fun crystallize(voltage: Voltage, workTicks: Long,
        baseCleanness: Double, normalCleanness: Double, idealCleanness: Double) {
        autoclave {
            defaults {
                voltage(voltage)
                workTicks(workTicks)
            }
            output(material, "gem") {
                input(material, "seed")
                extra {
                    requireCleanness(baseCleanness, normalCleanness)
                }
            }
            output(material, "gem", suffix = "_from_dust") {
                input(material, "dust")
                extra {
                    requireCleanness(baseCleanness, idealCleanness)
                }
            }
        }
    }

    inner class OreProcessBuilder {
        var amount = 1
        var primitive = false
        var siftAndHammer = false
        var siftPrimary = false
        private val variant = material.oreVariant()
        private val byProducts = mutableListOf<MaterialSet>()

        fun byProducts(vararg value: Any) {
            for (mat in value) {
                when (mat) {
                    is MaterialSet -> byProducts.add(mat)
                    is String -> byProducts.add(getMaterial(mat))
                    else -> throw IllegalArgumentException()
                }
            }
        }

        private fun byProduct(i: Int): MaterialSet {
            return if (byProducts.isEmpty()) {
                material
            } else if (i < byProducts.size) {
                byProducts[i]
            } else {
                byProducts[0]
            }
        }

        private fun crush(from: String, to: String) {
            val amount = if (from == "raw") 2 * amount else 1
            macerator {
                input(material, from) {
                    output(material, to, amount)
                    voltage(Voltage.LV)
                    workTicks((variant.destroyTime * 40).toLong())
                }
            }
        }

        private fun wash(from: String, to: String) {
            oreWasher {
                input(material, from) {
                    output(material, to)
                    if (from == "crushed") {
                        input("water")
                        output(material.oreVariant().material, "dust", port = 3)
                        output(byProduct(0), "dust", port = 4, rate = 0.3)
                        workTicks(200)
                    } else {
                        input("water", amount = 0.1)
                        workTicks(32)
                    }
                    if (primitive && from == "dust_impure") {
                        voltage(Voltage.PRIMITIVE)
                    } else {
                        voltage(Voltage.ULV)
                    }
                }
            }
        }

        fun build() {
            if (primitive || variant.voltage.rank <= Voltage.ULV.rank) {
                if (material.hasItem("gem")) {
                    siftAndHammer = true
                } else if (!siftAndHammer) {
                    crafting("crushed", "raw", TOOL_HAMMER, amount)
                }
                crafting("dust_pure", "crushed_purified", TOOL_HAMMER)
                crafting("dust_impure", "crushed", TOOL_HAMMER)
            }

            if (siftAndHammer) {
                crafting("primary", "raw", TOOL_HAMMER, amount)
            }

            crush("raw", "crushed")
            crush("crushed", "dust_impure")
            crush("crushed_purified", "dust_pure")
            crush("crushed_centrifuged", "dust")
            wash("crushed", "crushed_purified")
            wash("dust_impure", "dust")
            wash("dust_pure", "dust")

            centrifuge {
                input(material, "dust_pure") {
                    output(material, "dust")
                    output(byProduct(1), "dust", rate = 0.3)
                    voltage(Voltage.LV)
                    workTicks(80)
                }
            }

            thermalCentrifuge {
                input(material, "crushed_purified") {
                    output(material, "crushed_centrifuged")
                    output(byProduct(2), "dust", port = 2, rate = 0.4)
                }
            }

            sifter {
                if (material.hasItem("gem_flawless")) {
                    input(material, "crushed_purified") {
                        output(material, "gem_flawless", rate = 0.1)
                        output(material, "gem", rate = 0.35)
                        output(material, "dust_pure", rate = 0.65)
                        voltage(Voltage.LV)
                        workTicks(600)
                    }
                } else if (siftAndHammer || siftPrimary) {
                    input(material, "crushed_purified") {
                        output(material, "primary", rate = 0.8)
                        output(material, "primary", rate = 0.35)
                        output(material, "dust_pure", rate = 0.65)
                        voltage(Voltage.LV)
                        workTicks(400)
                    }
                }
            }

            hasOreProcess = true
        }
    }

    fun oreProcess(block: OreProcessBuilder.() -> Unit = {}) {
        OreProcessBuilder().apply {
            block()
            build()
        }
    }

    fun fluidOre(workTicks: Long, base: ItemLike, voltage: Voltage = Voltage.MV) {
        centrifuge {
            input(material, "raw") {
                output(base)
                output(material, "fluid")
                workTicks(workTicks)
                voltage(voltage)
            }
        }
    }

    private fun dustWithTiny() {
        if (material.hasItem("dust") && material.hasItem("dust_tiny")) {
            vanilla {
                shapeless(material.tag("dust"), material.item("dust_tiny"), toAmount = 9)
                shapeless(material.tag("dust_tiny"), material.item("dust"), fromAmount = 9)
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

        if (material.hasItem("primary") && !hasProcess) {
            LOGGER.warn("{} does not have process", material)
        }
        if (material.hasBlock("ore") && !hasOreProcess) {
            LOGGER.warn("{} does not have ore process", material)
        }
    }
}
