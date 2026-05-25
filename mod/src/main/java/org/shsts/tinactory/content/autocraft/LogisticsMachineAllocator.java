package org.shsts.tinactory.content.autocraft;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.IStackKey;
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
import org.shsts.tinactory.core.autocraft.pattern.TargetRecipeConstraint;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.logistics.CraftPortChannel;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.network.MachineBlock;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class LogisticsMachineAllocator implements IMachineAllocator {
    private static final String TARGET_RECIPE_CONFIG_KEY = "targetRecipe";
    private final LogisticComponent logistics;

    public LogisticsMachineAllocator(LogisticComponent logistics) {
        this.logistics = logistics;
    }

    @Override
    public Optional<IMachineLease> allocate(CraftStep step) {
        var targetRecipe = targetRecipeSelection(step);
        if (!targetRecipe.valid()) {
            return Optional.empty();
        }
        var machines = groupMachinePorts();
        for (var entry : machines.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            var ports = entry.getValue();
            if (ports.isEmpty()) {
                continue;
            }
            var machine = ports.get(0).machine();
            if (MachineBlock.getBlockVoltage(machine.blockEntity()).rank <
                step.pattern().machineRequirement().voltageTier()) {
                continue;
            }
            var processor = machine.processor()
                .filter(IMachineProcessor.class::isInstance)
                .map(IMachineProcessor.class::cast);
            if (processor.filter($ -> $.supportsRecipeType(step.pattern().machineRequirement().recipeTypeId()))
                .isEmpty()) {
                continue;
            }
            if (targetRecipe.recipeId().isPresent() &&
                processor.filter($ -> $.allowTargetRecipe(targetRecipe.recipeId().get())).isEmpty()) {
                continue;
            }
            var lease = buildLease(machine, ports, step, targetRecipe.recipeId());
            if (lease.isPresent()) {
                return lease;
            }
        }
        return Optional.empty();
    }

    private Map<UUID, List<LogisticComponent.PortInfo>> groupMachinePorts() {
        var grouped = new HashMap<UUID, List<LogisticComponent.PortInfo>>();
        for (var info : logistics.getAllPorts()) {
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
        Optional<ResourceLocation> targetRecipe) {
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

        return Optional.of(createLease(machine, inputRoutes, outputRoutes, targetRecipe));
    }

    private static TargetRecipeSelection targetRecipeSelection(CraftStep step) {
        var targetRecipe = Optional.<ResourceLocation>empty();
        for (var constraint : step.pattern().machineRequirement().constraints()) {
            if (!(constraint instanceof TargetRecipeConstraint targetRecipeConstraint)) {
                continue;
            }
            if (targetRecipe.isPresent() && !targetRecipe.get().equals(targetRecipeConstraint.recipeId())) {
                return new TargetRecipeSelection(false, Optional.empty());
            }
            targetRecipe = Optional.of(targetRecipeConstraint.recipeId());
        }
        return new TargetRecipeSelection(true, targetRecipe);
    }

    private static IMachineLease createLease(
        IMachine machine,
        List<IMachineRoute> inputRoutes,
        List<IMachineRoute> outputRoutes,
        Optional<ResourceLocation> targetRecipe) {
        if (targetRecipe.isEmpty()) {
            return new Lease(machine, inputRoutes, outputRoutes, false, Optional.empty());
        }
        var previousTargetRecipe = machine.config().getLoc(TARGET_RECIPE_CONFIG_KEY);
        machine.setConfig(SetMachineConfigPacket.builder()
            .set(TARGET_RECIPE_CONFIG_KEY, targetRecipe.get())
            .get());
        return new Lease(machine, inputRoutes, outputRoutes, true, previousTargetRecipe);
    }

    private static void restoreTargetRecipe(IMachine machine, Optional<ResourceLocation> previousTargetRecipe) {
        var builder = SetMachineConfigPacket.builder();
        previousTargetRecipe.ifPresentOrElse(
            recipeId -> builder.set(TARGET_RECIPE_CONFIG_KEY, recipeId),
            () -> builder.reset(TARGET_RECIPE_CONFIG_KEY));
        machine.setConfig(builder.get());
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
            .filter(constraint -> constraint.index() == slotIndex)
            .toList();
    }

    private static List<PortConstraint> outputConstraints(CraftStep step, int slotIndex) {
        return step.pattern().machineRequirement().constraints().stream()
            .filter(PortConstraint.class::isInstance)
            .map(PortConstraint.class::cast)
            .filter(constraint -> constraint.direction() == PortDirection.OUTPUT)
            .filter(constraint -> constraint.index() == slotIndex)
            .toList();
    }

    private static boolean matchesConstraints(
        LogisticComponent.PortInfo info,
        List<PortConstraint> constraints) {
        return constraints.stream().allMatch($ -> $.port() == info.portIndex());
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

    private record TargetRecipeSelection(boolean valid, Optional<ResourceLocation> recipeId) {}

    private static final class Lease implements IMachineLease {
        private final IMachine machine;
        private final List<IMachineRoute> inputRoutes;
        private final List<IMachineRoute> outputRoutes;
        private final boolean restoreTargetRecipe;
        private final Optional<ResourceLocation> previousTargetRecipe;
        private boolean released;

        private Lease(
            IMachine machine,
            List<IMachineRoute> inputRoutes,
            List<IMachineRoute> outputRoutes,
            boolean restoreTargetRecipe,
            Optional<ResourceLocation> previousTargetRecipe) {
            this.machine = machine;
            this.inputRoutes = List.copyOf(inputRoutes);
            this.outputRoutes = List.copyOf(outputRoutes);
            this.restoreTargetRecipe = restoreTargetRecipe;
            this.previousTargetRecipe = previousTargetRecipe;
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
            if (restoreTargetRecipe) {
                restoreTargetRecipe(machine, previousTargetRecipe);
            }
        }
    }
}
