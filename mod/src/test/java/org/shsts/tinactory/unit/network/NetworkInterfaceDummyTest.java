package org.shsts.tinactory.unit.network;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistryEntry;
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
import org.shsts.tinactory.api.network.IScheduling;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NetworkInterfaceDummyTest {
    @Test
    void shouldCreateInterfaceDummiesWithoutBootstrap() {
        var network = new DummyNetwork();
        var component = new DummyNetworkComponent();
        var componentType = new DummyComponentType(component);
        var scheduling = new DummyScheduling();
        var machine = new DummyMachine();

        var builtSchedulings = new ArrayList<IScheduling>();
        var tickerCalls = new int[]{0};
        component.buildSchedulings((addedScheduling, ticker) -> {
            builtSchedulings.add(addedScheduling);
            ticker.tick(null, network);
            tickerCalls[0]++;
        });
        machine.buildSchedulings((addedScheduling, ticker) -> {
            builtSchedulings.add(addedScheduling);
            ticker.tick(null, network);
            tickerCalls[0]++;
        });

        network.putComponent(componentType, component);
        assertSame(component, network.getComponent(componentType));
        assertNotNull(machine.uuid());
        assertFalse(machine.network().isPresent());
        machine.assignNetwork(network);
        assertTrue(machine.network().isPresent());
        assertSame(network, machine.network().orElseThrow());
        assertEquals(2, builtSchedulings.size());
        assertEquals(2, tickerCalls[0]);
        assertEquals(0, scheduling.conditions().size());
        scheduling.addConditions((left, right) -> scheduling.conditions().add(left + "->" + right));
        assertNotNull(componentType.clazz());
        assertSame(component, componentType.create(network));
    }

    private static final class DummyMachine implements IMachine {
        private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000123");
        private Optional<INetwork> network = Optional.empty();

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
        public void setConfig(ISetMachineConfigPacket packet, boolean invokeUpdate) {}

        @Override
        public Component title() {
            return new TextComponent("dummy");
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
            return network;
        }

        @Override
        public void assignNetwork(INetwork network) {
            this.network = Optional.of(network);
        }

        @Override
        public void onConnectToNetwork(INetwork network) {}

        @Override
        public void onDisconnectFromNetwork() {}

        @Override
        public void buildSchedulings(ISchedulingRegister builder) {
            builder.add(new DummyScheduling(), (world, network) -> {
            });
        }
    }

    private static final class DummyNetwork implements INetwork {
        private final Map<IComponentType<?>, INetworkComponent> components = new java.util.HashMap<>();
        private final Multimap<BlockPos, IMachine> machines = ArrayListMultimap.create();
        private final Map<BlockPos, BlockPos> blocks = new java.util.HashMap<>();

        private <T extends INetworkComponent> void putComponent(IComponentType<T> type, T component) {
            components.put(type, component);
        }

        @Override
        public ITeamProfile owner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T extends INetworkComponent> T getComponent(IComponentType<T> type) {
            return type.clazz().cast(components.get(type));
        }

        @Override
        public BlockPos getSubnet(BlockPos pos) {
            return blocks.getOrDefault(pos, BlockPos.ZERO);
        }

        @Override
        public Multimap<BlockPos, IMachine> allMachines() {
            return machines;
        }

        @Override
        public Collection<Map.Entry<BlockPos, BlockPos>> allBlocks() {
            return blocks.entrySet();
        }
    }

    private static final class DummyNetworkComponent implements INetworkComponent {
        @Override
        public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {}

        @Override
        public void onConnect() {}

        @Override
        public void onPostConnect() {}

        @Override
        public void onDisconnect() {}

        @Override
        public void buildSchedulings(ISchedulingRegister builder) {
            builder.add(new DummyScheduling(), (world, network) -> {
            });
        }
    }

    private static final class DummyComponentType extends ForgeRegistryEntry<IComponentType<?>>
        implements IComponentType<DummyNetworkComponent> {
        private final DummyNetworkComponent component;

        private DummyComponentType(DummyNetworkComponent component) {
            this.component = component;
        }

        @Override
        public Class<DummyNetworkComponent> clazz() {
            return DummyNetworkComponent.class;
        }

        @Override
        public DummyNetworkComponent create(INetwork network) {
            return component;
        }
    }

    private static final class DummyScheduling extends ForgeRegistryEntry<IScheduling>
        implements IScheduling {
        private final List<String> conditions = new ArrayList<>();

        @Override
        public void addConditions(java.util.function.BiConsumer<IScheduling, IScheduling> cons) {}

        private List<String> conditions() {
            return conditions;
        }
    }
}
