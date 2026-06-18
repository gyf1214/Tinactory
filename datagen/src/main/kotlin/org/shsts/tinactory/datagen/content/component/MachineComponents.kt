package org.shsts.tinactory.datagen.content.component

import org.shsts.tinactory.AllItems.getComponent
import org.shsts.tinactory.AllTags.TOOL_WIRE_CUTTER
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeFactory
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assemblyLine
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.toolCrafting
import org.shsts.tinactory.datagen.content.component.Components.ADVANCED_COMPONENT_TICKS
import org.shsts.tinactory.datagen.content.component.Components.COMPONENT_TICKS
import org.shsts.tinactory.integration.network.CableBlock

object MachineComponents {
    fun init() {
        cables()
        components()
        batteries()
        superconductors()
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
                    input(cable.cableMaterial, "wire", 4)
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

        component(Voltage.IV,
            main = "tungsten_steel",
            motor = "annealed_copper",
            pipe = "ptfe",
            rotor = "titanium",
            magnetic = "neodymium",
            sensor = "platinum",
            quartz = "ender_pearl")

        advancedComponent(Voltage.LUV,
            main = "hssg",
            casing = "rhodium_plated_palladium",
            insulation = "ptfe",
            motorWire = "ruridit",
            pipe = "niobium_titanium",
            rotor = "hssg",
            magnetic = "neodymium",
            sensorCore = "ender_eye",
            sensorBody = "ruridit",
            sensorFoil = "palladium")
    }

    private fun component(v: Voltage, main: String, motor: String,
        pipe: String, rotor: String, magnetic: String,
        sensor: String, quartz: String) {
        assembler {
            componentVoltage = v
            defaults {
                voltage(Voltage.fromRank(v.rank - 1))
                workTicks(COMPONENT_TICKS)
                if (v.rank >= Voltage.IV.rank) {
                    tech(Technologies.TUNGSTEN_STEEL)
                }
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
            component("field_generator") {
                input(quartz, "gem")
                circuit(2)
                input(v.id + "_superconductor", "wire", amount = 16)
                input(main, "plate", 4)
                tech(Technologies.NUCLEAR_PHYSICS)
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
            battery(Voltage.MV, "sodium_hydroxide")
            battery(Voltage.HV, "lithium")
        }
    }

    private fun advancedComponent(v: Voltage, main: String, casing: String,
        insulation: String, motorWire: String, pipe: String, rotor: String,
        magnetic: String, sensorCore: String, sensorBody: String, sensorFoil: String) {
        val v1 = Voltage.fromRank(v.rank - 1)
        assembler {
            componentVoltage = v
            defaults {
                voltage(v1)
                workTicks(ADVANCED_COMPONENT_TICKS)
            }
            component("machine_hull") {
                input(main, "stick", 4)
                input(casing, "plate", 8)
                component("cable", 2)
                input(insulation, amount = 2)
                input("soldering_alloy", amount = 4)
                tech(Technologies.RHODIUM_PLATED_PALLADIUM)
            }
        }
        assemblyLine {
            componentVoltage = v
            defaults {
                voltage(v1)
                workTicks(ADVANCED_COMPONENT_TICKS)
                tech(Technologies.ASSEMBLY_LINE)
            }
            component("electric_motor") {
                input(magnetic, "magnetic")
                input(main, "stick", 4)
                input(main, "ring", 2)
                input(motorWire, "wire", 32)
                component("cable", 2)
                input("soldering_alloy")
            }
            component("electric_pump") {
                component("electric_motor")
                input(pipe, "pipe")
                input(main, "plate", 2)
                input(main, "screw", 8)
                input(rotor, "rotor")
                input("silicone_rubber", "ring", 4)
                component("cable", 2)
                input("soldering_alloy")
            }
            component("electric_piston") {
                component("electric_motor")
                input(main, "plate", 4)
                input(main, "ring", 4)
                input(main, "stick", 4)
                input(main, "gear", 6)
                component("cable", 2)
                input(insulation, amount = 2)
                input("soldering_alloy")
            }
            component("conveyor_module") {
                component("electric_motor", 2)
                input(main, "plate", 2)
                input(main, "ring", 4)
                input(main, "screw", 4)
                component("cable", 2)
                input("silicone_rubber", amount = 8)
                input("soldering_alloy", amount = 2)
            }
            component("robot_arm") {
                component("electric_motor", 2)
                component("electric_piston")
                input(main, "stick", 8)
                input(main, "gear", 7)
                circuit(1)
                circuit(2, Voltage.fromRank(v.rank - 1))
                component("cable", 4)
                input("soldering_alloy", amount = 4)
            }
        }
        assemblyLine {
            componentVoltage = v
            defaults {
                input(main, "stick", 3)
                component("electric_motor")
                input(sensorCore, "gem")
                circuit(2)
                input(sensorFoil, "foil", 64)
                component("cable", 4)
                input("soldering_alloy", amount = 2)
                voltage(v1)
                workTicks(ADVANCED_COMPONENT_TICKS)
                tech(Technologies.ENDER_CHEMISTRY)
            }
            component("sensor") {
                input(sensorBody, "plate", 4)
            }
            component("emitter") {
                input(sensorBody, "stick", 4)
            }
        }
        assemblyLine {
            componentVoltage = v
            defaults {
                voltage(v1)
                workTicks(ADVANCED_COMPONENT_TICKS)
                tech(Technologies.ENDER_CHEMISTRY)
            }
            component("field_generator") {
                input(main, "stick", 3)
                input(sensorBody, "plate", 6)
                input(sensorCore, "gem")
                component("emitter", 2)
                circuit(2)
                input(v.id + "_superconductor", "wire", 64)
                component("cable", 4)
                input("soldering_alloy", amount = 4)
            }
        }
    }

    private fun AssemblyRecipeFactory.battery(v: Voltage, mat: String, sub: String = "dust") {
        val wires = v.rank - 1
        val plates = wires * wires
        component("battery", voltage = v) {
            component("cable", wires)
            input("battery_alloy", "plate", plates)
            input(mat, sub, plates)
            input("soldering_alloy", amount = wires)
            voltage(Voltage.fromRank(v.rank - 1))
        }
    }

    private fun superconductors() {
        assembler {
            defaults {
                workTicks(COMPONENT_TICKS)
                tech(Technologies.NUCLEAR_PHYSICS)
            }
            superconductor(Voltage.EV, "niobium_titanium", "ptfe", "coolant")
            superconductor(Voltage.IV, "vanadium_gallium", "niobium_titanium", "coolant")
            superconductor(Voltage.LUV, "yttrium_barium_cuprate", "hsss", "nitrogen", "liquid")
        }
    }

    private fun AssemblyRecipeFactory.superconductor(v: Voltage, mat: String,
        pipe: String, coolant: String, coolantSub: String? = null) {
        output(v.id + "_superconductor", "wire") {
            input(mat, "wire")
            component("electric_pump", voltage = v)
            input(pipe, "pipe")
            if (coolantSub == null) {
                input(coolant)
            } else {
                input(coolant, coolantSub)
            }
            voltage(v)
        }
    }
}
