package org.shsts.tinactory.datagen.content

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllBlockEntities.getMachine
import org.shsts.tinactory.content.AllItems.ITEM_STORAGE_CELL
import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllMaterials.getMaterial
import org.shsts.tinactory.content.AllMultiblocks
import org.shsts.tinactory.content.AllMultiblocks.getMultiblock
import org.shsts.tinactory.content.electric.CircuitComponentTier
import org.shsts.tinactory.content.electric.Circuits
import org.shsts.tinactory.content.electric.Circuits.getCircuitComponent
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.core.material.OreVariant
import org.shsts.tinactory.core.util.LocHelper.gregtech
import org.shsts.tinactory.datagen.builder.TechBuilder
import org.shsts.tinactory.datagen.content.builder.VeinBuilder.Companion.VEIN_TECH_RANK
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.provider.TechProvider
import org.shsts.tinactory.test.TinactoryTest.DATA_GEN

object Technologies {
    val TECHS = DATA_GEN.createHandler(::TechProvider)

    val BASE_ORE = Factory().run {
        OreVariant.entries.associateWith {
            child("ore/${it.serializedName}") {
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
    val ELECTRIC_HEATING: ResourceLocation
    val BATTERY: ResourceLocation
    val MOTOR: ResourceLocation
    val MATERIAL_CUTTING: ResourceLocation
    val SENSOR_AND_EMITTER: ResourceLocation
    val PUMP_AND_PISTON: ResourceLocation
    val HOT_WORKING: ResourceLocation
    val CONVEYOR_MODULE: ResourceLocation
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
    val NICHROME: ResourceLocation
    val OIL_CRACKING: ResourceLocation
    val HYDROMETALLURGY: ResourceLocation
    val ARC_FURNACE: ResourceLocation
    val CLEANROOM: ResourceLocation
    val ADVANCED_CHEMISTRY: ResourceLocation
    val TNT: ResourceLocation
    val SURFACE_MOUNT_DEVICE: ResourceLocation
    val AUTOCLAVE: ResourceLocation
    val DIGITAL_STORAGE: ResourceLocation
    val ADVANCED_POLYMER: ResourceLocation
    val LITHOGRAPHY: ResourceLocation
    val ROCKET_SCIENCE: ResourceLocation
    val ROCKET_T1: ResourceLocation
    val MULTI_SMELTER: ResourceLocation
    val TUNGSTEN_STEEL: ResourceLocation
    val METAL_FORMER: ResourceLocation

    init {
        Factory().apply {
            ALLOY_SMELTING = child("alloy_smelting") {
                maxProgress(20)
                displayItem(getMachine("alloy_smelter").entry(Voltage.ULV))
            }

            SOLDERING = tech("soldering") {
                maxProgress(30)
                displayMaterial("wrought_iron", "tool/screwdriver")
            }

            STEEL = child("steel") {
                maxProgress(30)
                displayMaterial("steel", "ingot")
            }

            ELECTRIC_HEATING = tech("electric_heating") {
                maxProgress(30)
                displayMaterial("copper", "wire")
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
                displayItem("component/buzzsaw/basic")
            }

            SENSOR_AND_EMITTER = tech("sensor_and_emitter") {
                maxProgress(40)
                displayItem(getComponent("sensor").getValue(Voltage.LV))
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
                displayItem(AllMultiblocks.COIL_BLOCKS.getValue("kanthal"))
            }

            SIFTING = tech("sifting") {
                maxProgress(10)
                displayItem("component/item_filter")
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
                displayMaterial("gold", "wire")
            }

            VACUUM_FREEZER = tech("vacuum_freezer") {
                maxProgress(50)
                displayItem(getMultiblock("vacuum_freezer").block)
            }

            DISTILLATION = tech("distillation") {
                maxProgress(50)
                displayItem(getMultiblock("distillation_tower").block)
            }

            CHEMISTRY = child("chemistry") {
                maxProgress(40)
                displayItem(getComponent("research_equipment").getValue(Voltage.LV))
            }

            PYROLYSE_OVEN = tech("pyrolyse_oven") {
                maxProgress(40)
                displayItem(getMultiblock("pyrolyse_oven").block)
            }

            OIL_PROCESSING = tech("oil_processing") {
                maxProgress(60)
                displayMaterial("sulfur", "dust")
            }

            ORGANIC_CHEMISTRY = child("organic_chemistry") {
                maxProgress(50)
                displayMaterial("pe", "sheet")
            }

            CPU = tech("cpu") {
                maxProgress(100)
                displayItem(Circuits.WAFER.getValue("cpu"))
                depends(INTEGRATED_CIRCUIT)
            }

            base = null
            voltage = Voltage.MV

            NICHROME = tech("nichrome") {
                maxProgress(50)
                displayItem(AllMultiblocks.COIL_BLOCKS.getValue("nichrome"))
                depends(KANTHAL)
            }

            OIL_CRACKING = tech("oil_cracking") {
                maxProgress(100)
                displayItem(getMultiblock("oil_cracking_unit").block)
                depends(OIL_PROCESSING)
            }

            base = CHEMISTRY

            HYDROMETALLURGY = tech("hydrometallurgy") {
                maxProgress(80)
                displayMaterial("aluminium_oxide", "dust")
            }

            ARC_FURNACE = tech("arc_furnace") {
                maxProgress(60)
                displayItem(getMachine("arc_furnace").entry(Voltage.HV))
            }

            base = ORGANIC_CHEMISTRY

            CLEANROOM = tech("cleanroom") {
                maxProgress(160)
                displayItem(AllMultiblocks.CLEANROOM)
            }

            ADVANCED_CHEMISTRY = tech("advanced_chemistry") {
                maxProgress(200)
                displayItem(getMultiblock("large_chemical_reactor").block)
            }

            TNT = tech("tnt") {
                maxProgress(200)
                displayItem(Items.TNT)
            }

            voltage = Voltage.HV

            base = ORGANIC_CHEMISTRY

            SURFACE_MOUNT_DEVICE = tech("surface_mount_device") {
                maxProgress(120)
                displayItem(getCircuitComponent("resistor").loc(CircuitComponentTier.SMD))
                depends(CPU)
            }

            base = CLEANROOM

            AUTOCLAVE = child("autoclave") {
                maxProgress(100)
                displayItem(getMaterial("certus_quartz").item("gem"))
            }

            DIGITAL_STORAGE = tech("digital_storage") {
                maxProgress(240)
                displayItem(ITEM_STORAGE_CELL[0])
                depends(CPU)
            }

            base = ADVANCED_CHEMISTRY

            ADVANCED_POLYMER = tech("advanced_polymer") {
                maxProgress(160)
                displayMaterial("epoxy", "sheet")
            }

            LITHOGRAPHY = tech("lithography") {
                maxProgress(160)
                displayItem(getComponent("emitter").item(Voltage.HV))
                depends(CPU)
            }

            ROCKET_SCIENCE = child("rocket_science") {
                maxProgress(200)
                displayItem(Items.FIREWORK_ROCKET)
                depends(CPU, TNT)
            }

            ROCKET_T1 = tech("rocket_t1") {
                maxProgress(250)
                displayItem(Items.FIREWORK_ROCKET)
                noResearch()
            }

            base = ARC_FURNACE

            MULTI_SMELTER = tech("multi_smelter") {
                maxProgress(200)
                displayItem(getMultiblock("multi_smelter").block)
            }

            base = ADVANCED_CHEMISTRY

            TUNGSTEN_STEEL = child("tungsten_steel") {
                maxProgress(200)
                displayMaterial("tungsten_steel", "ingot")
                depends(HYDROMETALLURGY)
            }

            METAL_FORMER = tech("metal_former") {
                maxProgress(200)
                displayItem(getMultiblock("metal_former").block)
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

        fun TechBuilder<*>.displayMaterial(name: String, sub: String) {
            displayItem(getMaterial(name).loc(sub))
        }
    }

    fun init() {}
}
