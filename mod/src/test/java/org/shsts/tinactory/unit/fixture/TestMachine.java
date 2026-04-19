package org.shsts.tinactory.unit.fixture;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.scores.PlayerTeam;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.gui.client.IRenderable;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public final class TestMachine implements IMachine {
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private final TestMachineConfig config = new TestMachineConfig();
    private final Random random = new Random(31L);
    private Optional<IContainer> container;
    private Optional<IElectricMachine> electric = Optional.empty();
    private Optional<TestTeamProfile> owner = Optional.empty();
    private boolean multiblock = false;
    private int parallel = 1;

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

    public TestMachine targetRecipe(ResourceLocation loc) {
        config.stringValue("targetRecipe", loc.toString());
        return this;
    }

    public TestTeamProfile team() {
        var team = new TestTeamProfile();
        owner = Optional.of(team);
        return team;
    }

    public TestMachine multiblock(boolean value) {
        multiblock = value;
        return this;
    }

    @Override
    public boolean isMultiblock() {
        return multiblock;
    }

    public TestMachine parallel(int value) {
        parallel = value;
        return this;
    }

    @Override
    public UUID uuid() {
        return id;
    }

    @Override
    public Optional<ITeamProfile> owner() {
        return owner.map(team -> team);
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
    public int parallel() {
        return parallel;
    }

    @Override
    public Random random() {
        return random;
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
        private final Map<String, String> strings = new java.util.HashMap<>();

        private void booleanValue(String key, boolean value) {
            booleans.put(key, value);
        }

        private void stringValue(String key, String value) {
            strings.put(key, value);
        }

        @Override
        public void apply(ISetMachineConfigPacket packet) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(String key, int tagType) {
            return booleans.containsKey(key) || strings.containsKey(key);
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
            return Optional.ofNullable(strings.get(key));
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

    public static final class TestTeamProfile implements IServerTeamProfile {
        private final Map<ResourceLocation, Long> progress = new HashMap<>();
        private final Set<ResourceLocation> available = new HashSet<>();
        private final Set<ResourceLocation> finished = new HashSet<>();
        private Optional<TestTechnology> target = Optional.empty();

        public TestTeamProfile available(ResourceLocation tech) {
            available.add(tech);
            return this;
        }

        public TestTeamProfile finished(ResourceLocation tech) {
            finished.add(tech);
            return this;
        }

        public TestTeamProfile progress(ResourceLocation tech, long value) {
            progress.put(tech, value);
            return this;
        }

        public TestTeamProfile target(ResourceLocation tech, long maxProgress) {
            target = Optional.of(new TestTechnology(tech, maxProgress));
            return available(tech);
        }

        @Override
        public PlayerTeam getPlayerTeam() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getTechProgress(ResourceLocation tech) {
            return progress.getOrDefault(tech, 0L);
        }

        @Override
        public boolean isTechFinished(ResourceLocation tech) {
            return finished.contains(tech);
        }

        @Override
        public boolean isTechAvailable(ResourceLocation tech) {
            return available.contains(tech);
        }

        @Override
        public boolean canResearch(ResourceLocation tech, long value) {
            return target.map($ -> $.getLoc().equals(tech) &&
                isTechAvailable(tech) &&
                getTechProgress(tech) + value <= $.getMaxProgress())
                .orElse(false);
        }

        @Override
        public Optional<ITechnology> getTargetTech() {
            return target.map(tech -> tech);
        }

        @Override
        public int getModifier(String key) {
            return 0;
        }

        @Override
        public void advanceTechProgress(ITechnology tech, long value) {
            advanceTechProgress(tech.getLoc(), value);
        }

        @Override
        public void advanceTechProgress(ResourceLocation tech, long value) {
            progress.put(tech, getTechProgress(tech) + value);
        }

        @Override
        public void setTargetTech(ITechnology tech) {
            target = Optional.of(new TestTechnology(tech.getLoc(), tech.getMaxProgress()));
            available(tech.getLoc());
        }

        @Override
        public void resetTargetTech() {
            target = Optional.empty();
        }
    }

    private record TestTechnology(ResourceLocation loc, long maxProgress) implements ITechnology {
        @Override
        public ResourceLocation getLoc() {
            return loc;
        }

        @Override
        public List<ITechnology> getDepends() {
            return List.of();
        }

        @Override
        public Map<String, Integer> getModifiers() {
            return Map.of();
        }

        @Override
        public long getMaxProgress() {
            return maxProgress;
        }

        @Override
        public DistLazy<? extends IRenderable> getDisplay() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int compareTo(ITechnology other) {
            return loc.compareTo(other.getLoc());
        }
    }
}
