package org.shsts.tinactory.integration.machine;

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
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.api.network.INetwork;
import org.shsts.tinactory.api.network.ISchedulingRegister;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingRuntime;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.integration.metrics.MetricsManager;
import org.shsts.tinactory.integration.recipe.ProcessingHelper;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
    IElectricMachine, IEventSubscriber, INBTSerializable<CompoundTag> {
    protected final BlockEntity blockEntity;
    protected final ProcessingRuntime runtime;
    private final Consumer<ITeamProfile> onTechChange = this::onTechChange;
    private final LazyOptional<IProcessor> processorCap;

    public MachineProcessor(BlockEntity blockEntity,
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe) {
        this.blockEntity = blockEntity;
        var world = blockEntity.getLevel();
        assert world != null;
        this.runtime = new ProcessingRuntime(
            processorFactories.stream().map(factory -> factory.apply(blockEntity)).toList(),
            autoRecipe,
            this::machine,
            world.isClientSide,
            blockEntity::setChanged,
            this::reportProcessingObject,
            ProcessingHelper.INFO_CODEC);
        this.processorCap = LazyOptional.of(() -> runtime);
    }

    protected Optional<IMachine> machine() {
        return MACHINE.tryGet(blockEntity);
    }

    private void reportProcessingObject(PortDirection direction, IProcessingObject object) {
        var action = switch (direction) {
            case INPUT -> "consumed";
            case OUTPUT -> "produced";
            case NONE -> throw new IllegalArgumentException("unexpected processing direction: " + direction);
        };
        machine().ifPresent(machine -> MetricsManager.reportProcessingObject(action, machine, object));
    }

    private void onTechChange(ITeamProfile team) {
        if (team == machine().flatMap(IMachine::owner).orElse(null)) {
            runtime.onContainerChange();
        }
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
            return processorCap.cast();
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
    public void deserializeNBT(CompoundTag tag) {
        runtime.deserializeNBT(tag);
    }
}
