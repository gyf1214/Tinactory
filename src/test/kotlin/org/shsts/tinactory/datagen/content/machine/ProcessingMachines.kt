package org.shsts.tinactory.datagen.content.machine

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllItems.ANNIHILATION_CORE
import org.shsts.tinactory.content.AllItems.FORMATION_CORE
import org.shsts.tinactory.content.electric.Circuits.CHIP
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS

object ProcessingMachines {
    fun init() {
        machine(Voltage.LV,
            main = "steel",
            heat = "copper",
            electric = "tin",
            pipe = "bronze",
            rotor = "tin")

        machine(Voltage.MV,
            main = "aluminium",
            heat = "cupronickel",
            electric = "copper",
            pipe = "brass",
            rotor = "bronze")

        machine(Voltage.HV,
            main = "stainless_steel",
            heat = "kanthal",
            electric = "silver",
            pipe = "stainless_steel",
            rotor = "steel")

        machine(Voltage.EV,
            main = "titanium",
            heat = "annealed_copper",
            electric = "electrum",
            pipe = "pvc",
            rotor = "stainless_steel")
    }

    private fun machine(v: Voltage, main: String,
        heat: String, electric: String,
        pipe: String, rotor: String) {
        val lastVoltage = Voltage.fromRank(v.rank - 1)
        val nextVoltage = Voltage.fromRank(v.rank + 1)
        val wireNumber = 4 * v.rank
        assembler {
            componentVoltage = v
            defaults {
                component("machine_hull")
                autoCable = true
                voltage(lastVoltage)
                workTicks(MACHINE_TICKS)
            }
            machine("research_bench") {
                circuit(2)
                component("sensor")
                component("emitter")
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            machine("assembler") {
                circuit(2)
                component("robot_arm", 2)
                component("conveyor_module", 2)
                tech(Technologies.ROBOT_ARM, Technologies.CONVEYOR_MODULE)
            }
            machine("laser_engraver") {
                circuit(3)
                component("electric_piston", 2)
                component("emitter")
                tech(Technologies.INTEGRATED_CIRCUIT)
            }

            machine("circuit_assembler") {
                circuit(4, nextVoltage)
                component("robot_arm")
                component("emitter")
                component("conveyor_module", 2)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }
            machine("stone_generator") {
                circuit(2)
                component("electric_motor")
                component("electric_piston")
                input("glass", "primary")
                tech(Technologies.PUMP_AND_PISTON)
            }
            machine("ore_analyzer") {
                circuit(2)
                component("electric_motor", 3)
                component("sensor")
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            machine("macerator") {
                circuit(3)
                component("electric_piston")
                component("conveyor_module")
                component("grinder")
                tech(Technologies.CONVEYOR_MODULE, Technologies.MATERIAL_CUTTING)
            }
            machine("ore_washer") {
                circuit(2)
                component("electric_motor")
                input(rotor, "rotor", 2)
                input("glass", "primary")
                tech(Technologies.MOTOR)
            }
            machine("centrifuge") {
                circuit(4)
                component("electric_motor", 2)
                tech(Technologies.MOTOR)
            }
            machine("thermal_centrifuge") {
                circuit(2)
                component("electric_motor", 2)
                input(heat, "wire", wireNumber)
                tech(Technologies.MOTOR, Technologies.ELECTRIC_HEATING)
            }
            machine("electric_furnace") {
                circuit(2)
                input(heat, "wire", wireNumber)
                input(main, "plate", 4)
                tech(Technologies.ELECTRIC_HEATING)
            }
            machine("alloy_smelter") {
                circuit(4)
                input(heat, "wire", wireNumber * 2)
                input(main, "plate", 8)
                tech(Technologies.ELECTRIC_HEATING)
            }
            machine("mixer") {
                circuit(2)
                component("electric_motor")
                input(rotor, "rotor")
                input("glass", "primary", 4)
                tech(Technologies.MOTOR)
            }
            machine("polarizer") {
                circuit(2)
                input(electric, "wire", wireNumber)
                tech(Technologies.MOTOR)
            }
            machine("wiremill") {
                circuit(2)
                component("electric_motor", 4)
                tech(Technologies.MOTOR)
            }
            machine("bender") {
                circuit(2)
                component("electric_motor", 2)
                component("electric_piston", 2)
                input(main, "plate", 4)
                tech(Technologies.PUMP_AND_PISTON)
            }
            machine("lathe") {
                circuit(3)
                component("electric_motor")
                component("electric_piston")
                component("grinder")
                tech(Technologies.PUMP_AND_PISTON, Technologies.MATERIAL_CUTTING)
            }
            machine("cutter") {
                circuit(3)
                component("electric_motor")
                component("conveyor_module")
                component("buzzsaw")
                tech(Technologies.CONVEYOR_MODULE, Technologies.MATERIAL_CUTTING)
            }
            machine("extruder") {
                circuit(4)
                component("electric_piston")
                input(heat, "wire", wireNumber)
                input(pipe, "pipe")
                tech(Technologies.COLD_WORKING)
            }
            machine("extractor") {
                circuit(2)
                component("electric_piston")
                component("electric_pump")
                input(heat, "wire", wireNumber)
                input("glass", "primary", 2)
                tech(Technologies.HOT_WORKING)
            }
            machine("fluid_solidifier") {
                circuit(2)
                component("electric_pump", 2)
                input("glass", "primary", 2)
                tech(Technologies.HOT_WORKING)
            }
            machine("electrolyzer") {
                circuit(4)
                input(electric, "wire", wireNumber * 2)
                input("glass", "primary")
                tech(Technologies.ELECTROLYZING)
            }
            machine("chemical_reactor") {
                circuit(4)
                component("electric_motor", 2)
                input(rotor, "rotor", 2)
                input("glass", "primary", 2)
                tech(Technologies.CHEMISTRY)
            }
            machine("arc_furnace") {
                circuit(2)
                input(electric, "wire", wireNumber * 4)
                input("graphite", "dust", 4)
                input(main, "plate", 4)
                tech(Technologies.ARC_FURNACE)
            }
            machine("steam_turbine") {
                circuit(2)
                component("electric_motor", 2)
                input(rotor, "rotor", 2)
                input(pipe, "pipe", 2)
                tech(Technologies.MOTOR)
            }
            machine("gas_turbine") {
                circuit(3)
                component("electric_motor")
                component("electric_pump")
                input(rotor, "rotor", 2)
                tech(Technologies.PUMP_AND_PISTON)
            }
            machine("combustion_generator") {
                circuit(3)
                component("electric_motor")
                component("electric_piston")
                input(main, "gear", 2)
                tech(Technologies.PUMP_AND_PISTON)
            }
            machine("electric_chest") {
                circuit(2)
                component("conveyor_module")
                input(main, "plate", 2)
                input(Items.CHEST)
                tech(Technologies.CONVEYOR_MODULE)
            }
            machine("electric_tank") {
                circuit(2)
                component("electric_pump")
                input(main, "plate", 2)
                input("glass", "primary")
                tech(Technologies.PUMP_AND_PISTON)
            }
            machine("battery_box") {
                circuit(2)
                pic()
                component("cable", 4)
                input(Items.CHEST)
                tech(Technologies.BATTERY)
            }
            component("transformer") {
                circuit(4)
                pic()
                component("cable")
                component("cable", 4, voltage = lastVoltage)
                tech(Technologies.BATTERY)
            }
            component("electric_buffer") {
                circuit(4)
                pic()
                component("cable", 2)
                tech(Technologies.BATTERY)
            }
            machine("logistic_worker") {
                circuit(4)
                component("conveyor_module", 2)
                component("electric_pump", 2)
                input(main, "plate", 4)
                tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            }
            machine("me_drive") {
                circuit(4)
                input(Items.CHEST)
                input("certus_quartz", "gem", 4)
                input("fluix", "dust", 4)
                input(main, "plate", 4)
                tech(Technologies.DIGITAL_STORAGE)
            }
            machine("me_storage_interface") {
                circuit(4)
                input(ANNIHILATION_CORE.get())
                input(FORMATION_CORE.get())
                input("fluix", "dust", 4)
                input(main, "plate", 4)
                tech(Technologies.DIGITAL_STORAGE)
            }
            component("multiblock_interface") {
                circuit(2)
                component("conveyor_module")
                component("electric_pump")
                input(Items.CHEST)
                input("glass", "primary")
                tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            }
        }
    }

    private fun AssemblyRecipeBuilder.pic() {
        val v = componentVoltage!!
        if (v.rank < Voltage.HV.rank) {
            return
        } else if (v.rank < Voltage.IV.rank) {
            input(CHIP.item("low_pic"), 2)
        } else if (v.rank < Voltage.ZPM.rank) {
            input(CHIP.item("pic"), 2)
        } else {
            input(CHIP.item("high_pic"), 2)
        }
    }
}
