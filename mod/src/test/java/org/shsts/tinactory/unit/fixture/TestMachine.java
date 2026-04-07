package org.shsts.tinactory.unit.fixture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.ITeamProfile;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TestMachine implements IMachine {
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private final TestMachineConfig config = new TestMachineConfig();
    private Optional<IContainer> container;
    private Optional<IElectricMachine> electric = Optional.empty();

    public TestMachine(IContainer container) {
        this.container = Optional.of(container);
    }

    public TestMachine withoutContainer() {
        this.container = Optional.empty();
        return this;
    }

    public TestMachine autoVoid(boolean value) {
        config.booleanValue("void", value);
        return this;
    }

    public TestMachine electricVoltage(long value) {
        electric = Optional.of(new TestElectricMachine(value));
        return this;
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
        return config;
    }

    @Override
    public void setConfig(ISetMachineConfigPacket packet, boolean invokeUpdate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Component title() {
        return new TextComponent("test-machine");
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
        return container;
    }

    @Override
    public Optional<IElectricMachine> electric() {
        return electric;
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
    }

    private static final class TestMachineConfig implements IMachineConfig {
        private final Map<String, Boolean> booleans = new java.util.HashMap<>();

        private void booleanValue(String key, boolean value) {
            booleans.put(key, value);
        }

        @Override
        public void apply(ISetMachineConfigPacket packet) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(String key, int tagType) {
            return false;
        }

        @Override
        public Optional<Boolean> getBoolean(String key) {
            return Optional.ofNullable(booleans.get(key));
        }

        @Override
        public Optional<Integer> getInt(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<String> getString(String key) {
            return Optional.empty();
        }

        @Override
        public Optional<CompoundTag> getCompound(String key) {
            return Optional.empty();
        }

        @Override
        public CompoundTag serializeNBT() {
            return new CompoundTag();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
        }
    }

    private record TestElectricMachine(long voltage) implements IElectricMachine {
        @Override
        public long getVoltage() {
            return voltage;
        }

        @Override
        public ElectricMachineType getMachineType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double getPowerGen() {
            return 0;
        }

        @Override
        public double getPowerCons() {
            return 0;
        }
    }
}
