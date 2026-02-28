package org.shsts.tinactory.unit.autocraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IFluidPort;
import org.shsts.tinactory.api.logistics.IItemPort;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.logistics.LogisticComponent.PortInfo;
import org.shsts.tinactory.core.autocraft.integration.LogisticsMachineAllocator;
import org.shsts.tinactory.core.autocraft.integration.LogisticsPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.InputPortConstraint;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;
import org.shsts.tinactory.core.autocraft.plan.CraftStep;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogisticsAdapterContractTest {
    @Test
    void repositoryShouldReturnPatternsSortedByPatternId() {
        var key = CraftKey.item("minecraft:iron_ingot", "");
        var second = pattern("tinactory:z_second", key);
        var first = pattern("tinactory:a_first", key);
        var repo = new LogisticsPatternRepository(List.of(second, first));

        var actual = repo.findPatternsProducing(key);

        assertEquals(List.of("tinactory:a_first", "tinactory:z_second"),
            actual.stream().map(CraftPattern::patternId).toList());
    }

    @Test
    void allocatorShouldAllocateLeaseWithRecipeTypeCapabilityProbe() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "smelting"), 0, List.of());
        var machine = new FakeMachine();
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new PortInfo(machine, 0, new FakeItemPort(), BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> recipeTypeId.equals(requirement.recipeTypeId()));

        var lease = allocator.allocate(new CraftStep("s1", new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:stone", ""), 1)),
            requirement), 1));

        assertTrue(lease.isPresent());
        assertEquals(machine.uuid(), lease.orElseThrow().machineId());
    }

    @Test
    void allocatorShouldFailWhenCapabilityProbeRejectsRecipeType() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "smelting"), 0, List.of());
        var machine = new FakeMachine();
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new PortInfo(machine, 0, IPort.EMPTY, BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> false);

        var lease = allocator.allocate(new CraftStep("s1", new CraftPattern(
            "tinactory:test",
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(CraftKey.item("minecraft:stone", ""), 1)),
            requirement), 1));

        assertTrue(lease.isEmpty());
    }

    @Test
    void leaseReleaseShouldBeIdempotent() {
        var lease = new TestLease();

        lease.release();
        lease.release();

        assertFalse(lease.isValid());
    }

    @Test
    void allocatorShouldMapDuplicateInputSlotsToDistinctConstrainedPorts() {
        var ore = CraftKey.item("minecraft:iron_ore", "");
        var ingot = CraftKey.item("minecraft:iron_ingot", "");
        var firstMachine = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var secondMachine = new FakeMachine("22222222-2222-2222-2222-222222222222");
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(
                new PortInfo(firstMachine, 0, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0),
                new PortInfo(firstMachine, 1, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0),
                new PortInfo(secondMachine, 0, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0),
                new PortInfo(secondMachine, 2, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> true);
        var step = new CraftStep(
            "s1",
            new CraftPattern(
                "tinactory:double_ore",
                List.of(new CraftAmount(ore, 1), new CraftAmount(ore, 1)),
                List.of(new CraftAmount(ingot, 1)),
                new MachineRequirement(
                    new ResourceLocation("tinactory", "smelting"),
                    0,
                    List.of(new InputPortConstraint(0, 0, null), new InputPortConstraint(1, 2, null)))),
            1);

        var lease = allocator.allocate(step).orElseThrow();

        assertEquals(secondMachine.uuid(), lease.machineId());
    }

    @Test
    void allocatorShouldHonorDirectionConstraintForInputRoutes() {
        var ore = CraftKey.item("minecraft:iron_ore", "");
        var ingot = CraftKey.item("minecraft:iron_ingot", "");
        var firstMachine = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var secondMachine = new FakeMachine("22222222-2222-2222-2222-222222222222");
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(
                new PortInfo(firstMachine, 0, new TrackingItemPort(true, false, 32), BlockPos.ZERO, 0),
                new PortInfo(firstMachine, 1, new TrackingFluidPort(true, 1000), BlockPos.ZERO, 0),
                new PortInfo(secondMachine, 0, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> true);
        var step = new CraftStep(
            "s1",
            new CraftPattern(
                "tinactory:directed",
                List.of(new CraftAmount(ore, 1)),
                List.of(new CraftAmount(ingot, 1)),
                new MachineRequirement(
                    new ResourceLocation("tinactory", "smelting"),
                    0,
                    List.of(new InputPortConstraint(0, null, InputPortConstraint.Direction.OUTPUT)))),
            1);

        var lease = allocator.allocate(step).orElseThrow();

        assertEquals(secondMachine.uuid(), lease.machineId());
    }

    @Test
    void allocatorShouldSkipMachineWhenAnyConstrainedSlotCannotRouteAndFallbackToNext() {
        var ore = CraftKey.item("minecraft:iron_ore", "");
        var ingot = CraftKey.item("minecraft:iron_ingot", "");
        var firstMachine = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var secondMachine = new FakeMachine("22222222-2222-2222-2222-222222222222");
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(
                new PortInfo(firstMachine, 0, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0),
                new PortInfo(secondMachine, 0, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0),
                new PortInfo(secondMachine, 1, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> true);
        var step = new CraftStep(
            "s1",
            new CraftPattern(
                "tinactory:double_ore",
                List.of(new CraftAmount(ore, 1), new CraftAmount(ore, 1)),
                List.of(new CraftAmount(ingot, 1)),
                new MachineRequirement(
                    new ResourceLocation("tinactory", "smelting"),
                    0,
                    List.of(new InputPortConstraint(0, 0, null), new InputPortConstraint(1, 1, null)))),
            1);

        var lease = allocator.allocate(step).orElseThrow();

        assertEquals(secondMachine.uuid(), lease.machineId());
    }

    @Test
    void allocatorShouldReturnEmptyWhenNoMachineMatchesSlotConstraints() {
        var ore = CraftKey.item("minecraft:iron_ore", "");
        var ingot = CraftKey.item("minecraft:iron_ingot", "");
        var machine = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new PortInfo(machine, 0, new TrackingItemPort(true, true, 32), BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> true);
        var step = new CraftStep(
            "s1",
            new CraftPattern(
                "tinactory:double_ore",
                List.of(new CraftAmount(ore, 1), new CraftAmount(ore, 1)),
                List.of(new CraftAmount(ingot, 1)),
                new MachineRequirement(
                    new ResourceLocation("tinactory", "smelting"),
                    0,
                    List.of(new InputPortConstraint(0, 0, null), new InputPortConstraint(1, 1, null)))),
            1);

        assertTrue(allocator.allocate(step).isEmpty());
    }

    private static CraftPattern pattern(String id, CraftKey key) {
        return new CraftPattern(
            id,
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(key, 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }

    private static final class TestLease implements org.shsts.tinactory.core.autocraft.api.IMachineLease {
        private boolean released;

        @Override
        public UUID machineId() {
            return UUID.randomUUID();
        }

        @Override
        public List<org.shsts.tinactory.core.autocraft.api.IMachineInputRoute> inputRoutes() {
            return List.of();
        }

        @Override
        public List<org.shsts.tinactory.core.autocraft.api.IMachineOutputRoute> outputRoutes() {
            return List.of();
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

    private static final class FakeItemPort implements IItemPort {
        @Override
        public boolean acceptInput(ItemStack stack) {
            return true;
        }

        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(ItemStack item, boolean simulate) {
            return item.copy();
        }

        @Override
        public ItemStack extractItem(int limit, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getItemCount(ItemStack item) {
            return 64;
        }

        @Override
        public Collection<ItemStack> getAllItems() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return true;
        }
    }

    private static final class TrackingItemPort implements IItemPort {
        private final boolean allowInput;
        private final boolean allowOutput;
        private int available;

        private TrackingItemPort(boolean allowInput, boolean allowOutput, int available) {
            this.allowInput = allowInput;
            this.allowOutput = allowOutput;
            this.available = available;
        }

        @Override
        public boolean acceptInput(ItemStack stack) {
            return allowInput;
        }

        @Override
        public ItemStack insertItem(ItemStack stack, boolean simulate) {
            if (!allowInput || stack.isEmpty()) {
                return stack.copy();
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(ItemStack item, boolean simulate) {
            if (!allowOutput || item.isEmpty()) {
                return ItemStack.EMPTY;
            }
            var moved = Math.min(item.getCount(), available);
            if (moved <= 0) {
                return ItemStack.EMPTY;
            }
            if (!simulate) {
                available -= moved;
            }
            var ret = item.copy();
            ret.setCount(moved);
            return ret;
        }

        @Override
        public ItemStack extractItem(int limit, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getItemCount(ItemStack item) {
            return allowOutput ? available : 0;
        }

        @Override
        public Collection<ItemStack> getAllItems() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return allowOutput;
        }
    }

    private static final class TrackingFluidPort implements IFluidPort {
        private final boolean allowOutput;
        private int available;

        private TrackingFluidPort(boolean allowOutput, int available) {
            this.allowOutput = allowOutput;
            this.available = available;
        }

        @Override
        public boolean acceptInput(FluidStack stack) {
            return false;
        }

        @Override
        public FluidStack fill(FluidStack fluid, boolean simulate) {
            return fluid;
        }

        @Override
        public FluidStack drain(FluidStack fluid, boolean simulate) {
            if (!allowOutput || fluid.isEmpty()) {
                return FluidStack.EMPTY;
            }
            var moved = Math.min(fluid.getAmount(), available);
            if (moved <= 0) {
                return FluidStack.EMPTY;
            }
            if (!simulate) {
                available -= moved;
            }
            var ret = fluid.copy();
            ret.setAmount(moved);
            return ret;
        }

        @Override
        public FluidStack drain(int limit, boolean simulate) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getFluidAmount(FluidStack fluid) {
            return allowOutput ? available : 0;
        }

        @Override
        public Collection<FluidStack> getAllFluids() {
            return List.of();
        }

        @Override
        public boolean acceptOutput() {
            return allowOutput;
        }
    }

    private static final class FakeMachine implements IMachine {
        private final UUID id;

        private FakeMachine(String id) {
            this.id = UUID.fromString(id);
        }

        private FakeMachine() {
            this("11111111-1111-1111-1111-111111111111");
        }

        @Override
        public UUID uuid() {
            return id;
        }

        @Override
        public Optional<ITeamProfile> owner() {
            return Optional.empty();
        }

        @Override
        public boolean canPlayerInteract(Player player) {
            return true;
        }

        @Override
        public IMachineConfig config() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setConfig(ISetMachineConfigPacket packet, boolean invokeUpdate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Component title() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ItemStack icon() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockEntity blockEntity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<BlockState> workBlock() {
            return Optional.empty();
        }

        @Override
        public Optional<IProcessor> processor() {
            return Optional.empty();
        }

        @Override
        public Optional<IContainer> container() {
            return Optional.empty();
        }

        @Override
        public Optional<IElectricMachine> electric() {
            return Optional.empty();
        }

        @Override
        public Optional<INetwork> network() {
            return Optional.empty();
        }

        @Override
        public void assignNetwork(INetwork network) {}

        @Override
        public void onConnectToNetwork(INetwork network) {}

        @Override
        public void onDisconnectFromNetwork() {}

        @Override
        public void buildSchedulings(ISchedulingRegister builder) {}
    }
}
