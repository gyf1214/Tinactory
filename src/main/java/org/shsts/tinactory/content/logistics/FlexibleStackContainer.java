package org.shsts.tinactory.content.logistics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.TinactoryConfig;
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
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.ItemHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlexibleStackContainer extends CapabilityProvider
    implements IFlexibleContainer, INBTSerializable<CompoundTag> {
    private final BlockEntity blockEntity;
    private final WrapperItemHandler items;
    private final WrapperItemHandler internalItems;
    private final WrapperFluidTank[] fluids;
    private final WrapperFluidTank[] internalFluids;
    private final CombinedFluidTank combinedFluids;
    private final List<PortInfo> ports = new ArrayList<>();
    private final LazyOptional<IItemHandler> itemHandlerCap;
    private final LazyOptional<IFluidStackHandler> fluidHandlerCap;

    public FlexibleStackContainer(BlockEntity blockEntity, int maxItemSlots, int maxFluidSlots) {
        this.blockEntity = blockEntity;

        this.internalItems = new WrapperItemHandler(maxItemSlots);
        this.items = new WrapperItemHandler(internalItems);
        for (var i = 0; i < maxItemSlots; i++) {
            items.setFilter(i, $ -> false);
        }
        internalItems.onUpdate(this::onUpdate);

        this.internalFluids = new WrapperFluidTank[maxFluidSlots];
        this.fluids = new WrapperFluidTank[maxFluidSlots];
        var fluidSize = TinactoryConfig.INSTANCE.fluidSlotSize.get();
        for (var i = 0; i < maxFluidSlots; i++) {
            internalFluids[i] = new WrapperFluidTank(fluidSize);
            internalFluids[i].onUpdate(this::onUpdate);
            fluids[i] = new WrapperFluidTank(internalFluids[i]);
            fluids[i].allowInput = false;
        }

        this.combinedFluids = new CombinedFluidTank(fluids);
        this.itemHandlerCap = LazyOptional.of(() -> items);
        this.fluidHandlerCap = LazyOptional.of(() -> combinedFluids);
    }

    private record PortInfo(SlotType type, IPort port, IPort internal) {}

    private PortInfo createItemPort(SlotType type, List<Layout.SlotInfo> slots) {
        if (slots.isEmpty()) {
            return new PortInfo(SlotType.NONE, IPort.EMPTY, IPort.EMPTY);
        }

        var minSlot = slots.get(0).index();
        var maxSlot = slots.get(slots.size() - 1).index() + 1;
        assert minSlot >= 0 && maxSlot <= items.getSlots() && minSlot < maxSlot;
        if (type == SlotType.ITEM_INPUT) {
            for (var i = minSlot; i < maxSlot; i++) {
                items.resetFilter(i);
            }
        }

        var port = new ItemHandlerCollection(items, minSlot, maxSlot);
        var internalPort = new ItemHandlerCollection(internalItems, minSlot, maxSlot);
        return new PortInfo(type, port, internalPort);
    }

    private PortInfo createFluidPort(SlotType type, List<Layout.SlotInfo> slots) {
        var views = new WrapperFluidTank[slots.size()];
        var internalViews = new WrapperFluidTank[slots.size()];
        var k = 0;
        for (var slot : slots) {
            var i = slot.index();
            if (type == SlotType.FLUID_INPUT) {
                fluids[i].allowInput = true;
            }
            views[k] = fluids[i];
            internalViews[k] = internalFluids[i];
            k++;
        }
        var view = new CombinedFluidTank(views);
        var internalView = new CombinedFluidTank(internalViews);
        return new PortInfo(type, view, internalView);
    }

    @Override
    public void setLayout(Layout layout) {
        resetLayout();

        for (var k = 0; k < layout.ports.size(); k++) {
            var type = layout.ports.get(k).type();
            var slots = layout.portSlots.get(k);

            var portInfo = switch (type.portType) {
                case ITEM -> createItemPort(type, slots);
                case FLUID -> createFluidPort(type, slots);
                default -> new PortInfo(SlotType.NONE, IPort.EMPTY, IPort.EMPTY);
            };

            ports.add(portInfo);
        }
    }

    @Override
    public void resetLayout() {
        for (var i = 0; i < items.getSlots(); i++) {
            items.setFilter(i, $ -> false);
        }
        for (var fluid : fluids) {
            fluid.allowInput = false;
            fluid.resetFilter();
        }
        ports.clear();
    }

    private void onUpdate() {
        EventManager.invoke(blockEntity, AllEvents.CONTAINER_CHANGE);
        blockEntity.setChanged();
    }

    @Override
    public Optional<? extends ITeamProfile> getOwnerTeam() {
        return AllCapabilities.MACHINE.tryGet(blockEntity).flatMap(Machine::getOwnerTeam);
    }

    @Override
    public int portSize() {
        return ports.size();
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < ports.size() && ports.get(port).type != SlotType.NONE;
    }

    @Override
    public PortDirection portDirection(int port) {
        return ports.get(port).type.direction;
    }

    @Override
    public IPort getPort(int port, boolean internal) {
        var info = ports.get(port);
        return internal ? info.internal : info.port;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == AllCapabilities.CONTAINER.get()) {
            return myself();
        } else if (cap == AllCapabilities.FLUID_STACK_HANDLER.get()) {
            return fluidHandlerCap.cast();
        } else if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("stack", ItemHelper.serializeItemHandler(items));
        tag.put("fluid", combinedFluids.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        ItemHelper.deserializeItemHandler(items, tag.getCompound("stack"));
        combinedFluids.deserializeNBT(tag.getCompound("fluid"));
    }

    public static <P> CapabilityProviderBuilder<BlockEntity, P> builder(P parent) {
        return CapabilityProviderBuilder.fromFactory(parent, "logistics/stack_container",
            be -> new FlexibleStackContainer(be, 16, 8));
    }
}
