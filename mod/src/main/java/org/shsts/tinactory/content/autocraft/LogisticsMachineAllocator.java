package org.shsts.tinactory.content.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.ChannelMachineRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.core.logistics.IStackKey;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsMachineAllocator implements IMachineAllocator {
    private final Supplier<Collection<LogisticComponent.PortInfo>> visiblePorts;
    private final ToIntFunction<IMachine> voltageRankResolver;
    private final BiPredicate<IMachine, ResourceLocation> recipeSupport;

    public LogisticsMachineAllocator(Supplier<Collection<LogisticComponent.PortInfo>> visiblePorts) {
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
        Supplier<Collection<LogisticComponent.PortInfo>> visiblePorts,
        ToIntFunction<IMachine> voltageRankResolver,
        BiPredicate<IMachine, ResourceLocation> recipeSupport) {

        this.visiblePorts = visiblePorts;
        this.voltageRankResolver = voltageRankResolver;
        this.recipeSupport = recipeSupport;
    }

    @Override
    public Optional<IMachineLease> allocate(CraftStep step) {
        var machines = groupMachinePorts();
        for (var entry : machines.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            var ports = entry.getValue();
            if (ports.isEmpty()) {
                continue;
            }
            var machine = ports.get(0).machine();
            if (voltageRankResolver.applyAsInt(machine) < step.pattern().machineRequirement().voltageTier()) {
                continue;
            }
            if (!recipeSupport.test(machine, step.pattern().machineRequirement().recipeTypeId())) {
                continue;
            }
            var lease = buildLease(machine, ports, step);
            if (lease.isPresent()) {
                return lease;
            }
        }
        return Optional.empty();
    }

    private Map<UUID, List<LogisticComponent.PortInfo>> groupMachinePorts() {
        var grouped = new HashMap<UUID, List<LogisticComponent.PortInfo>>();
        for (var info : visiblePorts.get()) {
            grouped.computeIfAbsent(info.machine().uuid(), $ -> new ArrayList<>()).add(info);
        }
        for (var ports : grouped.values()) {
            ports.sort(Comparator.comparing(LogisticComponent.PortInfo::portIndex));
        }
        return grouped;
    }

    private Optional<IMachineLease> buildLease(
        IMachine machine,
        List<LogisticComponent.PortInfo> ports,
        CraftStep step) {
        var inputRoutes = new ArrayList<IMachineRoute>();
        for (var i = 0; i < step.pattern().inputs().size(); i++) {
            var input = step.pattern().inputs().get(i);
            var route = buildInputRoute(input.key(), ports, inputConstraints(step, i));
            if (route.isEmpty()) {
                return Optional.empty();
            }
            inputRoutes.add(route.get());
        }

        var outputRoutes = new ArrayList<IMachineRoute>();
        for (var i = 0; i < step.pattern().outputs().size(); i++) {
            var output = step.pattern().outputs().get(i);
            var route = buildOutputRoute(output.key(), ports, outputConstraints(step, i));
            if (route.isEmpty()) {
                return Optional.empty();
            }
            outputRoutes.add(route.get());
        }

        return Optional.of(new Lease(machine.uuid(), inputRoutes, outputRoutes));
    }

    private Optional<IMachineRoute> buildInputRoute(
        IStackKey key,
        List<LogisticComponent.PortInfo> ports,
        List<PortConstraint> constraints) {
        for (var info : ports) {
            if (!matchesConstraints(info, constraints)) {
                continue;
            }
            var route = switch (key.type()) {
                case ITEM -> buildItemInputRoute(info.port(), key);
                case FLUID -> buildFluidInputRoute(info.port(), key);
                case NONE -> Optional.<IMachineRoute>empty();
            };
            if (route.isPresent()) {
                return route;
            }
        }
        return Optional.empty();
    }

    private Optional<IMachineRoute> buildOutputRoute(
        IStackKey key,
        List<LogisticComponent.PortInfo> ports,
        List<PortConstraint> constraints) {
        for (var info : ports) {
            if (!matchesConstraints(info, constraints)) {
                continue;
            }
            var route = switch (key.type()) {
                case ITEM -> buildItemOutputRoute(info.port(), key);
                case FLUID -> buildFluidOutputRoute(info.port(), key);
                case NONE -> Optional.<IMachineRoute>empty();
            };
            if (route.isPresent()) {
                return route;
            }
        }
        return Optional.empty();
    }

    private static List<PortConstraint> inputConstraints(CraftStep step, int slotIndex) {
        return step.pattern().machineRequirement().constraints().stream()
            .filter(PortConstraint.class::isInstance)
            .map(PortConstraint.class::cast)
            .filter(constraint -> constraint.direction() == PortDirection.INPUT)
            .filter(constraint -> constraint.slotIndex() == slotIndex)
            .toList();
    }

    private static List<PortConstraint> outputConstraints(CraftStep step, int slotIndex) {
        return step.pattern().machineRequirement().constraints().stream()
            .filter(PortConstraint.class::isInstance)
            .map(PortConstraint.class::cast)
            .filter(constraint -> constraint.direction() == PortDirection.OUTPUT)
            .filter(constraint -> constraint.slotIndex() == slotIndex)
            .toList();
    }

    private static boolean matchesConstraints(
        LogisticComponent.PortInfo info,
        List<PortConstraint> constraints) {
        for (var constraint : constraints) {
            if (constraint.portIndex() != null && constraint.portIndex() != info.portIndex()) {
                return false;
            }
        }
        return true;
    }

    private static Optional<IMachineRoute> buildItemInputRoute(IPort<?> port, IStackKey key) {
        return itemChannel(port)
            .map(channel -> new ChannelMachineRoute<>(key, PortDirection.INPUT, channel));
    }

    private static Optional<IMachineRoute> buildItemOutputRoute(IPort<?> port, IStackKey key) {
        if (!port.acceptOutput()) {
            return Optional.empty();
        }
        return itemChannel(port)
            .map(channel -> new ChannelMachineRoute<>(key, PortDirection.OUTPUT, channel));
    }

    private static Optional<IMachineRoute> buildFluidInputRoute(IPort<?> port, IStackKey key) {
        return fluidChannel(port)
            .map(channel -> new ChannelMachineRoute<>(key, PortDirection.INPUT, channel));
    }

    private static Optional<IMachineRoute> buildFluidOutputRoute(IPort<?> port, IStackKey key) {
        if (!port.acceptOutput()) {
            return Optional.empty();
        }
        return fluidChannel(port)
            .map(channel -> new ChannelMachineRoute<>(key, PortDirection.OUTPUT, channel));
    }

    private static Optional<CraftPortChannel<ItemStack>> itemChannel(IPort<?> port) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
        return Optional.of(new CraftPortChannel<>(
            StackHelper.ITEM_ADAPTER,
            port.asItem()));
    }

    private static Optional<CraftPortChannel<FluidStack>> fluidChannel(IPort<?> port) {
        if (port.type() != PortType.FLUID) {
            return Optional.empty();
        }
        return Optional.of(new CraftPortChannel<>(
            StackHelper.FLUID_ADAPTER,
            port.asFluid()));
    }

    private static final class Lease implements IMachineLease {
        private final UUID machineId;
        private final List<IMachineRoute> inputRoutes;
        private final List<IMachineRoute> outputRoutes;
        private boolean released;

        private Lease(UUID machineId, List<IMachineRoute> inputRoutes, List<IMachineRoute> outputRoutes) {
            this.machineId = machineId;
            this.inputRoutes = List.copyOf(inputRoutes);
            this.outputRoutes = List.copyOf(outputRoutes);
        }

        @Override
        public UUID machineId() {
            return machineId;
        }

        @Override
        public List<IMachineRoute> inputRoutes() {
            return inputRoutes;
        }

        @Override
        public List<IMachineRoute> outputRoutes() {
            return outputRoutes;
        }

        @Override
        public boolean isValid() {
            return !released;
        }

        @Override
        public void release() {
            released = true;
        }
    }
}
