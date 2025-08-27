package org.shsts.tinactory.datagen.content

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllBlockEntities
import org.shsts.tinactory.content.AllItems
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllMultiblocks
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.content.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.datagen.builder.TechBuilder
import org.shsts.tinactory.datagen.content.builder.VeinBuilder.Companion.VEIN_TECH_RANK
import org.shsts.tinactory.datagen.provider.TechProvider
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

object Technologies {
    @JvmField
    val TECHS = DATA_GEN.createHandler(::TechProvider)

    @JvmField
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

    @JvmField
    val ALLOY_SMELTING: ResourceLocation

    @JvmField
    val SOLDERING: ResourceLocation

    @JvmField
    val STEEL: ResourceLocation

    @JvmField
    val MOTOR: ResourceLocation

    @JvmField
    val PUMP_AND_PISTON: ResourceLocation

    @JvmField
    val ELECTRIC_HEATING: ResourceLocation

    @JvmField
    val MATERIAL_CUTTING: ResourceLocation

    @JvmField
    val CONVEYOR_MODULE: ResourceLocation

    @JvmField
    val BATTERY: ResourceLocation

    @JvmField
    val SENSOR_AND_EMITTER: ResourceLocation

    @JvmField
    val HOT_WORKING: ResourceLocation

    @JvmField
    val ROBOT_ARM: ResourceLocation

    @JvmField
    val KANTHAL: ResourceLocation

    @JvmField
    val SIFTING: ResourceLocation

    @JvmField
    val AUTOFARM: ResourceLocation

    @JvmField
    val INTEGRATED_CIRCUIT: ResourceLocation

    @JvmField
    val COLD_WORKING: ResourceLocation

    @JvmField
    val ELECTROLYZING: ResourceLocation

    @JvmField
    val VACUUM_FREEZER: ResourceLocation

    @JvmField
    val DISTILLATION: ResourceLocation

    @JvmField
    val CHEMISTRY: ResourceLocation

    @JvmField
    val PYROLYSE_OVEN: ResourceLocation

    @JvmField
    val OIL_PROCESSING: ResourceLocation

    @JvmField
    val ORGANIC_CHEMISTRY: ResourceLocation

    @JvmField
    val CPU: ResourceLocation

    @JvmField
    val CLEANROOM: ResourceLocation

    @JvmField
    val NICHROME: ResourceLocation

    @JvmField
    val ARC_FURNACE: ResourceLocation

    @JvmField
    val HYDROMETALLURGY: ResourceLocation

    @JvmField
    val ADVANCED_CHEMISTRY: ResourceLocation

    init {
        Factory().apply {
            ALLOY_SMELTING = child("alloy_smelting") {
                maxProgress(20)
                displayItem(AllBlockEntities.ALLOY_SMELTER.entry(Voltage.ULV))
            }

            SOLDERING = tech("soldering") {
                maxProgress(30)
                displayItem(getMaterial("wrought_iron").entry("tool/screwdriver"))
            }

            STEEL = child("steel") {
                maxProgress(30)
                displayItem(getMaterial("steel").entry("ingot"))
            }

            ELECTRIC_HEATING = tech("electric_heating") {
                maxProgress(30)
                displayItem(getMaterial("copper").entry("wire"))
            }

            BATTERY = tech("battery") {
                maxProgress(40)
                displayItem(AllItems.BATTERY.getValue(Voltage.LV))
            }

            MOTOR = child("motor") {
                maxProgress(30)
                displayItem(AllItems.ELECTRIC_MOTOR.getValue(Voltage.LV))
            }

            MATERIAL_CUTTING = tech("material_cutting") {
                maxProgress(35)
                displayItem(AllItems.BASIC_BUZZSAW)
            }

            SENSOR_AND_EMITTER = tech("sensor_and_emitter") {
                maxProgress(40)
                displayItem(AllItems.EMITTER.getValue(Voltage.LV))
            }

            PUMP_AND_PISTON = child("pump_and_piston") {
                maxProgress(35)
                displayItem(AllItems.ELECTRIC_PISTON.getValue(Voltage.LV))
            }

            HOT_WORKING = child("hot_working") {
                maxProgress(40)
                displayItem(Items.BLAZE_POWDER)
            }

            CONVEYOR_MODULE = child("conveyor_module") {
                maxProgress(40)
                displayItem(AllItems.CONVEYOR_MODULE.getValue(Voltage.LV))
            }

            ROBOT_ARM = child("robot_arm") {
                maxProgress(50)
                displayItem(AllItems.ROBOT_ARM.getValue(Voltage.LV))
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
                displayItem(AllItems.GOOD_INTEGRATED.item())
                depends(SENSOR_AND_EMITTER, MATERIAL_CUTTING)
            }

            COLD_WORKING = tech("cold_working") {
                maxProgress(40)
                displayTexture(gregtech("items/metaitems/shape.extruder.rotor"))
            }

            ELECTROLYZING = tech("electrolyzing") {
                maxProgress(40)
                displayItem(getMaterial("gold").entry("wire"))
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
                displayItem(AllItems.RESEARCH_EQUIPMENT.getValue(Voltage.LV))
            }

            PYROLYSE_OVEN = tech("pyrolyse_oven") {
                maxProgress(40)
                displayItem(AllMultiblocks.PYROLYSE_OVEN.get())
            }

            OIL_PROCESSING = tech("oil_processing") {
                maxProgress(60)
                displayItem(getMaterial("sulfur").entry("dust"))
            }

            ORGANIC_CHEMISTRY = child("organic_chemistry") {
                maxProgress(50)
                displayItem(getMaterial("pe").entry("sheet"))
            }

            CPU = tech("cpu") {
                maxProgress(100)
                displayItem(AllItems.WAFERS.getValue("cpu"))
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
                displayItem(getMaterial("aluminium_oxide").entry("dust"))
            }

            ARC_FURNACE = tech("arc_furnace") {
                maxProgress(30)
                displayItem(AllBlockEntities.ARC_FURNACE.entry(Voltage.HV))
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
    }

    fun init() {}
}
