package org.shsts.tinactory.datagen.content.machine

import net.minecraft.world.item.Items
import org.shsts.tinactory.content.AllBlockEntities.ALLOY_SMELTER
import org.shsts.tinactory.content.AllBlockEntities.ARC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.ASSEMBLER
import org.shsts.tinactory.content.AllBlockEntities.BATTERY_BOX
import org.shsts.tinactory.content.AllBlockEntities.BENDER
import org.shsts.tinactory.content.AllBlockEntities.CENTRIFUGE
import org.shsts.tinactory.content.AllBlockEntities.CHEMICAL_REACTOR
import org.shsts.tinactory.content.AllBlockEntities.CIRCUIT_ASSEMBLER
import org.shsts.tinactory.content.AllBlockEntities.COMBUSTION_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.CUTTER
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_CHEST
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_FURNACE
import org.shsts.tinactory.content.AllBlockEntities.ELECTRIC_TANK
import org.shsts.tinactory.content.AllBlockEntities.ELECTROLYZER
import org.shsts.tinactory.content.AllBlockEntities.EXTRACTOR
import org.shsts.tinactory.content.AllBlockEntities.EXTRUDER
import org.shsts.tinactory.content.AllBlockEntities.FLUID_SOLIDIFIER
import org.shsts.tinactory.content.AllBlockEntities.GAS_TURBINE
import org.shsts.tinactory.content.AllBlockEntities.LASER_ENGRAVER
import org.shsts.tinactory.content.AllBlockEntities.LATHE
import org.shsts.tinactory.content.AllBlockEntities.LOGISTIC_WORKER
import org.shsts.tinactory.content.AllBlockEntities.MACERATOR
import org.shsts.tinactory.content.AllBlockEntities.MIXER
import org.shsts.tinactory.content.AllBlockEntities.MULTIBLOCK_INTERFACE
import org.shsts.tinactory.content.AllBlockEntities.ORE_ANALYZER
import org.shsts.tinactory.content.AllBlockEntities.ORE_WASHER
import org.shsts.tinactory.content.AllBlockEntities.POLARIZER
import org.shsts.tinactory.content.AllBlockEntities.RESEARCH_BENCH
import org.shsts.tinactory.content.AllBlockEntities.STEAM_TURBINE
import org.shsts.tinactory.content.AllBlockEntities.STONE_GENERATOR
import org.shsts.tinactory.content.AllBlockEntities.THERMAL_CENTRIFUGE
import org.shsts.tinactory.content.AllBlockEntities.WIREMILL
import org.shsts.tinactory.content.AllItems.BUZZSAW
import org.shsts.tinactory.content.AllItems.CABLE
import org.shsts.tinactory.content.AllItems.CHIPS
import org.shsts.tinactory.content.AllItems.CONVEYOR_MODULE
import org.shsts.tinactory.content.AllItems.ELECTRIC_BUFFER
import org.shsts.tinactory.content.AllItems.ELECTRIC_MOTOR
import org.shsts.tinactory.content.AllItems.ELECTRIC_PISTON
import org.shsts.tinactory.content.AllItems.ELECTRIC_PUMP
import org.shsts.tinactory.content.AllItems.EMITTER
import org.shsts.tinactory.content.AllItems.GRINDER
import org.shsts.tinactory.content.AllItems.MACHINE_HULL
import org.shsts.tinactory.content.AllItems.ROBOT_ARM
import org.shsts.tinactory.content.AllItems.SENSOR
import org.shsts.tinactory.content.AllItems.TRANSFORMER
import org.shsts.tinactory.content.electric.Voltage
import org.shsts.tinactory.datagen.content.Technologies
import org.shsts.tinactory.datagen.content.builder.AssemblyRecipeBuilder
import org.shsts.tinactory.datagen.content.builder.RecipeFactories.assembler
import org.shsts.tinactory.datagen.content.component.item
import org.shsts.tinactory.datagen.content.machine.Machines.MACHINE_TICKS

