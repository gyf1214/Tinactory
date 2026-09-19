package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.AllNetworks;
import org.shsts.tinactory.AllRegistries;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.gui.IRenderDescriptor;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.machine.ISetMachineConfigPacket;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.tech.IServerTeamProfile;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.gui.EmptyRenderDescriptor;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.machine.MachineConfig;
import org.shsts.tinactory.core.machine.MachineConfigType;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.core.ILoc;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

public final class TestMachine implements IMachine {
    private final UUID id = UUID.fromString("00000000-0000-0000-0000-000000000031");
    private final IMachineConfig config = new MachineConfig();
    private final RandomSource random = RandomSource.create(31L);
    private Optional<IContainer> container;
    private Optional<IElectricMachine> electric = Optional.empty();
    private Optional<TestTeamProfile> owner = Optional.empty();
    private Optional<TestProcessor> processor = Optional.empty();
    private boolean multiblock = false;
    private int parallel = 1;

    public TestMachine(IContainer container) {
        this.container = Optional.ofNullable(container);
    }

    public TestMachine withoutContainer() {
        this.container = Optional.empty();
        return this;
    }

    private <T> void setConfig(IEntry<IMachineConfigType<T>> type, T val) {
        config.apply(SetMachineConfigPacket.builder().set(type, val).get());
    }

    public TestMachine autoVoid(boolean value) {
        setConfig(AUTO_VOID, value);
        return this;
    }

    public TestMachine electricVoltage(long value) {
        electric = Optional.of(new TestElectricMachine(value));
        return this;
    }

    public TestMachine targetRecipe(ResourceLocation loc) {
        setConfig(TARGET_RECIPE, loc);
        return this;
    }

    public TestMachine supportsRecipeType(ResourceLocation loc) {
        processor = Optional.of(new TestProcessor(Set.of(loc)));
        return this;
    }

    public Optional<String> targetRecipe() {
        return config.getString("targetRecipe");
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
        config.apply(packet);
    }

    @Override
    public Component title() {
        return I18n.raw("test-machine");
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
        return processor.map(value -> value);
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
    public RandomSource random() {
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
    public void buildSchedulings(ISchedulingRegister builder) {}

    private record TestProcessor(Set<ResourceLocation> recipeTypes) implements IMachineProcessor {
        @Override
        public Optional<IProcessingObject> getInfo(int port, int index) {
            return Optional.empty();
        }

        @Override
        public List<IProcessingObject> getAllInfo() {
            return List.of();
        }

        @Override
        public long progressTicks() {
            return 0;
        }

        @Override
        public long maxProgressTicks() {
            return 0;
        }

        @Override
        public double workSpeed() {
            return 0;
        }

        @Override
        public boolean supportsRecipeType(ResourceLocation recipeTypeId) {
            return recipeTypes.contains(recipeTypeId);
        }

        @Override
        public boolean allowTargetRecipe(ResourceLocation loc) {
            return true;
        }

        @Override
        public void onPreWork() {
        }

        @Override
        public void onWorkTick(double partial) {
        }

        @Override
        public boolean isWorking(double partial) {
            return false;
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
        public String getName() {
            return "test-team";
        }

        @Override
        public Component getDisplayName() {
            return I18n.raw("test-team");
        }

        @Override
        public long getTechProgress(ResourceLocation tech) {
            return progress.getOrDefault(tech, 0L);
        }

        @Override
        public long getTechProgress(ITechnology tech) {
            return getTechProgress(techKey(tech));
        }

        @Override
        public boolean isTechFinished(ResourceLocation tech) {
            return finished.contains(tech);
        }

        @Override
        public boolean isTechFinished(ITechnology tech) {
            return isTechFinished(techKey(tech));
        }

        @Override
        public boolean isTechAvailable(ResourceLocation tech) {
            return available.contains(tech);
        }

        @Override
        public boolean isTechAvailable(ITechnology tech) {
            return isTechAvailable(techKey(tech));
        }

        @Override
        public boolean canResearch(ResourceLocation tech, long value) {
            return target.map($ -> $.loc().equals(tech) &&
                    isTechAvailable(tech) &&
                    getTechProgress(tech) + value <= $.getMaxProgress())
                .orElse(false);
        }

        @Override
        public boolean canResearch(ITechnology tech) {
            return canResearch(techKey(tech));
        }

        @Override
        public boolean canResearch(ITechnology tech, long value) {
            return canResearch(techKey(tech), value);
        }

        @Override
        public Optional<ITechnology> getTargetTech() {
            return target.map(tech -> tech);
        }

        @Override
        public Optional<ResourceLocation> getTargetTechKey() {
            return target.map(TestTechnology::loc);
        }

        @Override
        public int getModifier(String key) {
            return 0;
        }

        @Override
        public void advanceTechProgress(ITechnology tech, long value) {
            advanceTechProgress(techKey(tech), value);
        }

        @Override
        public void advanceTechProgress(ResourceLocation tech, long value) {
            progress.put(tech, getTechProgress(tech) + value);
        }

        @Override
        public void setTargetTech(ITechnology tech) {
            var loc = techKey(tech);
            target = Optional.of(new TestTechnology(loc, tech.getMaxProgress()));
            available(loc);
        }

        @Override
        public void resetTargetTech() {
            target = Optional.empty();
        }

        private static ResourceLocation techKey(ITechnology tech) {
            if (tech instanceof TestTechnology testTechnology) {
                return testTechnology.loc();
            }
            throw new IllegalArgumentException("Unknown test technology " + tech);
        }
    }

    private record TestTechnology(ResourceLocation loc, long maxProgress) implements ITechnology {
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
        public IRenderDescriptor getDisplay() {
            return EmptyRenderDescriptor.INSTANCE;
        }
    }

    public static final MappedRegistry<IMachineConfigType<?>> MACHINE_CONFIGS = new MappedRegistry<>(
        AllRegistries.MACHINE_CONFIGS.key(), Lifecycle.stable());

    public static final IEntry<IMachineConfigType<Boolean>> AUTO_VOID =
        machineConfig(AllNetworks.AUTO_VOID, Codec.BOOL, "void");
    public static final IEntry<IMachineConfigType<ResourceLocation>> TARGET_RECIPE =
        machineConfig(AllNetworks.TARGET_RECIPE, ResourceLocation.CODEC, "targetRecipe");
    public static final IEntry<IMachineConfigType<Integer>> MACHINE_LIMIT = machineConfig("limit", Codec.INT);
    public static final IEntry<IMachineConfigType<String>> MACHINE_NAME = machineConfig("name", Codec.STRING);

    private static <T> IEntry<IMachineConfigType<T>> machineConfig(ILoc loc, Codec<T> codec, String legacyKey) {
        var val = Registry.register(MACHINE_CONFIGS, loc.loc(), new MachineConfigType<>(codec, legacyKey));
        return TestCodecHelper.createEntry(loc.loc(), val);
    }

    private static <T> IEntry<IMachineConfigType<T>> machineConfig(String id, Codec<T> codec) {
        var loc = modLoc(id);
        var val = Registry.register(MACHINE_CONFIGS, loc, new MachineConfigType<>(codec, null));
        return TestCodecHelper.createEntry(loc, val);
    }
}
