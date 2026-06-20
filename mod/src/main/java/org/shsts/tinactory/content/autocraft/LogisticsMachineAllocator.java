package org.shsts.tinactory.content.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.ChannelMachineRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineRoute;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsMachineAllocator implements IMachineAllocator {
    private final LogisticComponent logistics;
    private final IMachine viewer;

    public LogisticsMachineAllocator(LogisticComponent logistics, IMachine viewer) {
        this.logistics = logistics;
        this.viewer = viewer;
    }

    @Override
    public Optional<IMachineLease> allocate(CraftStep step, Set<UUID> excludedMachineIds) {
        return allocate(step, excludedMachineIds, Optional.empty());
    }

    @Override
    public Optional<IMachineLease> allocate(CraftStep step, UUID machineId) {
        return allocate(step, Set.of(), Optional.of(machineId));
    }

    private Optional<IMachineLease> allocate(
        CraftStep step,
        Set<UUID> excludedMachineIds,
        Optional<UUID> targetMachineId) {

        var constraints = effectiveConstraints(step);
        var machines = groupMachinePorts();
        for (var entry : machines.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            if (excludedMachineIds.contains(entry.getKey()) ||
                targetMachineId.isPresent() && !targetMachineId.get().equals(entry.getKey())) {
                continue;
            }
            var ports = entry.getValue();
            if (ports.isEmpty()) {
                continue;
            }
            var machine = ports.get(0).machine();
            var voltage = MachineBlock.getBlockVoltage(machine.blockEntity());
            if (constraints.stream().anyMatch(constraint -> !constraint.matches(machine, voltage))) {
                continue;
            }
            var lease = buildLease(machine, ports, step, constraints);
            if (lease.isPresent()) {
                return lease;
            }
        }
        return Optional.empty();
    }

    private Map<UUID, List<LogisticComponent.PortInfo>> groupMachinePorts() {
        var grouped = new HashMap<UUID, List<LogisticComponent.PortInfo>>();
        for (var info : logistics.getVisiblePorts(viewer)) {
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
        CraftStep step,
        List<IMachineConstraint> constraints) {
        var inputRoutes = new ArrayList<IMachineRoute>();
        for (var i = 0; i < step.pattern().inputs().size(); i++) {
            var input = step.pattern().inputs().get(i);
            var route = buildInputRoute(input.key(), input.amount() * step.runs(), ports, constraints, i);
            if (route.isEmpty()) {
                return Optional.empty();
            }
            inputRoutes.add(route.get());
        }

        var outputRoutes = new ArrayList<IMachineRoute>();
        for (var i = 0; i < step.pattern().outputs().size(); i++) {
            var output = step.pattern().outputs().get(i);
            var route = buildOutputRoute(output.key(), output.amount() * step.runs(), ports, constraints, i);
            if (route.isEmpty()) {
                return Optional.empty();
            }
            outputRoutes.add(route.get());
        }

        return Optional.of(createLease(machine, inputRoutes, outputRoutes, constraints));
    }

    private static IMachineLease createLease(
        IMachine machine,
        List<IMachineRoute> inputRoutes,
        List<IMachineRoute> outputRoutes,
        List<IMachineConstraint> constraints) {
        var restoreCallbacks = constraints.stream()
            .map(constraint -> constraint.configureLease(machine))
            .flatMap(Optional::stream)
            .toList();
        return new Lease(machine, inputRoutes, outputRoutes, restoreCallbacks);
    }

    private Optional<IMachineRoute> buildInputRoute(
        IStackKey key,
        long amount,
        List<LogisticComponent.PortInfo> ports,
        List<IMachineConstraint> constraints,
        int slotIndex) {
        for (var info : ports) {
            if (!matchesConstraints(PortDirection.INPUT, slotIndex, key, amount, info, constraints)) {
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
        long amount,
        List<LogisticComponent.PortInfo> ports,
        List<IMachineConstraint> constraints,
        int slotIndex) {
        for (var info : ports) {
            if (!matchesConstraints(PortDirection.OUTPUT, slotIndex, key, amount, info, constraints)) {
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

    private static List<IMachineConstraint> effectiveConstraints(CraftStep step) {
        return step.pattern().constraints();
    }

    private static boolean matchesConstraints(
        PortDirection direction,
        int slotIndex,
        IStackKey key,
        long amount,
        LogisticComponent.PortInfo info,
        List<IMachineConstraint> constraints) {
        return constraints.stream().allMatch(constraint -> constraint.matchesRoute(
            direction,
            slotIndex,
            key,
            amount,
            info.portIndex(),
            info.port().type()));
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
        private final IMachine machine;
        private final List<IMachineRoute> inputRoutes;
        private final List<IMachineRoute> outputRoutes;
        private final List<Runnable> restoreCallbacks;
        private boolean released;

        private Lease(
            IMachine machine,
            List<IMachineRoute> inputRoutes,
            List<IMachineRoute> outputRoutes,
            List<Runnable> restoreCallbacks) {
            this.machine = machine;
            this.inputRoutes = List.copyOf(inputRoutes);
            this.outputRoutes = List.copyOf(outputRoutes);
            this.restoreCallbacks = List.copyOf(restoreCallbacks);
        }

        @Override
        public UUID machineId() {
            return machine.uuid();
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
            if (released) {
                return;
            }
            released = true;
            for (var i = restoreCallbacks.size() - 1; i >= 0; i--) {
                restoreCallbacks.get(i).run();
            }
        }
    }
}
