package org.shsts.tinactory.content.logistics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllEvents;
import org.shsts.tinactory.content.AllNetworks;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.common.IEventSubscriber;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackContainer implements ICapabilityProvider,
        IContainer, IEventSubscriber, INBTSerializable<CompoundTag> {
    private record PortInfo(int startSlot, int endSlot, SlotType type,
                            IPort port, IPort internalPort) {}

    private final BlockEntity blockEntity;
    private final IItemHandlerModifiable combinedItems;
    private final CombinedFluidTank combinedFluids;
    private final List<PortInfo> ports;
    private final Map<Integer, WrapperItemHandler> itemInputs = new HashMap<>();
    private final Multimap<Integer, WrapperFluidTank> fluidInputs = ArrayListMultimap.create();

    private StackContainer(BlockEntity blockEntity, List<Builder.PortInfo> portInfo) {
        this.blockEntity = blockEntity;
        this.ports = new ArrayList<>(portInfo.size());

        var items = new ArrayList<WrapperItemHandler>(portInfo.size());
        var fluids = new ArrayList<WrapperFluidTank>();
        var slotIdx = 0;
        var portIdx = 0;
        for (var port : portInfo) {
            var type = port.type;
            if (port.slots <= 0 || type == SlotType.NONE) {
                ports.add(new PortInfo(slotIdx, slotIdx, SlotType.NONE,
                        IPort.EMPTY, IPort.EMPTY));
                portIdx++;
                continue;
            }
            switch (type) {
                case ITEM_INPUT -> {
                    var view = new WrapperItemHandler(port.slots);
                    view.onUpdate(this::onInputUpdate);
                    items.add(view);

                    var collection = new ItemHandlerCollection(view);
                    itemInputs.put(portIdx, view);
                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots, type,
                            collection, collection));
                }
                case ITEM_OUTPUT -> {
                    var inner = new WrapperItemHandler(port.slots);
                    inner.onUpdate(this::onOutputUpdate);

                    var view = new WrapperItemHandler(inner);
                    view.allowInput = false;
                    items.add(view);

                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots, type,
                            new ItemHandlerCollection(view), new ItemHandlerCollection(inner)));
                }
                case FLUID_INPUT -> {
                    var views = new WrapperFluidTank[port.slots];
                    for (var i = 0; i < port.slots; i++) {
                        var view = new WrapperFluidTank(TinactoryConfig.INSTANCE.fluidSlotSize.get());
                        view.onUpdate(this::onInputUpdate);

                        views[i] = view;
                        fluidInputs.put(portIdx, view);
                        fluids.add(view);
                    }

                    var collection = new CombinedFluidTank(views);
                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots, type,
                            collection, collection));
                }
                case FLUID_OUTPUT -> {
                    var inners = new WrapperFluidTank[port.slots];
                    var views = new WrapperFluidTank[port.slots];

                    for (var i = 0; i < port.slots; i++) {
                        var inner = new WrapperFluidTank(TinactoryConfig.INSTANCE.fluidSlotSize.get());
                        inner.onUpdate(this::onOutputUpdate);
                        inners[i] = inner;

                        var view = new WrapperFluidTank(inner);
                        view.allowInput = false;
                        views[i] = view;
                        fluids.add(view);
                    }

                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots, type,
                            new CombinedFluidTank(views), new CombinedFluidTank(inners)));
                }
            }
            portIdx++;
            slotIdx += port.slots;
        }
        this.combinedItems = new CombinedInvWrapper(items.toArray(IItemHandlerModifiable[]::new));
        this.combinedFluids = new CombinedFluidTank(fluids.toArray(WrapperFluidTank[]::new));
    }

    private void onInputUpdate() {
        EventManager.invoke(blockEntity, AllEvents.CONTAINER_CHANGE, true);
        blockEntity.setChanged();
    }

    private void onOutputUpdate() {
        EventManager.invoke(blockEntity, AllEvents.CONTAINER_CHANGE, false);
        blockEntity.setChanged();
    }

    @Override
    public Optional<ITeamProfile> getOwnerTeam() {
        return Machine.get(blockEntity).getOwnerTeam().map($ -> $);
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < ports.size() &&
                ports.get(port).type != SlotType.NONE;
    }

    @Override
    public IPort getPort(int port, boolean internal) {
        if (!hasPort(port)) {
            return IPort.EMPTY;
        }
        var portInfo = ports.get(port);
        return internal ? portInfo.internalPort : portInfo.port;
    }

    private void updateTargetRecipe(boolean updateFilter) {
        var world = blockEntity.getLevel();
        assert world != null && !world.isClientSide;
        var machine = Machine.get(blockEntity);
        var targetRecipe = machine.config.getRecipe("targetRecipe", world).orElse(null);
        var logistics = machine.getNetwork()
                .map(network -> network.getComponent(AllNetworks.LOGISTICS_COMPONENT))
                .orElse(null);
        if (targetRecipe == null) {
            if (updateFilter) {
                for (var itemHandler : itemInputs.values()) {
                    itemHandler.resetFilter();
                }
                for (var tank : fluidInputs.values()) {
                    tank.resetFilter();
                }
            }
            if (logistics != null) {
                for (var portInfo : ports) {
                    if (portInfo.type != SlotType.NONE && portInfo.type.direction == PortDirection.INPUT) {
                        logistics.removePassiveStorage(PortDirection.INPUT, portInfo.port);
                    }
                }
            }
            return;
        }
        for (ProcessingRecipe.Input input : targetRecipe.inputs) {
            var port = input.port();
            var ingredient = input.ingredient();
            if (!hasPort(port)) {
                continue;
            }
            if (updateFilter) {
                if (ingredient instanceof ProcessingIngredients.ItemIngredient item) {
                    var handler = itemInputs.get(port);
                    if (handler != null) {
                        handler.filter = item.ingredient();
                    }
                } else if (ingredient instanceof ProcessingIngredients.SimpleItemIngredient item) {
                    var handler = itemInputs.get(port);
                    if (handler != null) {
                        var stack1 = item.stack();
                        handler.filter = stack -> ItemHelper.canItemsStack(stack, stack1);
                    }
                } else if (ingredient instanceof ProcessingIngredients.FluidIngredient fluid) {
                    for (var tank : fluidInputs.get(port)) {
                        var stack1 = fluid.fluid();
                        tank.filter = stack -> stack.isFluidEqual(stack1);
                    }
                }
            }
            if (logistics != null) {
                logistics.addPassiveStorage(PortDirection.INPUT, getPort(port, false));
            }
        }
    }

    private void dumpItemOutput(LogisticsComponent logistics) {
        for (var portInfo : ports) {
            if (portInfo.type == SlotType.ITEM_OUTPUT) {
                var itemPort = portInfo.port.asItem();
                for (var slot = portInfo.startSlot; slot < portInfo.endSlot; slot++) {
                    var item = combinedItems.getStackInSlot(slot);
                    if (!item.isEmpty()) {
                        logistics.addActiveRequest(PortDirection.OUTPUT, itemPort, item);
                    }
                }
            }
        }
    }

    private void dumpFluidOutput(LogisticsComponent logistics) {
        for (var portInfo : ports) {
            if (portInfo.type == SlotType.FLUID_OUTPUT) {
                var fluidPort = portInfo.port.asFluid();
                for (var slot = portInfo.startSlot; slot < portInfo.endSlot; slot++) {
                    var fluid = combinedFluids.getFluidInTank(slot);
                    if (!fluid.isEmpty()) {
                        logistics.addActiveRequest(PortDirection.OUTPUT, fluidPort, fluid);
                    }
                }
            }
        }
    }

    @Override
    public void subscribeEvents(EventManager eventManager) {
        eventManager.subscribe(AllEvents.SERVER_LOAD, $ -> updateTargetRecipe(true));
        eventManager.subscribe(AllEvents.CONNECT, $ -> updateTargetRecipe(false));
        eventManager.subscribe(AllEvents.DUMP_ITEM_OUTPUT, this::dumpItemOutput);
        eventManager.subscribe(AllEvents.DUMP_FLUID_OUTPUT, this::dumpFluidOutput);
        eventManager.subscribe(AllEvents.SET_MACHINE_CONFIG, $ -> updateTargetRecipe(true));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> combinedItems).cast();
        } else if (cap == AllCapabilities.FLUID_STACK_HANDLER.get()) {
            return LazyOptional.of(() -> combinedFluids).cast();
        } else if (cap == AllCapabilities.CONTAINER.get()) {
            return LazyOptional.of(() -> this).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("stack", ItemHelper.serializeItemHandler(combinedItems));
        tag.put("fluid", combinedFluids.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(combinedItems, tag.getCompound("stack"));
        combinedFluids.deserializeNBT(tag.getCompound("fluid"));
    }

    public static class Builder<P> extends CapabilityProviderBuilder<BlockEntity, P> {
        private record PortInfo(int slots, SlotType type) {}

        private final List<PortInfo> ports = new ArrayList<>();

        public Builder(P parent) {
            super(parent, "logistics/stack_container");
        }

        public Builder<P> layout(Layout layout) {
            ports.clear();
            var slots = layout.slots.stream().filter(s -> s.type() != SlotType.NONE).toList();
            if (slots.isEmpty()) {
                return this;
            }
            var portCount = 1 + slots.stream().mapToInt(Layout.SlotInfo::port).max().getAsInt();
            var newPorts = new ArrayList<>(Collections.nCopies(portCount, new PortInfo(0, SlotType.NONE)));
            for (var slot : slots) {
                var info = newPorts.get(slot.port());
                newPorts.set(slot.port(), new PortInfo(info.slots + 1, slot.type()));
            }
            ports.addAll(newPorts);
            return this;
        }

        @Override
        public Function<BlockEntity, ICapabilityProvider> createObject() {
            var ports = this.ports;
            return be -> new StackContainer(be, ports);
        }
    }

    public static <P> Builder<P> builder(P parent) {
        return new Builder<>(parent);
    }
}
