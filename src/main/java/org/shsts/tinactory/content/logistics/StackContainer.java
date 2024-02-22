package org.shsts.tinactory.content.logistics;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackContainer implements ICapabilityProvider, IContainer, INBTSerializable<CompoundTag> {
    protected record PortInfo(int slots, Layout.SlotType type) {}

    protected final BlockEntity blockEntity;
    protected final IItemHandlerModifiable combinedItems;
    protected final CombinedFluidTank combinedFluids;
    protected final List<IPort> ports;
    protected final List<IPort> internalPorts;
    @Nullable
    protected IProcessor processor = null;

    public StackContainer(BlockEntity blockEntity, Collection<PortInfo> ports) {
        this.blockEntity = blockEntity;
        this.ports = new ArrayList<>(ports.size());
        this.internalPorts = new ArrayList<>(ports.size());
        var items = new ArrayList<WrapperItemHandler>(ports.size());
        var fluids = new ArrayList<WrapperFluidTank>();
        for (var port : ports) {
            if (ports.size() == 0) {
                this.internalPorts.add(IPort.EMPTY);
                this.ports.add(IPort.EMPTY);
                continue;
            }
            switch (port.type()) {
                case ITEM_INPUT -> {
                    var view = new WrapperItemHandler(port.slots);
                    view.onUpdate(this::onUpdate);
                    items.add(view);

                    var collection = new ItemHandlerCollection(view);
                    this.internalPorts.add(collection);
                    this.ports.add(collection);
                }
                case ITEM_OUTPUT -> {
                    var inner = new WrapperItemHandler(port.slots);
                    inner.onUpdate(this::onUpdate);

                    var view = new WrapperItemHandler(inner);
                    view.allowInput = false;
                    items.add(view);

                    this.internalPorts.add(new ItemHandlerCollection(inner));
                    this.ports.add(new ItemHandlerCollection(view));
                }
                case FLUID_INPUT -> {
                    var views = new WrapperFluidTank[port.slots];
                    for (var i = 0; i < port.slots; i++) {
                        var view = new WrapperFluidTank(TinactoryConfig.INSTANCE.fluidSlotSize.get());
                        view.onUpdate(this::onUpdate);

                        views[i] = view;
                        fluids.add(view);
                    }

                    var collection = new CombinedFluidTank(views);
                    this.internalPorts.add(collection);
                    this.ports.add(collection);
                }
                case FLUID_OUTPUT -> {
                    var inners = new WrapperFluidTank[port.slots];
                    var views = new WrapperFluidTank[port.slots];

                    for (var i = 0; i < port.slots; i++) {
                        var inner = new WrapperFluidTank(TinactoryConfig.INSTANCE.fluidSlotSize.get());
                        inner.onUpdate(this::onUpdate);
                        inners[i] = inner;

                        var view = new WrapperFluidTank(inner);
                        view.allowInput = false;
                        views[i] = view;
                        fluids.add(view);
                    }

                    this.internalPorts.add(new CombinedFluidTank(inners));
                    this.ports.add(new CombinedFluidTank(views));
                }
            }
        }
        this.combinedItems = new CombinedInvWrapper(items.toArray(IItemHandlerModifiable[]::new));
        this.combinedFluids = new CombinedFluidTank(fluids.toArray(WrapperFluidTank[]::new));
    }

    protected IProcessor getProcessor() {
        if (this.processor == null) {
            this.processor = this.blockEntity.getCapability(AllCapabilities.PROCESSOR.get())
                    .orElseThrow(NoSuchElementException::new);
        }
        return this.processor;
    }

    protected void onUpdate() {
        this.getProcessor().onContainerUpdate();
        this.blockEntity.setChanged();
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < this.ports.size() &&
                this.ports.get(port) != IPort.EMPTY;
    }

    @Override
    public IPort getPort(int port, boolean internal) {
        if (!this.hasPort(port)) {
            return IPort.EMPTY;
        }
        return internal ? this.internalPorts.get(port) : this.ports.get(port);
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return LazyOptional.of(() -> this.combinedItems).cast();
        } else if (cap == AllCapabilities.FLUID_STACK_HANDLER.get()) {
            return LazyOptional.of(() -> this.combinedFluids).cast();
        } else if (cap == AllCapabilities.CONTAINER.get()) {
            return LazyOptional.of(() -> this).cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("stack", ItemHelper.serializeItemHandler(this.combinedItems));
        tag.put("fluid", this.combinedFluids.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(this.combinedItems, tag.getCompound("stack"));
        this.combinedFluids.deserializeNBT(tag.getCompound("fluid"));
    }

    public static class Builder implements Function<BlockEntity, ICapabilityProvider> {
        private final List<PortInfo> ports = new ArrayList<>();

        public Builder layout(Layout layout, Voltage voltage) {
            this.ports.clear();
            var slots = layout.getStackSlots(voltage);
            if (slots.isEmpty()) {
                return this;
            }
            var portCount = 1 + slots.stream().mapToInt(Layout.SlotInfo::port).max().getAsInt();
            var ports = new ArrayList<>(Collections.nCopies(portCount, new PortInfo(0, Layout.SlotType.NONE)));
            for (var slot : slots) {
                var info = ports.get(slot.port());
                ports.set(slot.port(), new PortInfo(info.slots + 1, slot.type()));
            }
            this.ports.addAll(ports);
            return this;
        }

        @Override
        public ICapabilityProvider apply(BlockEntity be) {
            return new StackContainer(be, this.ports);
        }
    }
}
