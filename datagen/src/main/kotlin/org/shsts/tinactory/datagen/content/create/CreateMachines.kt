package org.shsts.tinactory.datagen.content.create

import net.neoforged.neoforge.common.conditions.ModLoadedCondition
import org.shsts.tinactory.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.compat.create.CreateIntegration
import org.shsts.tinactory.content.machine.MachineSet
import org.shsts.tinactory.core.electric.Voltage
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.machine.Machines.machineModel

object CreateMachines {
    private val voltages = listOf(Voltage.ULV, Voltage.MV, Voltage.EV)

    fun init() {
        machine(CreateIntegration.MOTORS)
        machine(CreateIntegration.GENERATORS)
    }

    private fun machine(set: MachineSet) {
        for (voltage in voltages) {
            blockData(set.entry(voltage)) {
                machineModel {
                    casing(voltage)
                    overlay("machines/electric_furnace")
                }
                conditions(ModLoadedCondition("create"))
                tag(MINEABLE_WITH_WRENCH)
            }
        }
    }
}
