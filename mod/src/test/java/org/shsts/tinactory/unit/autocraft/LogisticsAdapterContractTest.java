package org.shsts.tinactory.unit.autocraft;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.autocraft.integration.LogisticsMachineAllocator;
import org.shsts.tinactory.core.autocraft.integration.LogisticsPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

import java.util.List;
import java.util.Optional;

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
    void allocatorShouldUseRecipeTypeCapabilityProbe() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "smelting"), 0, List.of());
        var machine = new FakeMachine();
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new org.shsts.tinactory.content.logistics.LogisticComponent.PortInfo(
                machine, 0, org.shsts.tinactory.api.logistics.IPort.EMPTY, BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> recipeTypeId.equals(requirement.recipeTypeId()));

        assertTrue(allocator.canRun(requirement));
    }

    @Test
    void allocatorShouldFailWhenCapabilityProbeRejectsRecipeType() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "smelting"), 0, List.of());
        var machine = new FakeMachine();
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new org.shsts.tinactory.content.logistics.LogisticComponent.PortInfo(
                machine, 0, org.shsts.tinactory.api.logistics.IPort.EMPTY, BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> false);

        assertFalse(allocator.canRun(requirement));
    }

    private static CraftPattern pattern(String id, CraftKey key) {
        return new CraftPattern(
            id,
            List.of(new CraftAmount(CraftKey.item("minecraft:cobblestone", ""), 1)),
            List.of(new CraftAmount(key, 1)),
            new MachineRequirement(new ResourceLocation("tinactory", "mixer"), 0, List.of()));
    }

    private static final class FakeMachine implements IMachine {
        @Override
        public java.util.UUID uuid() {
            return java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
        }

        @Override
        public Optional<org.shsts.tinactory.api.tech.ITeamProfile> owner() {
            return Optional.empty();
        }

        @Override
        public boolean canPlayerInteract(net.minecraft.world.entity.player.Player player) {
            return true;
        }

        @Override
        public org.shsts.tinactory.api.machine.IMachineConfig config() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setConfig(org.shsts.tinactory.api.machine.ISetMachineConfigPacket packet, boolean invokeUpdate) {
            throw new UnsupportedOperationException();
        }

        @Override
        public net.minecraft.network.chat.Component title() {
            throw new UnsupportedOperationException();
        }

        @Override
        public net.minecraft.world.item.ItemStack icon() {
            throw new UnsupportedOperationException();
        }

        @Override
        public net.minecraft.world.level.block.entity.BlockEntity blockEntity() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<net.minecraft.world.level.block.state.BlockState> workBlock() {
            return Optional.empty();
        }

        @Override
        public Optional<org.shsts.tinactory.api.machine.IProcessor> processor() {
            return Optional.empty();
        }

        @Override
        public Optional<org.shsts.tinactory.api.logistics.IContainer> container() {
            return Optional.empty();
        }

        @Override
        public Optional<org.shsts.tinactory.api.electric.IElectricMachine> electric() {
            return Optional.empty();
        }

        @Override
        public Optional<org.shsts.tinactory.api.network.INetwork> network() {
            return Optional.empty();
        }

        @Override
        public void assignNetwork(org.shsts.tinactory.api.network.INetwork network) {}

        @Override
        public void onConnectToNetwork(org.shsts.tinactory.api.network.INetwork network) {}

        @Override
        public void onDisconnectFromNetwork() {}

        @Override
        public void buildSchedulings(org.shsts.tinactory.api.network.INetworkComponent.SchedulingBuilder builder) {}
    }
}
