package org.shsts.tinactory.content.logistics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.common.EventManager;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackProcessingContainer extends CapabilityProvider
    implements IContainer, INBTSerializable<CompoundTag> {
    private record PortInfo(int startSlot, int endSlot, SlotType type,
        IPort port, IPort internalPort) {}

    private final BlockEntity blockEntity;
    private final IItemHandlerModifiable combinedItems;
    private final CombinedFluidTank combinedFluids;
    private final List<PortInfo> ports;

    private final LazyOptional<?> itemHandlerCap;
    private final LazyOptional<?> fluidHandlerCap;

    private StackProcessingContainer(BlockEntity blockEntity, List<Layout.PortInfo> portInfo) {
        this.blockEntity = blockEntity;
        this.ports = new ArrayList<>(portInfo.size());

        var items = new ArrayList<WrapperItemHandler>(portInfo.size());
        var fluids = new ArrayList<WrapperFluidTank>();
        var slotIdx = 0;
        for (var port : portInfo) {
            var type = port.type();
            if (port.slots() <= 0 || type == SlotType.NONE) {
                ports.add(new PortInfo(slotIdx, slotIdx, SlotType.NONE,
                    IPort.EMPTY, IPort.EMPTY));
                continue;
            }
            switch (type) {
                case ITEM_INPUT -> {
                    var view = new WrapperItemHandler(port.slots());
                    view.onUpdate(this::onUpdate);
                    items.add(view);

                    var collection = new ItemHandlerCollection(view);
                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots(), type,
                        collection, collection));
                }
                case ITEM_OUTPUT -> {
                    var inner = new WrapperItemHandler(port.slots());
                    inner.onUpdate(this::onUpdate);

                    var view = new WrapperItemHandler(inner);
                    view.allowInput = false;
                    items.add(view);

                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots(), type,
                        new ItemHandlerCollection(view), new ItemHandlerCollection(inner)));
                }
                case FLUID_INPUT -> {
                    var views = new WrapperFluidTank[port.slots()];
                    for (var i = 0; i < port.slots(); i++) {
                        var view = new WrapperFluidTank(TinactoryConfig.INSTANCE.fluidSlotSize.get());
                        view.onUpdate(this::onUpdate);

                        views[i] = view;
                        fluids.add(view);
                    }

                    var collection = new CombinedFluidTank(views);
                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots(), type,
                        collection, collection));
                }
                case FLUID_OUTPUT -> {
                    var inners = new WrapperFluidTank[port.slots()];
                    var views = new WrapperFluidTank[port.slots()];

                    for (var i = 0; i < port.slots(); i++) {
                        var inner = new WrapperFluidTank(TinactoryConfig.INSTANCE.fluidSlotSize.get());
                        inner.onUpdate(this::onUpdate);
                        inners[i] = inner;

                        var view = new WrapperFluidTank(inner);
                        view.allowInput = false;
                        views[i] = view;
                        fluids.add(view);
                    }

                    ports.add(new PortInfo(slotIdx, slotIdx + port.slots(), type,
                        new CombinedFluidTank(views), new CombinedFluidTank(inners)));
                }
            }
            slotIdx += port.slots();
        }
        this.combinedItems = new CombinedInvWrapper(items.toArray(IItemHandlerModifiable[]::new));
        this.combinedFluids = new CombinedFluidTank(fluids.toArray(WrapperFluidTank[]::new));

        this.itemHandlerCap = LazyOptional.of(() -> combinedItems);
        this.fluidHandlerCap = LazyOptional.of(() -> combinedFluids);
    }

    private void onUpdate() {
        EventManager.invoke(blockEntity, AllEvents.CONTAINER_CHANGE);
        blockEntity.setChanged();
    }

    @Override
    public Optional<? extends ITeamProfile> getOwnerTeam() {
        var world = blockEntity.getLevel();
        assert world != null;
        if (world.isClientSide) {
            return TechManager.localTeam();
        } else {
            return AllCapabilities.MACHINE.tryGet(blockEntity)
                .flatMap(Machine::getOwnerTeam);
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

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        } else if (cap == AllCapabilities.FLUID_STACK_HANDLER.get()) {
            return fluidHandlerCap.cast();
        } else if (cap == AllCapabilities.CONTAINER.get()) {
            return myself();
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
        private final List<Layout.PortInfo> ports = new ArrayList<>();

        public Builder(P parent) {
            super(parent, "logistics/stack_container");
        }

        public Builder<P> layout(Layout layout) {
            ports.clear();
            ports.addAll(layout.ports);
            return this;
        }

        @Override
        protected Function<BlockEntity, ICapabilityProvider> createObject() {
            var ports = this.ports;
            return be -> new StackProcessingContainer(be, ports);
        }
    }

    public static <P> Builder<P> builder(P parent) {
        return new Builder<>(parent);
    }

    public static <P> Function<P, CapabilityProviderBuilder<BlockEntity, P>> builder(Layout layout) {
        return p -> builder(p).layout(layout);
    }
}
