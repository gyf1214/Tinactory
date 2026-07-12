package org.shsts.tinactory.datagen.builder

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.common.data.ExistingFileHelper
import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.api.tech.ITechnology
import org.shsts.tinactory.core.builder.Builder
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.tech.Technology
import org.shsts.tinactory.core.util.LocHelper.modLoc
import org.shsts.tinactory.datagen.content.RegistryHelper
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.research
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.provider.TechProvider
import org.shsts.tinycorelib.api.registrate.entry.IEntry
import org.shsts.tinycorelib.datagen.api.IDataHandler
import java.util.Optional

class TechBuilder<P>(parent: P) : Builder<Technology, P, TechBuilder<P>>(parent) {
    private val depends = mutableListOf<ResourceLocation>()
    private val modifiers = mutableMapOf<String, Int>()
    private var maxProgress = 0L
    private var rank = 0
    private var displayItem: ResourceLocation? = null
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
        displayItem = loc
    }

    fun displayItem(id: String) {
        displayItem = modLoc(id)
    }

    fun displayItem(item: ItemLike) {
        displayItem = RegistryHelper.itemLoc(item)
    }

    fun displayItem(item: IEntry<out ItemLike>) {
        displayItem = item.loc()
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
        return Technology(depends.toList(), maxProgress, modifiers.toMap(),
            Optional.ofNullable(displayItem),
            Optional.ofNullable(displayTexture),
            rank + RANK_PER_VOLTAGE * voltage!!.rank)
    }

    fun validate(existingFileHelper: ExistingFileHelper) {
        check(maxProgress > 0)
        checkNotNull(voltage)
        for (loc in depends) {
            check(existingFileHelper.exists(loc, TechProvider.RESOURCE_TYPE)) {
                "Technology at $loc does not exist"
            }
        }
    }

    private fun onRegister(handler: IDataHandler<TechProvider>, loc: ResourceLocation) {
        val dataGen = handler.dataGen()
        handler.addCallback { it.addTech(loc, this) }
        val description = ITechnology.getDescriptionId(loc)
        val details = ITechnology.getDetailsId(loc)
        dataGen.trackLang(description)
        dataGen.trackLang(details)

        if (!noResearch) {
            research {
                target(loc) {
                    input(getComponent("research_equipment").item(voltage!!))
                    voltage(voltage!!)
                }
            }
        }
    }

    companion object {
        const val RANK_PER_VOLTAGE = 1000

        fun factory(handler: IDataHandler<TechProvider>, parent: Any, loc: ResourceLocation):
            TechBuilder<ResourceLocation> {
            val builder = TechBuilder(loc)
            return builder.onBuild { builder.onRegister(handler, loc) }
        }
    }
}
