package org.shsts.tinactory.datagen.content.create

import net.neoforged.neoforge.common.conditions.ModLoadedCondition
import org.shsts.tinactory.AllBlockEntities.getMachine
import org.shsts.tinactory.AllTags.MINEABLE_WITH_WRENCH
import org.shsts.tinactory.datagen.content.builder.DataFactories.blockData
import org.shsts.tinactory.datagen.content.machine.Machines.machineModel

object CreateMachines {
    fun init() {
        machine("electric_motor", "overlay/machine/overlay_muffler")
        machine("kinetic_generator", "overlay/machine/overlay_rotor_holder")
    }

    private fun machine(name: String, overlay: String) {
        val set = getMachine("create/$name")
        for (voltage in set.voltages) {
            blockData(set.entry(voltage)) {
                machineModel {
                    casing(voltage)
                    overlay(overlay)
                }
                conditions(ModLoadedCondition("create"))
                tag(MINEABLE_WITH_WRENCH)
            }
        }
    }
}
