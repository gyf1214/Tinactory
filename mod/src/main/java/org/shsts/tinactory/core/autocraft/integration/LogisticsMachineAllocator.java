package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.AllBlockEntities;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.network.MachineBlock;
import org.shsts.tinactory.core.util.LocHelper;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsMachineAllocator implements IMachineAllocator {
    private final Supplier<java.util.Collection<LogisticComponent.PortInfo>> visiblePorts;

    public LogisticsMachineAllocator(Supplier<java.util.Collection<LogisticComponent.PortInfo>> visiblePorts) {
        this.visiblePorts = visiblePorts;
    }

    @Override
    public boolean canRun(MachineRequirement requirement) {
        var seenMachines = new HashSet<UUID>();
        for (var info : visiblePorts.get()) {
            var machine = info.machine();
            if (!seenMachines.add(machine.uuid())) {
                continue;
            }
            if (MachineBlock.getBlockVoltage(machine.blockEntity()).rank < requirement.voltageTier()) {
                continue;
            }
            if (supportedRecipeTypes(machine).contains(requirement.recipeTypeId())) {
                return true;
            }
        }
        return false;
    }

    private static Set<ResourceLocation> supportedRecipeTypes(org.shsts.tinactory.api.machine.IMachine machine) {
        var block = machine.blockEntity().getBlockState().getBlock();
        var out = new HashSet<ResourceLocation>();
        for (var entry : AllBlockEntities.MACHINE_SETS.entrySet()) {
            var key = entry.getKey();
            var set = entry.getValue();
            var hasBlock = set.entries().stream().anyMatch($ -> $.get() == block);
            if (!hasBlock) {
                continue;
            }
            if (set instanceof ProcessingSet processingSet) {
                out.add(LocHelper.modLoc(processingSet.recipeType.id()));
            }
            if ("electric_furnace".equals(key)) {
                out.add(SmeltingRecipePatternSource.SMELTING_RECIPE_TYPE_ID);
            }
        }
        return out;
    }
}
