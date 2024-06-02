package org.shsts.tinactory.content.machine;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.gui.sync.SetMachinePacket;
import org.shsts.tinactory.content.logistics.LogisticsComponent;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.common.ReturnEvent;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.UpdatableCapabilityProvider;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.network.Component;
import org.shsts.tinactory.core.network.Network;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.tech.TeamProfile;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Machine extends UpdatableCapabilityProvider
        implements IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final Logger LOGGER = LogUtils.getLogger();

    protected final SmartBlockEntity blockEntity;

    @Nullable
    protected Network network;

    public final MachineConfig config = new MachineConfig();

    protected Machine(SmartBlockEntity be) {
        this.blockEntity = be;
    }

    public void setConfig(SetMachinePacket packet) {
        config.apply(packet);
        if (packet.contains("targetRecipe")) {
            updateTargetRecipe(true);
            sendUpdate(blockEntity);
        }
        EventManager.invoke(blockEntity, AllEvents.SET_MACHINE_CONFIG, packet);
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, this::onServerLoad);
        eventManager.subscribe(AllEvents.REMOVED_IN_WORLD, this::onRemoved);
        eventManager.subscribe(AllEvents.REMOVED_BY_CHUNK, this::onRemoved);
        eventManager.subscribe(AllEvents.SERVER_USE, this::onServerUse);
    }

    protected void onServerLoad(Level world) {
        LOGGER.debug("machine {}: loaded", this);
        updateTargetRecipe(true);
    }

    protected void onRemoved(Level world) {
        if (network != null) {
            network.invalidate();
        }
        LOGGER.debug("machine {}: removed in world", this);
    }

    public boolean canPlayerInteract(Player player) {
        return network != null && network.team.hasPlayer(player);
    }

    protected void onServerUse(AllEvents.OnUseArg arg, ReturnEvent.Token<InteractionResult> token) {
        if (!canPlayerInteract(arg.player())) {
            token.setReturn(InteractionResult.FAIL);
        } else {
            token.setReturn(InteractionResult.PASS);
        }
    }

    protected void resetTargetRecipe(IContainer container, boolean updateFilter) {
        var portSize = container.portSize();
        for (var i = 0; i < portSize; i++) {
            if (!container.hasPort(i) || container.portDirection(i) != PortDirection.INPUT) {
                continue;
            }
            if (updateFilter) {
                container.resetFilter(i);
            }
            var port = container.getPort(i, false);
            getLogistics().ifPresent(component -> component.removePassiveStorage(PortDirection.INPUT, port));
        }
    }

    protected void setTargetRecipe(IContainer container, ProcessingRecipe recipe, boolean updateFilter) {
        for (var input : recipe.inputs) {
            var idx = input.port();
            var port = container.getPort(idx, false);
            var ingredient = input.ingredient();
            if (!container.hasPort(idx)) {
                continue;
            }
            if (updateFilter) {
                if (ingredient instanceof ProcessingIngredients.TagIngredient tag) {
                    container.setItemFilter(idx, tag.ingredient);
                } else if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
                    var stack1 = item.stack();
                    container.setItemFilter(idx, stack -> ItemHelper.canItemsStack(stack, stack1));
                } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
                    var stack1 = fluid.fluid();
                    container.setFluidFilter(idx, stack -> stack.isFluidEqual(stack1));
                }
            }
            getLogistics().ifPresent(component -> component.addPassiveStorage(PortDirection.INPUT, port));
        }
    }

    protected void updateTargetRecipe(boolean updateFilter) {
        var world = blockEntity.getLevel();
        assert world != null;
        var recipe = config.getRecipe("targetRecipe", world).orElse(null);
        LOGGER.debug("update target recipe = {}", recipe);
        var container = getContainer().orElse(null);
        if (container == null) {
            return;
        }
        if (recipe == null) {
            resetTargetRecipe(container, updateFilter);
        } else {
            setTargetRecipe(container, recipe, updateFilter);
        }
    }

    protected void dumpItemOutput(IContainer container, LogisticsComponent logistics) {
        var size = container.portSize();
        for (var i = 0; i < size; i++) {
            if (!container.hasPort(i) || container.portDirection(i) != PortDirection.OUTPUT) {
                continue;
            }
            var port = container.getPort(i, false);
            if (port.type() != PortType.ITEM) {
                continue;
            }
            for (var stack : port.asItem().getAllItems()) {
                logistics.addActiveRequest(PortDirection.OUTPUT, port.asItem(), stack);
            }
        }
    }

    protected void dumpFluidOutput(IContainer container, LogisticsComponent logistics) {
        var size = container.portSize();
        for (var i = 0; i < size; i++) {
            if (!container.hasPort(i) || container.portDirection(i) != PortDirection.OUTPUT) {
                continue;
            }
            var port = container.getPort(i, false);
            if (port.type() != PortType.FLUID) {
                continue;
            }
            for (var stack : port.asFluid().getAllFluids()) {
                logistics.addActiveRequest(PortDirection.OUTPUT, port.asFluid(), stack);
            }
        }
    }

    /**
     * Called when connect to the network
     */
    public void onConnectToNetwork(Network network) {
        LOGGER.debug("machine {}: connect to network {}", this, network);
        this.network = network;
        updateTargetRecipe(false);
        EventManager.invoke(blockEntity, AllEvents.CONNECT, network);
    }

    protected void onPreWork(Level world, Network network) {
        assert this.network == network;
        getProcessor().ifPresent(IProcessor::onPreWork);
        var logistics = network.getComponent(AllNetworks.LOGISTICS_COMPONENT);
        if (config.getBoolean("autoDumpItem", false)) {
            getContainer().ifPresent(container -> dumpItemOutput(container, logistics));
        }
        if (config.getBoolean("autoDumpFluid", false)) {
            getContainer().ifPresent(container -> dumpFluidOutput(container, logistics));
        }
    }

    protected void onWork(Level world, Network network) {
        assert this.network == network;
        var workFactor = network.getComponent(AllNetworks.ELECTRIC_COMPONENT).getWorkFactor();
        getProcessor().ifPresent(processor -> processor.onWorkTick(workFactor));
    }

    public void buildSchedulings(Component.SchedulingBuilder builder) {
        builder.add(AllNetworks.PRE_WORK_SCHEDULING, this::onPreWork);
        builder.add(AllNetworks.WORK_SCHEDULING, this::onWork);
        EventManager.invoke(blockEntity, AllEvents.BUILD_SCHEDULING, builder);
    }

    public Optional<Network> getNetwork() {
        return Optional.ofNullable(network);
    }

    public Optional<TeamProfile> getOwnerTeam() {
        if (network == null) {
            return Optional.empty();
        } else {
            return Optional.of(network.team);
        }
    }

    public Optional<IProcessor> getProcessor() {
        return AllCapabilities.PROCESSOR.tryGet(blockEntity);
    }

    public Optional<IContainer> getContainer() {
        return AllCapabilities.CONTAINER.tryGet(blockEntity);
    }

    public Optional<IElectricMachine> getElectric() {
        return AllCapabilities.ELECTRIC_MACHINE.tryGet(blockEntity);
    }

    public Optional<LogisticsComponent> getLogistics() {
        return getNetwork().map(network -> network.getComponent(AllNetworks.LOGISTICS_COMPONENT));
    }

    public Optional<ProcessingRecipe> getTargetRecipe() {
        var world = blockEntity.getLevel();
        assert world != null;
        return config.getRecipe("targetRecipe", world);
    }

    /**
     * Called when disconnect from the network
     */
    public void onDisconnectFromNetwork() {
        network = null;
        LOGGER.debug("machine {}: disconnect from network", this);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.MACHINE.get()) {
            return myself();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("config", config.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        config.deserializeNBT(tag.getCompound("config"));
    }

    @Override
    public CompoundTag serializeOnUpdate() {
        var tag = new CompoundTag();
        config.getLoc("targetRecipe").ifPresent(loc -> tag.putString("targetRecipe", loc.toString()));
        return tag;
    }

    @Override
    public void deserializeOnUpdate(CompoundTag tag) {
        if (tag.contains("targetRecipe", Tag.TAG_STRING)) {
            config.setString("targetRecipe", tag.getString("targetRecipe"));
        } else {
            config.reset("targetRecipe");
        }
        updateTargetRecipe(true);
    }

    public static Machine get(BlockEntity be) {
        return AllCapabilities.MACHINE.get(be);
    }

    public static Optional<Machine> tryGet(BlockEntity be) {
        return AllCapabilities.MACHINE.tryGet(be);
    }

    public static <P> CapabilityProviderBuilder<SmartBlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "network/machine", Machine::new);
    }
}
