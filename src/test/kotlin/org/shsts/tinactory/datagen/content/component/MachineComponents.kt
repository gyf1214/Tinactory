package org.shsts.tinactory.datagen.content.component

import org.shsts.tinactory.content.AllItems.BATTERY
import org.shsts.tinactory.content.AllItems.CABLE
import org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE
import org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR
import org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON
import org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP
import org.shsts.tinactory.content.AllItems.EMITTER
import org.shsts.tinactory.content.AllItems.FLUID_CELL
import org.shsts.tinactory.content.AllItems.MACHINE_HULL
import org.shsts.tinactory.content.AllItems.ROBOT_ARM
import org.shsts.tinactory.content.AllItems.SENSOR
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS

object MachineComponents {
    fun init() {
        cables()
        components()
        batteries()
    }

    private fun cables() {
        toolCrafting {
            result(CABLE.item(Voltage.LV)) {
                pattern("WWR")
                pattern("WWR")
                pattern("RR ")
                define('W', "tin", "wire")
                define('R', "rubber", "sheet")
                toolTag(TOOL_WIRE_CUTTER)
            }
        }

        assembler {
            for ((v, entry) in CABLE) {
                if (v == Voltage.ULV) {
                    continue
                }
                val cable = entry.get()
                output(cable) {
                    input(cable.material, "wire", 4)
                    input("rubber", amount = 2)
                    if (v == Voltage.LV) {
                        voltage(Voltage.ULV)
                    } else {
                        voltage(Voltage.LV)
                    }
                    workTicks(COMPONENT_TICKS)
                    tech(Technologies.HOT_WORKING)
                }
            }
        }
    }

    private fun components() {
        component(Voltage.LV,
            main = "steel",
            motor = "copper",
            pipe = "bronze",
            rotor = "tin",
            magnetic = "steel",
            sensor = "brass",
            quartz = "glass")

        component(Voltage.MV,
            main = "aluminium",
            motor = "cupronickel",
            pipe = "brass",
            rotor = "bronze",
            magnetic = "steel",
            sensor = "electrum",
            quartz = "ruby")

        component(Voltage.HV,
            main = "stainless_steel",
            motor = "electrum",
            pipe = "stainless_steel",
            rotor = "steel",
            magnetic = "steel",
            sensor = "chrome",
            quartz = "emerald")

        // TODO: sensor and emitter
        component(Voltage.EV,
            main = "titanium",
            motor = "kanthal",
            pipe = "titanium",
            rotor = "stainless_steel",
            magnetic = "neodymium",
            sensor = "chrome",
            quartz = "emerald")
    }

    private fun component(voltage: Voltage, main: String, motor: String,
        pipe: String, rotor: String, magnetic: String,
        sensor: String, quartz: String) {
        assembler {
            componentVoltage = voltage
            defaults {
                if (voltage.rank > Voltage.LV.rank) {
                    voltage(Voltage.LV)
                } else {
                    voltage(Voltage.ULV)
                }
                workTicks(COMPONENT_TICKS)
            }
            output(ELECTRIC_MOTOR) {
                input(magnetic, "magnetic")
                input(main, "stick", 2)
                input(motor, "wire", 2 * voltage.rank)
                input(CABLE, 2)
                tech(Technologies.MOTOR)
            }
            output(ELECTRIC_PUMP) {
                input(ELECTRIC_MOTOR, 1)
                input(pipe, "pipe")
                input(rotor, "rotor")
                input(rotor, "screw", 3)
                input("rubber", "ring", 2)
                input(CABLE, 1)
                tech(Technologies.PUMP_AND_PISTON)
            }
            output(ELECTRIC_PISTON) {
                input(ELECTRIC_MOTOR, 1)
                input(main, "plate", 3)
                input(main, "stick", 2)
                input(main, "gear")
                input(CABLE, 2)
                tech(Technologies.PUMP_AND_PISTON)
            }
            output(CONVEYOR_MODULE) {
                input(ELECTRIC_MOTOR, 2)
                input(CABLE, 1)
                input("rubber", amount = 6)
                tech(Technologies.CONVEYOR_MODULE)
            }
            output(SENSOR) {
                input(quartz, "gem")
                circuit(1)
                input(sensor, "stick")
                input(main, "plate", 4)
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            output(EMITTER) {
                input(quartz, "gem")
                circuit(2)
                input(CABLE, 2)
                input(sensor, "stick", 4)
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            output(ROBOT_ARM) {
                circuit(1)
                input(CABLE, 3)
                input(ELECTRIC_MOTOR, 2)
                input(ELECTRIC_PISTON, 1)
                input(main, "stick", 2)
                tech(Technologies.ROBOT_ARM)
            }
            output(MACHINE_HULL) {
                input(main, "plate", 8)
                input(CABLE, 2)
                if (voltage.rank >= Voltage.HV.rank) {
                    input("pe", amount = 2)
                }
                tech(Technologies.SOLDERING)
            }
            output(FLUID_CELL) {
                input(main, "plate", voltage.rank * 2)
                input(rotor, "ring", voltage.rank)
                input("soldering_alloy", amount = voltage.rank)
                voltage(Voltage.LV)
                tech(Technologies.SOLDERING)
            }
        }
    }

    private fun batteries() {
        assembler {
            defaults {
                voltage(Voltage.LV)
                workTicks(COMPONENT_TICKS)
                tech(Technologies.BATTERY)
            }
            battery(Voltage.LV, "cadmium")
            battery(Voltage.MV, "sodium_hydroxide")
            battery(Voltage.HV, "lithium")
        }
    }

    private fun AssemblyRecipeFactory.battery(voltage: Voltage, mat: String) {
        val wires = voltage.rank - 1
        val plates = wires * wires
        output(BATTERY, voltage = voltage) {
            if (voltage.rank > Voltage.LV.rank) {
                input(AllTags.battery(Voltage.fromRank(voltage.rank - 1)), 2)
            }
            input(CABLE, wires)
            input("battery_alloy", "plate", plates)
            input(mat, "dust", plates)
            input("soldering_alloy", amount = wires)
        }
    }
}
