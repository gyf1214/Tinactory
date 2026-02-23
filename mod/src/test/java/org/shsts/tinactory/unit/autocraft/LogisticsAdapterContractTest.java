package org.shsts.tinactory.unit.autocraft;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent.SchedulingBuilder;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.logistics.LogisticComponent.PortInfo;
import org.shsts.tinactory.core.autocraft.integration.LogisticsMachineAllocator;
import org.shsts.tinactory.core.autocraft.integration.LogisticsPatternRepository;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.autocraft.model.CraftPattern;
import org.shsts.tinactory.core.autocraft.model.MachineRequirement;

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
    void allocatorShouldUseRecipeTypeCapabilityProbe() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "smelting"), 0, List.of());
        var machine = new FakeMachine();
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new PortInfo(machine, 0, IPort.EMPTY, BlockPos.ZERO, 0)),
            $ -> 0,
            ($, recipeTypeId) -> recipeTypeId.equals(requirement.recipeTypeId()));

        assertTrue(allocator.canRun(requirement));
    }

    @Test
    void allocatorShouldFailWhenCapabilityProbeRejectsRecipeType() {
        var requirement = new MachineRequirement(new ResourceLocation("tinactory", "smelting"), 0, List.of());
        var machine = new FakeMachine();
        var allocator = new LogisticsMachineAllocator(
            () -> List.of(new PortInfo(machine, 0, IPort.EMPTY, BlockPos.ZERO, 0)),
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
        public UUID uuid() {
            return UUID.fromString("11111111-1111-1111-1111-111111111111");
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
        public void buildSchedulings(SchedulingBuilder builder) {}
    }
}
