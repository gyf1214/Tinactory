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
import java.util.function.BiConsumer;

final class NetworkRuntimeFixtures {
    private NetworkRuntimeFixtures() {}

    static final class DummyNetwork implements INetwork {
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
            throw new UnsupportedOperationException();
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

    static final class SchedulingFixture extends ForgeRegistryEntry<IScheduling> implements IScheduling {
        private final String id;
        private final List<Dependency> dependencies = new ArrayList<>();

        SchedulingFixture(String id) {
            this.id = id;
        }

        void before(IScheduling right) {
            dependencies.add(new Dependency(this, right));
        }

        @Override
        public void addConditions(BiConsumer<IScheduling, IScheduling> cons) {
            for (var dependency : dependencies) {
                cons.accept(dependency.left(), dependency.right());
            }
        }

        @Override
        public String toString() {
            return id;
        }

        private record Dependency(IScheduling left, IScheduling right) {}
    }

    static final class ComponentTypeFixture extends ForgeRegistryEntry<IComponentType<?>>
        implements IComponentType<ComponentFixture> {
        private final ComponentFixture component;

        ComponentTypeFixture(ComponentFixture component) {
            this.component = component;
        }

        @Override
        public Class<ComponentFixture> clazz() {
            return ComponentFixture.class;
        }

        @Override
        public ComponentFixture create(INetwork network) {
            return component;
        }
    }

    static final class ComponentFixture implements INetworkComponent {
        private final List<String> events;
        private final IScheduling scheduling;

        ComponentFixture(List<String> events, IScheduling scheduling) {
            this.events = events;
            this.scheduling = scheduling;
        }

        @Override
        public void putBlock(BlockPos pos, BlockState state, BlockPos subnet) {
            events.add("component.putBlock:" + pos + "->" + subnet);
        }

        @Override
        public void onConnect() {
            events.add("component.onConnect");
        }

        @Override
        public void onPostConnect() {
            events.add("component.onPostConnect");
        }

        @Override
        public void onDisconnect() {
            events.add("component.onDisconnect");
        }

        @Override
        public void buildSchedulings(ISchedulingRegister builder) {
            builder.add(scheduling, (world, network) -> events.add("component.tick"));
        }
    }

    static class MachineFixture implements IMachine {
        private final UUID id;
        private final List<String> events;
        private final IScheduling scheduling;
        private Optional<INetwork> network = Optional.empty();

        MachineFixture(String id, List<String> events, IScheduling scheduling) {
            this.id = UUID.fromString(id);
            this.events = events;
            this.scheduling = scheduling;
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
        }

        @Override
        public Component title() {
            return new TextComponent("machine");
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
            events.add("machine.assignNetwork");
        }

        @Override
        public void onConnectToNetwork(INetwork network) {
            events.add("machine.onConnect");
        }

        @Override
        public void onDisconnectFromNetwork() {
            events.add("machine.onDisconnect");
        }

        @Override
        public void buildSchedulings(ISchedulingRegister builder) {
            builder.add(scheduling, (world, network) -> events.add("machine.tick"));
        }
    }

    static final class ThrowOnTouchMachine extends MachineFixture {
        ThrowOnTouchMachine(String id, List<String> events, IScheduling scheduling) {
            super(id, events, scheduling);
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
    }
}
