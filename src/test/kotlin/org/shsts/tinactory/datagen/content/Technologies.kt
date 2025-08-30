package org.shsts.tinactory.datagen.content

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllBlockEntities
import org.shsts.tinactory.content.AllItems
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllMultiblocks
import org.shsts.tinactory.content.electric.Circuits
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.datagen.builder.TechBuilder
import org.shsts.tinactory.datagen.content.builder.VeinBuilder.Companion.VEIN_TECH_RANK
import org.shsts.tinactory.datagen.provider.TechProvider
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

object Technologies {
    val TECHS = DATA_GEN.createHandler(::TechProvider)

    val BASE_ORE = Factory().run {
        OreVariant.entries.associateWith {
            child("ore_base/${it.name.lowercase()}") {
                maxProgress(20)
                displayItem(it.baseItem)
                researchVoltage(it.voltage)
                rank(VEIN_TECH_RANK)
            }
        }
    }

    val ALLOY_SMELTING: ResourceLocation
    val SOLDERING: ResourceLocation
    val STEEL: ResourceLocation
    val MOTOR: ResourceLocation
    val PUMP_AND_PISTON: ResourceLocation
    val ELECTRIC_HEATING: ResourceLocation
    val MATERIAL_CUTTING: ResourceLocation
    val CONVEYOR_MODULE: ResourceLocation
    val BATTERY: ResourceLocation
    val SENSOR_AND_EMITTER: ResourceLocation
    val HOT_WORKING: ResourceLocation
    val ROBOT_ARM: ResourceLocation
    val KANTHAL: ResourceLocation
    val SIFTING: ResourceLocation
    val AUTOFARM: ResourceLocation
    val INTEGRATED_CIRCUIT: ResourceLocation
    val COLD_WORKING: ResourceLocation
    val ELECTROLYZING: ResourceLocation
    val VACUUM_FREEZER: ResourceLocation
    val DISTILLATION: ResourceLocation
    val CHEMISTRY: ResourceLocation
    val PYROLYSE_OVEN: ResourceLocation
    val OIL_PROCESSING: ResourceLocation
    val ORGANIC_CHEMISTRY: ResourceLocation
    val CPU: ResourceLocation
    val CLEANROOM: ResourceLocation
    val NICHROME: ResourceLocation
    val ARC_FURNACE: ResourceLocation
    val HYDROMETALLURGY: ResourceLocation
    val ADVANCED_CHEMISTRY: ResourceLocation

