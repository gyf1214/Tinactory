package org.shsts.tinactory.integration.machine;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.AllCapabilities;
import org.shsts.tinactory.api.electric.ElectricMachineType;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.Machine;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllEvents.BUILD_SCHEDULING;
import static org.shsts.tinactory.AllEvents.CONNECT;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.AllEvents.REMOVED_BY_CHUNK;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.AllEvents.SERVER_LOAD;
import static org.shsts.tinactory.AllEvents.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.PRE_SIGNAL_SCHEDULING;
import static org.shsts.tinactory.integration.network.MachineBlock.getBlockVoltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineProcessor extends CapabilityProvider implements
    IProcessor, IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String VOID_KEY = "void";
    public static final boolean VOID_DEFAULT = false;

    protected final BlockEntity blockEntity;
    protected final ProcessingRuntime runtime;
    private final Consumer<ITeamProfile> onTechChange = this::onTechChange;

    public MachineProcessor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe) {
        this.blockEntity = blockEntity;
        this.runtime = new ProcessingRuntime(
            processorFactories.stream().map(factory -> factory.apply(blockEntity))
                .collect(Collectors.toUnmodifiableList()),
            autoRecipe,
            this::machine,
            world().isClientSide,
            blockEntity::setChanged,
            ProcessingHelper.INFO_CODEC);
    }

    private Level world() {
        var world = blockEntity.getLevel();
        assert world != null;
        return world;
    }

    protected Optional<IMachine> machine() {
        return MACHINE.tryGet(blockEntity);
    }

    private void onTechChange(ITeamProfile team) {
        if (team == machine().flatMap(IMachine::owner).orElse(null)) {
            runtime.onContainerChange();
        }
    }

    @Override
    public void onPreWork() {
        runtime.onPreWork();
    }

    @Override
    public void onWorkTick(double partial) {
        runtime.onWorkTick(partial);
    }

    @Override
    public double getProgress() {
        return runtime.getProgress();
    }

    @Override
    public boolean isWorking(double partial) {
        return runtime.isWorking(partial);
    }

    @Override
    public long getVoltage() {
        return machine().map($ -> getBlockVoltage($.blockEntity()).value).orElse(0L);
    }

    @Override
    public ElectricMachineType getMachineType() {
        return runtime.machineType();
    }

    @Override
    public double getPowerGen() {
        return runtime.powerGen();
    }

    @Override
    public double getPowerCons() {
        return runtime.powerCons();
    }

    private void onServerLoad(Level world) {
        TechManager.server().onProgressChange(onTechChange);
    }

    private void onRemoved(Level world) {
        if (!world.isClientSide) {
            TechManager.server().removeProgressChangeListener(onTechChange);
        }
    }

    private void onConnect(INetwork network) {
        machine().ifPresent(machine -> Machine.registerStopSignal(network, machine, runtime::setStopped));
        runtime.onConnect();
    }

    private void onMachineConfig() {
        runtime.onMachineConfig();
    }

    private void buildScheduling(ISchedulingRegister builder) {
        builder.add(PRE_SIGNAL_SCHEDULING.get(), (world, network) -> runtime.setStopped(false));
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(SERVER_LOAD.get(), this::onServerLoad);
        eventManager.subscribe(REMOVED_BY_CHUNK.get(), this::onRemoved);
        eventManager.subscribe(REMOVED_IN_WORLD.get(), this::onRemoved);
        eventManager.subscribe(CONTAINER_CHANGE.get(), runtime::onContainerChange);
        eventManager.subscribe(CONNECT.get(), this::onConnect);
        eventManager.subscribe(BUILD_SCHEDULING.get(), this::buildScheduling);
        eventManager.subscribe(SET_MACHINE_CONFIG.get(), this::onMachineConfig);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.PROCESSOR.get()) {
            return LazyOptional.of(() -> runtime).cast();
        }
        if (cap == AllCapabilities.ELECTRIC_MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return runtime.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        runtime.deserializeNBT(nbt);
    }
}
