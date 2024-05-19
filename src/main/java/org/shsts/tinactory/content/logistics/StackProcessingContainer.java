package org.shsts.tinactory.content.logistics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
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
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.tech.TechManager;
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
import java.util.function.Predicate;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackProcessingContainer implements ICapabilityProvider, IContainer, INBTSerializable<CompoundTag> {
    private record PortInfo(int startSlot, int endSlot, SlotType type,
                            IPort port, IPort internalPort) {}

    private final BlockEntity blockEntity;
    private final IItemHandlerModifiable combinedItems;
    private final CombinedFluidTank combinedFluids;
    private final List<PortInfo> ports;
    private final Map<Integer, WrapperItemHandler> itemInputs = new HashMap<>();
    private final Multimap<Integer, WrapperFluidTank> fluidInputs = ArrayListMultimap.create();

    private StackProcessingContainer(BlockEntity blockEntity, List<Builder.PortInfo> portInfo) {
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
    public Optional<? extends ITeamProfile> getOwnerTeam() {
        var world = blockEntity.getLevel();
        assert world != null;
        if (world.isClientSide) {
            return TechManager.client().localPlayerTeam();
        } else {
            return Machine.get(blockEntity).getOwnerTeam();
        }
    }

    @Override
    public int portSize() {
        return ports.size();
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < ports.size() &&
                ports.get(port).type != SlotType.NONE;
    }

    @Override
    public PortDirection portDirection(int port) {
        return ports.get(port).type.direction;
    }

    @Override
    public IPort getPort(int port, boolean internal) {
        if (!hasPort(port)) {
            return IPort.EMPTY;
        }
        var portInfo = ports.get(port);
        return internal ? portInfo.internalPort : portInfo.port;
    }

    @Override
    public void setItemFilter(int port, Predicate<ItemStack> filter) {
        itemInputs.get(port).filter = filter;
    }

    @Override
    public void setFluidFilter(int port, Predicate<FluidStack> filter) {
        for (var tank : fluidInputs.get(port)) {
            tank.filter = filter;
        }
    }

    @Override
    public void resetFilter(int port) {
        if (itemInputs.containsKey(port)) {
            itemInputs.get(port).resetFilter();
        } else if (fluidInputs.containsKey(port)) {
            for (var tank : fluidInputs.get(port)) {
                tank.resetFilter();
            }
        }
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
            return be -> new StackProcessingContainer(be, ports);
        }
    }

    public static <P> Builder<P> builder(P parent) {
        return new Builder<>(parent);
    }
}