    init {
        Factory().apply {
            ALLOY_SMELTING = child("alloy_smelting") {
                maxProgress(20)
                displayItem(AllBlockEntities.ALLOY_SMELTER.entry(Voltage.ULV))
            }

            SOLDERING = tech("soldering") {
                maxProgress(30)
                displayItem("wrought_iron", "tool/screwdriver")
            }

            STEEL = child("steel") {
                maxProgress(30)
                displayItem("steel", "ingot")
            }

            ELECTRIC_HEATING = tech("electric_heating") {
                maxProgress(30)
                displayItem("copper", "wire")
            }

            BATTERY = tech("battery") {
                maxProgress(40)
                displayItem(getComponent("battery").getValue(Voltage.LV))
            }

            MOTOR = child("motor") {
                maxProgress(30)
                displayItem(getComponent("electric_motor").getValue(Voltage.LV))
            }

            MATERIAL_CUTTING = tech("material_cutting") {
                maxProgress(35)
                displayItem(AllItems.BASIC_BUZZSAW)
            }

            SENSOR_AND_EMITTER = tech("sensor_and_emitter") {
                maxProgress(40)
                displayItem(getComponent("emitter").getValue(Voltage.LV))
            }

            PUMP_AND_PISTON = child("pump_and_piston") {
                maxProgress(35)
                displayItem(getComponent("electric_piston").getValue(Voltage.LV))
            }

            HOT_WORKING = child("hot_working") {
                maxProgress(40)
                displayItem(Items.BLAZE_POWDER)
            }

            CONVEYOR_MODULE = child("conveyor_module") {
                maxProgress(40)
                displayItem(getComponent("conveyor_module").getValue(Voltage.LV))
            }

            ROBOT_ARM = child("robot_arm") {
                maxProgress(50)
                displayItem(getComponent("robot_arm").getValue(Voltage.LV))
            }

            voltage = Voltage.LV

            KANTHAL = tech("kanthal") {
                maxProgress(10)
                displayItem(AllMultiblocks.KANTHAL_COIL_BLOCK)
            }

            SIFTING = tech("sifting") {
                maxProgress(10)
                displayItem(AllItems.ITEM_FILTER)
            }

            AUTOFARM = tech("autofarm") {
                maxProgress(15)
                displayItem(Items.WHEAT)
            }

            INTEGRATED_CIRCUIT = tech("integrated_circuit") {
                maxProgress(20)
                displayItem(Circuits.getCircuit("good_integrated").entry())
                depends(SENSOR_AND_EMITTER, MATERIAL_CUTTING)
            }

            COLD_WORKING = tech("cold_working") {
                maxProgress(40)
                displayTexture(gregtech("items/metaitems/shape.extruder.rotor"))
            }

            ELECTROLYZING = tech("electrolyzing") {
                maxProgress(40)
                displayItem("gold", "wire")
            }

            VACUUM_FREEZER = tech("vacuum_freezer") {
                maxProgress(50)
                displayItem(AllMultiblocks.VACUUM_FREEZER)
            }

            DISTILLATION = tech("distillation") {
                maxProgress(50)
                displayItem(AllMultiblocks.DISTILLATION_TOWER)
            }

            CHEMISTRY = child("chemistry") {
                maxProgress(40)
                displayItem(getComponent("research_equipment").getValue(Voltage.LV))
            }

            PYROLYSE_OVEN = tech("pyrolyse_oven") {
                maxProgress(40)
                displayItem(AllMultiblocks.PYROLYSE_OVEN.get())
            }

            OIL_PROCESSING = tech("oil_processing") {
                maxProgress(60)
                displayItem("sulfur", "dust")
            }

            ORGANIC_CHEMISTRY = child("organic_chemistry") {
                maxProgress(50)
                displayItem("pe", "sheet")
            }

            CPU = tech("cpu") {
                maxProgress(100)
                displayItem(Circuits.WAFER.getValue("cpu"))
                depends(INTEGRATED_CIRCUIT)
            }

            base = null
            voltage = Voltage.MV

            NICHROME = tech("nichrome") {
                maxProgress(20)
                displayItem(AllMultiblocks.NICHROME_COIL_BLOCK)
                depends(KANTHAL)
            }

            base = CHEMISTRY

            HYDROMETALLURGY = tech("hydrometallurgy") {
                maxProgress(30)
                displayItem("aluminium_oxide", "dust")
            }

            ARC_FURNACE = tech("arc_furnace") {
                maxProgress(30)
                displayItem(AllBlockEntities.ARC_FURNACE.entry(
                    Voltage.HV))
            }

            base = ORGANIC_CHEMISTRY

            CLEANROOM = tech("cleanroom") {
                maxProgress(40)
                displayItem(AllMultiblocks.CLEANROOM)
            }

            ADVANCED_CHEMISTRY = tech("advanced_chemistry") {
                maxProgress(60)
                displayItem(AllMultiblocks.LARGE_CHEMICAL_REACTOR)
            }

        }
    }

    private class Factory {
        var base: ResourceLocation? = null
        var voltage = Voltage.ULV
        var rank = 0

        fun child(id: String, block: TechBuilder<*>.() -> Unit) =
            tech(id, block).also { base = it }

        fun tech(id: String, block: TechBuilder<*>.() -> Unit) =
            TECHS.builder(Unit, id) { handler, parent, loc ->
                TechBuilder.factory(handler, parent, loc)
            }.run {
                rank(rank++)
                if (base != null) {
                    depends(base)
                }
                researchVoltage(voltage)
                block()
                register()
            }

        fun TechBuilder<*>.displayItem(name: String, sub: String) {
            displayItem(getMaterial(name).loc(sub))
        }
    }

    fun init() {}
}
