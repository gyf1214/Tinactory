package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.network.MachineBlock;

import java.util.HashSet;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsMachineAllocator implements IMachineAllocator {
    private final Supplier<java.util.Collection<LogisticComponent.PortInfo>> visiblePorts;
    private final ToIntFunction<IMachine> voltageRankResolver;
    private final BiPredicate<IMachine, ResourceLocation> recipeSupport;

    public LogisticsMachineAllocator(Supplier<java.util.Collection<LogisticComponent.PortInfo>> visiblePorts) {
        this(
            visiblePorts,
            machine -> MachineBlock.getBlockVoltage(machine.blockEntity()).rank,
            (machine, recipeTypeId) -> machine.processor()
                .filter(IMachineProcessor.class::isInstance)
                .map(IMachineProcessor.class::cast)
                .map(processor -> processor.supportsRecipeType(recipeTypeId))
                .orElse(false));
    }

    public LogisticsMachineAllocator(
        Supplier<java.util.Collection<LogisticComponent.PortInfo>> visiblePorts,
        ToIntFunction<IMachine> voltageRankResolver,
        BiPredicate<IMachine, ResourceLocation> recipeSupport) {

        this.visiblePorts = visiblePorts;
        this.voltageRankResolver = voltageRankResolver;
        this.recipeSupport = recipeSupport;
    }

    @Override
    public boolean canRun(MachineRequirement requirement) {
        var seenMachines = new HashSet<UUID>();
        for (var info : visiblePorts.get()) {
            var machine = info.machine();
            if (!seenMachines.add(machine.uuid())) {
                continue;
            }
            if (voltageRankResolver.applyAsInt(machine) < requirement.voltageTier()) {
                continue;
            }
            if (recipeSupport.test(machine, requirement.recipeTypeId())) {
                return true;
            }
        }
        return false;
    }
}
