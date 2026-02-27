package org.shsts.tinactory.core.autocraft.integration;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.core.autocraft.api.IMachineAllocator;
import org.shsts.tinactory.core.autocraft.api.IMachineInputRoute;
import org.shsts.tinactory.core.autocraft.api.IMachineLease;
import org.shsts.tinactory.core.autocraft.api.IMachineOutputRoute;
import org.shsts.tinactory.core.autocraft.pattern.CraftKey;
import org.shsts.tinactory.core.autocraft.pattern.InputPortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.OutputPortConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
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
        var inputRoutes = new ArrayList<IMachineInputRoute>();
        for (var i = 0; i < step.pattern().inputs().size(); i++) {
            var input = step.pattern().inputs().get(i);
            var route = buildInputRoute(input.key(), ports, inputConstraints(step, i));
            if (route.isEmpty()) {
                return Optional.empty();
            }
            inputRoutes.add(route.get());
        }

        var outputRoutes = new ArrayList<IMachineOutputRoute>();
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

    private Optional<IMachineInputRoute> buildInputRoute(
        CraftKey key,
        List<LogisticComponent.PortInfo> ports,
        List<InputPortConstraint> constraints) {
        for (var info : ports) {
            if (!matchesInputConstraints(key, info, constraints)) {
                continue;
            }
            var route = switch (key.type()) {
                case ITEM -> buildItemInputRoute(info.port(), key);
                case FLUID -> buildFluidInputRoute(info.port(), key);
            };
            if (route.isPresent()) {
                return route;
            }
        }
        return Optional.empty();
    }

    private Optional<IMachineOutputRoute> buildOutputRoute(
        CraftKey key,
        List<LogisticComponent.PortInfo> ports,
        List<OutputPortConstraint> constraints) {
        for (var info : ports) {
            if (!matchesOutputConstraints(key, info, constraints)) {
                continue;
            }
            var route = switch (key.type()) {
                case ITEM -> buildItemOutputRoute(info.port(), key);
                case FLUID -> buildFluidOutputRoute(info.port(), key);
            };
            if (route.isPresent()) {
                return route;
            }
        }
        return Optional.empty();
    }

    private static List<InputPortConstraint> inputConstraints(CraftStep step, int slotIndex) {
        return step.pattern().machineRequirement().constraints().stream()
            .filter(InputPortConstraint.class::isInstance)
            .map(InputPortConstraint.class::cast)
            .filter(constraint -> constraint.inputSlotIndex() == slotIndex)
            .toList();
    }

    private static List<OutputPortConstraint> outputConstraints(CraftStep step, int slotIndex) {
        return step.pattern().machineRequirement().constraints().stream()
            .filter(OutputPortConstraint.class::isInstance)
            .map(OutputPortConstraint.class::cast)
            .filter(constraint -> constraint.outputSlotIndex() == slotIndex)
            .toList();
    }

    private static boolean matchesInputConstraints(
        CraftKey key,
        LogisticComponent.PortInfo info,
        List<InputPortConstraint> constraints) {
        for (var constraint : constraints) {
            if (constraint.portIndex() != null && constraint.portIndex() != info.portIndex()) {
                return false;
            }
            if (constraint.direction() != null &&
                !matchesDirection(key, info.port(), constraint.direction() == InputPortConstraint.Direction.INPUT)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesOutputConstraints(
        CraftKey key,
        LogisticComponent.PortInfo info,
        List<OutputPortConstraint> constraints) {
        for (var constraint : constraints) {
            if (constraint.portIndex() != null && constraint.portIndex() != info.portIndex()) {
                return false;
            }
            if (constraint.direction() != null &&
                !matchesDirection(key, info.port(), constraint.direction() == OutputPortConstraint.Direction.INPUT)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesDirection(CraftKey key, IPort<?> port, boolean inputDirection) {
        if (!inputDirection) {
            return port.acceptOutput();
        }
        return true;
    }

    private static Optional<IMachineInputRoute> buildItemInputRoute(IPort<?> port, CraftKey key) {
        if (port.type() != PortType.ITEM) {
            return Optional.empty();
        }
        IPort<ItemStack> itemPort = port.asItem();
        return Optional.of(new IMachineInputRoute() {
            @Override
            public CraftKey key() {
                return key;
            }

            @Override
            public long push(long amount, boolean simulate) {
                if (amount <= 0L) {
                    return 0L;
                }
                var total = 0L;
                var left = amount;
                while (left > 0L) {
                    var chunk = (int) Math.min(Integer.MAX_VALUE, left);
                    var stack = LogisticsInventoryView.toItemStack(key, chunk);
                    if (!itemPort.acceptInput(stack)) {
                        break;
                    }
                    var remaining = itemPort.insert(stack, simulate);
                    var moved = chunk - remaining.getCount();
                    total += moved;
                    left -= moved;
                    if (moved <= 0L) {
                        break;
                    }
                }
                return total;
            }
        });
    }

    private static Optional<IMachineOutputRoute> buildItemOutputRoute(IPort<?> port, CraftKey key) {
        if (port.type() != PortType.ITEM || !port.acceptOutput()) {
            return Optional.empty();
        }
        IPort<ItemStack> itemPort = port.asItem();
        return Optional.of(new IMachineOutputRoute() {
            @Override
            public CraftKey key() {
                return key;
            }

            @Override
            public long pull(long amount, boolean simulate) {
                if (amount <= 0L) {
                    return 0L;
                }
                var total = 0L;
                var left = amount;
                while (left > 0L) {
                    var chunk = (int) Math.min(Integer.MAX_VALUE, left);
                    var extracted = itemPort.extract(LogisticsInventoryView.toItemStack(key, chunk), simulate);
                    total += extracted.getCount();
                    left -= extracted.getCount();
                    if (extracted.isEmpty()) {
                        break;
                    }
                }
                return total;
            }
        });
    }

    private static Optional<IMachineInputRoute> buildFluidInputRoute(IPort<?> port, CraftKey key) {
        if (port.type() != PortType.FLUID) {
            return Optional.empty();
        }
        IPort<FluidStack> fluidPort = port.asFluid();
        return Optional.of(new IMachineInputRoute() {
            @Override
            public CraftKey key() {
                return key;
            }

            @Override
            public long push(long amount, boolean simulate) {
                if (amount <= 0L) {
                    return 0L;
                }
                var total = 0L;
                var left = amount;
                while (left > 0L) {
                    var chunk = (int) Math.min(Integer.MAX_VALUE, left);
                    var fluid = LogisticsInventoryView.toFluidStack(key, chunk);
                    if (!fluidPort.acceptInput(fluid)) {
                        break;
                    }
                    var remaining = fluidPort.insert(fluid, simulate);
                    var moved = chunk - remaining.getAmount();
                    total += moved;
                    left -= moved;
                    if (moved <= 0L) {
                        break;
                    }
                }
                return total;
            }
        });
    }

    private static Optional<IMachineOutputRoute> buildFluidOutputRoute(IPort<?> port, CraftKey key) {
        if (port.type() != PortType.FLUID || !port.acceptOutput()) {
            return Optional.empty();
        }
        IPort<FluidStack> fluidPort = port.asFluid();
        return Optional.of(new IMachineOutputRoute() {
            @Override
            public CraftKey key() {
                return key;
            }

            @Override
            public long pull(long amount, boolean simulate) {
                if (amount <= 0L) {
                    return 0L;
                }
                var total = 0L;
                var left = amount;
                while (left > 0L) {
                    var chunk = (int) Math.min(Integer.MAX_VALUE, left);
                    var extracted = fluidPort.extract(LogisticsInventoryView.toFluidStack(key, chunk), simulate);
                    total += extracted.getAmount();
                    left -= extracted.getAmount();
                    if (extracted.isEmpty()) {
                        break;
                    }
                }
                return total;
            }
        });
    }

    private static final class Lease implements IMachineLease {
        private final UUID machineId;
        private final List<IMachineInputRoute> inputRoutes;
        private final List<IMachineOutputRoute> outputRoutes;
        private boolean released;

        private Lease(UUID machineId, List<IMachineInputRoute> inputRoutes, List<IMachineOutputRoute> outputRoutes) {
            this.machineId = machineId;
            this.inputRoutes = List.copyOf(inputRoutes);
            this.outputRoutes = List.copyOf(outputRoutes);
        }

        @Override
        public UUID machineId() {
            return machineId;
        }

        @Override
        public List<IMachineInputRoute> inputRoutes() {
            return inputRoutes;
        }

        @Override
        public List<IMachineOutputRoute> outputRoutes() {
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
