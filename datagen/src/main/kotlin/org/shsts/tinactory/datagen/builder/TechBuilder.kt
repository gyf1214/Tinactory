package org.shsts.tinactory.datagen.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.common.data.ExistingFileHelper
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.core.builder.Builder
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.tech.Technology
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.RegistryHelper
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.research
import org.shsts.tinactory.datagen.provider.TechProvider
import org.shsts.tinycorelib.api.core.ILoc
import org.shsts.tinycorelib.datagen.api.IDataHandler
import java.util.Optional
import java.util.function.Supplier

class TechBuilder<P>(parent: P, private val loc: ResourceLocation) :
    Builder<Technology, P, TechBuilder<P>>(parent), ILoc {
    private val depends = mutableListOf<ResourceLocation>()
    private val modifiers = mutableMapOf<String, Int>()
    private var maxProgress = 0L
    private var rank = 0
    private var displayItem: (() -> ResourceLocation)? = null
    private var displayTexture: ResourceLocation? = null
    private var voltage: Voltage? = null
    private var noResearch = false

    fun depends(vararg loc: ResourceLocation) {
        depends += loc
    }

    fun maxProgress(value: Long) {
        maxProgress = value
    }

    fun displayItem(loc: ResourceLocation) {
        displayItem = { loc }
    }

    fun displayItem(id: String) {
        displayItem = { modLoc(id) }
    }

    fun displayItem(item: ItemLike) {
        displayItem = { RegistryHelper.itemLoc(item) }
    }

    fun displayItem(item: Supplier<out ItemLike>) {
        displayItem = { RegistryHelper.itemLoc(item.get()) }
    }

    fun displayTexture(value: ResourceLocation) {
        displayTexture = value
    }

    fun researchVoltage(value: Voltage) {
        voltage = value
    }

    fun noResearch() {
        noResearch = true
    }

    fun modifier(key: String, value: Int) {
        modifiers[key] = value
    }

    fun rank(value: Int) {
        rank = value
    }

    override fun createObject(): Technology {
        check(maxProgress > 0)
        val voltage1 = checkNotNull(voltage)
        return Technology(depends.toList(), maxProgress, modifiers.toMap(),
            Optional.ofNullable(displayItem?.invoke()),
            Optional.ofNullable(displayTexture),
            rank + RANK_PER_VOLTAGE * voltage1.rank)
    }

    fun validate(existingFileHelper: ExistingFileHelper) {
        for (loc in depends) {
            check(existingFileHelper.exists(loc, TechProvider.RESOURCE_TYPE)) {
                "Technology at $loc does not exist"
            }
        }
    }

    override fun loc(): ResourceLocation {
        return loc
    }

    private fun onRegister(handler: IDataHandler<TechProvider>) {
        val dataGen = handler.dataGen()
        handler.addCallback { it.addTech(this) }
        val description = Technology.getDescriptionId(loc)
        val details = Technology.getDetailsId(loc)
        dataGen.trackLang(description)
        dataGen.trackLang(details)

        if (!noResearch) {
            val voltage1 = checkNotNull(voltage)
            val input = checkNotNull(getComponent("research_equipment")[voltage1]).get()

            research {
                target(loc) {
                    input(input)
                    voltage(voltage1)
                }
            }
        }
    }

    fun register(): ResourceLocation {
        build()
        return loc
    }

    companion object {
        const val RANK_PER_VOLTAGE = 1000

        fun <P> factory(handler: IDataHandler<TechProvider>, parent: P, loc: ResourceLocation): TechBuilder<P> {
            val builder = TechBuilder(parent, loc)
            return builder.onBuild { builder.onRegister(handler) }
        }
    }
}
