package org.shsts.tinactory.datagen.content.component

import org.shsts.tinactory.content.AllItems.getComponent
import org.shsts.tinactory.content.AllTags
import org.shsts.tinactory.content.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.content.network.CableBlock
import org.shsts.tinactory.core.electric.Voltage
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
        val items = getComponent("cable")

        toolCrafting {
            result(items.item(Voltage.LV)) {
                pattern("WWR")
                pattern("WWR")
                pattern("RR ")
                define('W', "tin", "wire")
                define('R', "rubber", "sheet")
                toolTag(TOOL_WIRE_CUTTER)
            }
        }

        assembler {
            defaults {
                workTicks(COMPONENT_TICKS)
                tech(Technologies.HOT_WORKING)
            }
            for ((v, entry) in items) {
                if (v == Voltage.ULV) {
                    continue
                }
                val cable = entry.get() as CableBlock
                output(cable) {
                    input(cable.material, "wire", 4)
                    if (v.rank >= Voltage.EV.rank) {
                        input("silicone_rubber", "foil", 2)
                    }
                    input("rubber", amount = 2)
                    voltage(Voltage.fromRank(v.rank - 1))
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

        component(Voltage.EV,
            main = "titanium",
            motor = "kanthal",
            pipe = "pvc",
            rotor = "stainless_steel",
            magnetic = "neodymium",
            sensor = "molybdenum",
            quartz = "fluix")

        // TODO
        component(Voltage.IV,
            main = "tungsten_steel",
            motor = "annealed_copper",
            pipe = "ptfe",
            rotor = "titanium",
            magnetic = "neodymium",
            sensor = "platinum",
            quartz = "fluix")
    }

    private fun component(v: Voltage, main: String, motor: String,
        pipe: String, rotor: String, magnetic: String,
        sensor: String, quartz: String) {
        assembler {
            componentVoltage = v
            defaults {
                voltage(Voltage.fromRank(v.rank - 1))
                workTicks(COMPONENT_TICKS)
            }
            component("electric_motor") {
                input(magnetic, "magnetic")
                input(main, "stick", 2)
                input(motor, "wire", 2 * v.rank)
                component("cable", 2)
                tech(Technologies.MOTOR)
            }
            component("electric_pump") {
                component("electric_motor", 1)
                input(pipe, "pipe")
                input(rotor, "rotor")
                input(rotor, "screw", 3)
                input("rubber", "ring", 2)
                component("cable", 1)
                tech(Technologies.PUMP_AND_PISTON)
            }
            component("electric_piston") {
                component("electric_motor", 1)
                input(main, "plate", 3)
                input(main, "stick", 2)
                input(main, "gear")
                component("cable", 2)
                tech(Technologies.PUMP_AND_PISTON)
            }
            component("conveyor_module") {
                component("electric_motor", 2)
                component("cable", 1)
                input("rubber", amount = 6)
                tech(Technologies.CONVEYOR_MODULE)
            }
            component("sensor") {
                input(quartz, "gem")
                circuit(1)
                input(sensor, "stick")
                input(main, "plate", 4)
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            component("emitter") {
                input(quartz, "gem")
                circuit(2)
                component("cable", 2)
                input(sensor, "stick", 4)
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            component("robot_arm") {
                circuit(1)
                component("cable", 3)
                component("electric_motor", 2)
                component("electric_piston", 1)
                input(main, "stick", 2)
                tech(Technologies.ROBOT_ARM)
            }
            component("machine_hull") {
                input(main, "plate", 8)
                component("cable", 2)
                if (v.rank >= Voltage.HV.rank) {
                    input("pe", amount = 2)
                }
                tech(Technologies.SOLDERING)
            }
            component("fluid_cell") {
                input(main, "plate", v.rank * 2)
                input(rotor, "ring", v.rank)
                input("soldering_alloy", amount = v.rank)
                voltage(Voltage.LV)
                tech(Technologies.SOLDERING)
            }
        }
    }

    private fun batteries() {
        assembler {
            defaults {
                workTicks(COMPONENT_TICKS)
                tech(Technologies.BATTERY)
            }
            battery(Voltage.LV, "cadmium")
            battery(Voltage.MV, "sulfuric_acid", "dilute")
            battery(Voltage.HV, "lithium")
        }
    }

    private fun AssemblyRecipeFactory.battery(v: Voltage, mat: String, sub: String = "dust") {
        val wires = v.rank - 1
        val plates = wires * wires
        component("battery", voltage = v) {
            if (v.rank > Voltage.LV.rank) {
                input(AllTags.battery(Voltage.fromRank(v.rank - 1)), 2)
                input("soldering_alloy", amount = wires)
            }
            component("cable", wires)
            input("battery_alloy", "plate", plates)
            input(mat, sub, plates)
            voltage(v)
        }
    }
}