object ProcessingMachines {
    fun init() {
        machine(Voltage.LV, "steel", "copper", "tin", "bronze", "tin")
        machine(Voltage.MV, "aluminium", "cupronickel", "copper", "brass", "bronze")
        machine(Voltage.HV, "stainless_steel", "kanthal", "silver", "stainless_steel", "steel")
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
                input(MACHINE_HULL)
                autoCable = true
                voltage(lastVoltage)
                workTicks(MACHINE_TICKS)
            }
            output(RESEARCH_BENCH) {
                circuit(2)
                input(SENSOR)
                input(EMITTER)
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            output(ASSEMBLER) {
                circuit(2)
                input(ROBOT_ARM, 2)
                input(CONVEYOR_MODULE, 2)
                tech(Technologies.ROBOT_ARM, Technologies.CONVEYOR_MODULE)
            }
            output(LASER_ENGRAVER) {
                circuit(3)
                input(ELECTRIC_PISTON, 2)
                input(EMITTER)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }

            output(CIRCUIT_ASSEMBLER) {
                circuit(4, nextVoltage)
                input(ROBOT_ARM)
                input(EMITTER)
                input(CONVEYOR_MODULE, 2)
                tech(Technologies.INTEGRATED_CIRCUIT)
            }
            output(STONE_GENERATOR) {
                circuit(2)
                input(ELECTRIC_MOTOR)
                input(ELECTRIC_PISTON)
                input(GRINDER)
                input("glass", "primary")
                tech(Technologies.PUMP_AND_PISTON, Technologies.MATERIAL_CUTTING)
            }
            output(ORE_ANALYZER) {
                circuit(2)
                input(ELECTRIC_MOTOR, 3)
                input(SENSOR)
                tech(Technologies.SENSOR_AND_EMITTER)
            }
            output(MACERATOR) {
                circuit(3)
                input(ELECTRIC_PISTON)
                input(CONVEYOR_MODULE)
                input(GRINDER)
                tech(Technologies.CONVEYOR_MODULE, Technologies.MATERIAL_CUTTING)
            }
            output(ORE_WASHER) {
                circuit(2)
                input(ELECTRIC_MOTOR)
                input(rotor, "rotor", 2)
                input("glass", "primary")
                tech(Technologies.MOTOR)
            }
            output(CENTRIFUGE) {
                circuit(4)
                input(ELECTRIC_MOTOR, 2)
                tech(Technologies.MOTOR)
            }
            output(THERMAL_CENTRIFUGE) {
                circuit(2)
                input(ELECTRIC_MOTOR, 2)
                input(heat, "wire", wireNumber)
                tech(Technologies.MOTOR, Technologies.ELECTRIC_HEATING)
            }
            output(ELECTRIC_FURNACE) {
                circuit(2)
                input(heat, "wire", wireNumber)
                input(main, "plate", 4)
                tech(Technologies.ELECTRIC_HEATING)
            }
            output(ALLOY_SMELTER) {
                circuit(4)
                input(heat, "wire", wireNumber * 2)
                input(main, "plate", 8)
                tech(Technologies.ELECTRIC_HEATING)
            }
            output(MIXER) {
                circuit(2)
                input(ELECTRIC_MOTOR)
                input(rotor, "rotor")
                input("glass", "primary", 4)
                tech(Technologies.MOTOR)
            }
            output(POLARIZER) {
                circuit(2)
                input(electric, "wire", wireNumber)
                tech(Technologies.MOTOR)
            }
            output(WIREMILL) {
                circuit(2)
                input(ELECTRIC_MOTOR, 4)
                tech(Technologies.MOTOR)
            }
            output(BENDER) {
                circuit(2)
                input(ELECTRIC_MOTOR, 2)
                input(ELECTRIC_PISTON, 2)
                input(main, "plate", 4)
                tech(Technologies.PUMP_AND_PISTON)
            }
            output(LATHE) {
                circuit(3)
                input(ELECTRIC_MOTOR)
                input(ELECTRIC_PISTON)
                input(GRINDER)
                tech(Technologies.PUMP_AND_PISTON, Technologies.MATERIAL_CUTTING)
            }
            output(CUTTER) {
                circuit(3)
                input(ELECTRIC_MOTOR)
                input(CONVEYOR_MODULE)
                input(BUZZSAW)
                tech(Technologies.CONVEYOR_MODULE, Technologies.MATERIAL_CUTTING)
            }
            output(EXTRUDER) {
                circuit(4)
                input(ELECTRIC_PISTON)
                input(heat, "wire", wireNumber)
                input(pipe, "pipe")
                tech(Technologies.COLD_WORKING)
            }
            output(EXTRACTOR) {
                circuit(2)
                input(ELECTRIC_PISTON)
                input(ELECTRIC_PUMP)
                input(heat, "wire", wireNumber)
                input("glass", "primary", 2)
                tech(Technologies.HOT_WORKING)
            }
            output(FLUID_SOLIDIFIER) {
                circuit(2)
                input(ELECTRIC_PUMP, 2)
                input("glass", "primary", 2)
                tech(Technologies.HOT_WORKING)
            }
            output(ELECTROLYZER) {
                circuit(4)
                input(electric, "wire", wireNumber * 2)
                input("glass", "primary")
                tech(Technologies.ELECTROLYZING)
            }
            output(CHEMICAL_REACTOR) {
                circuit(4)
                input(ELECTRIC_MOTOR, 2)
                input(rotor, "rotor", 2)
                input("glass", "primary", 2)
                tech(Technologies.CHEMISTRY)
            }
            output(ARC_FURNACE) {
                circuit(2)
                input(electric, "wire", wireNumber * 4)
                input("graphite", "dust", 4)
                input(main, "plate", 4)
                tech(Technologies.ARC_FURNACE)
            }
            output(STEAM_TURBINE) {
                circuit(2)
                input(ELECTRIC_MOTOR, 2)
                input(rotor, "rotor", 2)
                input(pipe, "pipe", 2)
                tech(Technologies.MOTOR)
            }
            output(GAS_TURBINE) {
                circuit(3)
                input(ELECTRIC_MOTOR)
                input(ELECTRIC_PUMP)
                input(rotor, "rotor", 2)
                tech(Technologies.PUMP_AND_PISTON)
            }
            output(COMBUSTION_GENERATOR) {
                circuit(3)
                input(ELECTRIC_MOTOR)
                input(ELECTRIC_PISTON)
                input(main, "gear", 2)
                tech(Technologies.PUMP_AND_PISTON)
            }
            output(ELECTRIC_CHEST) {
                circuit(2)
                input(CONVEYOR_MODULE)
                input(main, "plate", 2)
                input(Items.CHEST)
                tech(Technologies.CONVEYOR_MODULE)
            }
            output(ELECTRIC_TANK) {
                circuit(2)
                input(ELECTRIC_PUMP)
                input(main, "plate", 2)
                input("glass", "primary")
                tech(Technologies.PUMP_AND_PISTON)
            }
            output(BATTERY_BOX) {
                circuit(2)
                pic()
                input(CABLE, 4)
                input(Items.CHEST)
                tech(Technologies.BATTERY)
            }
            output(TRANSFORMER) {
                circuit(4)
                pic()
                input(CABLE)
                input(CABLE, 4, voltage = lastVoltage)
                tech(Technologies.BATTERY)
            }
            output(ELECTRIC_BUFFER) {
                circuit(4)
                pic()
                input(CABLE, 2)
                tech(Technologies.BATTERY)
            }
            output(LOGISTIC_WORKER) {
                circuit(4)
                input(CONVEYOR_MODULE, 2)
                input(ELECTRIC_PUMP, 2)
                input(main, "plate", 4)
                tech(Technologies.PUMP_AND_PISTON, Technologies.CONVEYOR_MODULE)
            }
            output(MULTIBLOCK_INTERFACE) {
                circuit(2)
                input(CONVEYOR_MODULE)
                input(ELECTRIC_PUMP)
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
            input(CHIPS.item("low_pic"), 2)
        } else if (v.rank < Voltage.ZPM.rank) {
            input(CHIPS.item("pic"), 2)
        } else {
            input(CHIPS.item("high_pic"), 2)
        }
    }
}
