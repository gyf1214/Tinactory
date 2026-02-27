package org.shsts.tinactory.unit.autocraft;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.Test;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.IComponentType;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.INetworkComponent;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.logistics.LogisticComponent;
import org.shsts.tinactory.content.material.MiscMeta;
import org.shsts.tinactory.core.autocraft.integration.AutocraftJobService;
import org.shsts.tinactory.core.autocraft.integration.AutocraftSubmitErrorCode;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinactory.core.autocraft.model.CraftKey;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutocraftCpuLifecycleTest {
    @Test
    void cpuShouldRegisterAndUnregisterWithLifecycle() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var subnet = new BlockPos(0, 0, 0);
        var cpu = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var service = new TestService(cpu.uuid());

        component.registerAutocraftCpu(cpu, subnet, service);

        assertTrue(component.isAutocraftCpuRegistered(cpu.uuid()));
        assertEquals(List.of(cpu.uuid()), component.listVisibleAutocraftCpus(subnet));

        component.unregisterAutocraftCpu(cpu.uuid());
        assertFalse(component.isAutocraftCpuRegistered(cpu.uuid()));
        assertEquals(List.of(), component.listVisibleAutocraftCpus(subnet));
    }

    @Test
    void multipleCpusShouldCoexist() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var subnet = new BlockPos(0, 0, 0);
        var cpuA = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var cpuB = new FakeMachine("22222222-2222-2222-2222-222222222222");

        component.registerAutocraftCpu(cpuA, subnet, new TestService(cpuA.uuid()));
        component.registerAutocraftCpu(cpuB, subnet, new TestService(cpuB.uuid()));

        assertEquals(List.of(cpuA.uuid(), cpuB.uuid()), component.listVisibleAutocraftCpus(subnet));
    }

    @Test
    void submitShouldRejectBusyCpu() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var subnet = new BlockPos(0, 0, 0);
        var cpu = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var service = new TestService(cpu.uuid());
        component.registerAutocraftCpu(cpu, subnet, service);
        service.busy = true;

        var result = component.submitAutocraft(subnet, cpu.uuid(),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)));

        assertEquals(AutocraftSubmitErrorCode.CPU_BUSY, result.errorCode());
        assertFalse(result.optionalJobId().isPresent());
    }

    @Test
    void submitShouldUseCpuMachineId() {
        var component = new LogisticComponent(null, new FakeNetwork());
        var subnet = new BlockPos(0, 0, 0);
        var cpu = new FakeMachine("11111111-1111-1111-1111-111111111111");
        var service = new TestService(cpu.uuid());
        component.registerAutocraftCpu(cpu, subnet, service);

        var result = component.submitAutocraft(subnet, cpu.uuid(),
            List.of(new CraftAmount(CraftKey.item("minecraft:iron_ingot", ""), 1)));

        assertTrue(result.isSuccess());
        assertNotNull(result.optionalJobId().orElse(null));
        assertEquals(cpu.uuid(), service.cpuId());
    }

    @Test
    void miscMetaShouldRequireCpuRuntimeFields() {
        var jo = new JsonObject();
        jo.addProperty("power", 32d);

        assertThrows(IllegalArgumentException.class, () -> MiscMeta.parseAutocraftCpuConfig(jo));
    }

    @Test
    void miscMetaShouldParseCpuRuntimeFields() {
        var jo = new JsonObject();
        jo.addProperty("power", 32d);
        jo.addProperty("transmissionBandwidth", 128L);
        jo.addProperty("executionIntervalTicks", 2);

        var config = MiscMeta.parseAutocraftCpuConfig(jo);

        assertEquals(32d, config.power());
        assertEquals(128L, config.transmissionBandwidth());
        assertEquals(2, config.executionIntervalTicks());
    }

    private static final class TestService extends AutocraftJobService {
        private boolean busy;

        private TestService(UUID cpuId) {
            super(cpuId, (targets, available) -> {
                throw new UnsupportedOperationException();
            }, () -> null, List::of);
        }

        @Override
        public boolean isBusy() {
            return busy;
        }
    }

    private static final class FakeNetwork implements INetwork {
        @Override
        public ITeamProfile owner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends INetworkComponent> T getComponent(IComponentType<T> type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockPos getSubnet(BlockPos pos) {
            return BlockPos.ZERO;
        }

        @Override
        public Multimap<BlockPos, IMachine> allMachines() {
            return ArrayListMultimap.create();
        }

        @Override
        public Collection<Map.Entry<BlockPos, BlockPos>> allBlocks() {
            return List.of();
        }
    }

    private static final class FakeMachine implements IMachine {
        private final UUID id;

        private FakeMachine(String id) {
            this.id = UUID.fromString(id);
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
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IProcessor> processor() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IContainer> container() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<IElectricMachine> electric() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<INetwork> network() {
            return Optional.empty();
        }

        @Override
        public void assignNetwork(INetwork network) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onConnectToNetwork(INetwork network) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onDisconnectFromNetwork() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void buildSchedulings(ISchedulingRegister builder) {
            throw new UnsupportedOperationException();
        }
    }
}
