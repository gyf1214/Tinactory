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
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlexibleStackContainer extends CapabilityProvider
    implements IFlexibleContainer, ILayoutProvider, INBTSerializable<CompoundTag> {
    private static final String ID = StackProcessingContainer.ID;

    private final BlockEntity blockEntity;
    private final WrapperItemHandler internalItems;
    private final WrapperItemHandler menuItems;
    private final WrapperItemHandler externalItems;
    private final WrapperFluidTank[] internalFluids;
    private final WrapperFluidTank[] externalFluids;
    private final CombinedFluidTank combinedFluids;
    private final List<PortInfo> ports = new ArrayList<>();
    private final LazyOptional<?> itemHandlerCap;
    private final LazyOptional<?> menuItemHandlerCap;
    private final LazyOptional<?> fluidHandlerCap;

    private Layout layout = Layout.EMPTY;

    public FlexibleStackContainer(BlockEntity blockEntity, int maxItemSlots, int maxFluidSlots) {
        this.blockEntity = blockEntity;

        this.internalItems = new WrapperItemHandler(maxItemSlots);
        this.menuItems = new WrapperItemHandler(internalItems);
        this.externalItems = new WrapperItemHandler(menuItems);
        for (var i = 0; i < maxItemSlots; i++) {
            menuItems.disallowInput(i);
        }
        internalItems.onUpdate(this::onUpdate);

        this.internalFluids = new WrapperFluidTank[maxFluidSlots];
        this.externalFluids = new WrapperFluidTank[maxFluidSlots];
        var fluidSize = TinactoryConfig.INSTANCE.fluidSlotSize.get();
        for (var i = 0; i < maxFluidSlots; i++) {
            internalFluids[i] = new WrapperFluidTank(fluidSize);
            internalFluids[i].onUpdate(this::onUpdate);
            externalFluids[i] = new WrapperFluidTank(internalFluids[i]);
            externalFluids[i].allowInput = false;
        }

        this.combinedFluids = new CombinedFluidTank(externalFluids);

        this.itemHandlerCap = LazyOptional.of(() -> externalItems);
        this.menuItemHandlerCap = LazyOptional.of(() -> menuItems);
        this.fluidHandlerCap = LazyOptional.of(() -> combinedFluids);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, be -> new FlexibleStackContainer(be, 16, 8));
    }

    private record PortInfo(SlotType type, IPort port, IPort internal) {}

    private PortInfo createItemPort(SlotType type, List<Layout.SlotInfo> slots) {
        if (slots.isEmpty()) {
            return new PortInfo(SlotType.NONE, IPort.EMPTY, IPort.EMPTY);
        }

        var minSlot = slots.get(0).index();
        var maxSlot = slots.get(slots.size() - 1).index() + 1;
        assert minSlot >= 0 && maxSlot <= menuItems.getSlots() && minSlot < maxSlot;
        if (type == SlotType.ITEM_INPUT) {
            for (var i = minSlot; i < maxSlot; i++) {
                menuItems.resetFilter(i);
                externalItems.setAllowOutput(i, false);
            }
        }

        var allowOutput = type.direction != PortDirection.INPUT;
        var internalPort = new ItemHandlerCollection(internalItems, minSlot, maxSlot);
        var externalPort = new ItemHandlerCollection(externalItems, minSlot, maxSlot, allowOutput);
        return new PortInfo(type, externalPort, internalPort);
    }

    private PortInfo createFluidPort(SlotType type, List<Layout.SlotInfo> slots) {
        var externalTanks = new WrapperFluidTank[slots.size()];
        var internalTanks = new WrapperFluidTank[slots.size()];
        var k = 0;
        for (var slot : slots) {
            var i = slot.index();
            if (type == SlotType.FLUID_INPUT) {
                externalFluids[i].allowInput = true;
            }
            externalTanks[k] = externalFluids[i];
            internalTanks[k] = internalFluids[i];
            k++;
        }

        var allowOutput = type.direction != PortDirection.INPUT;
        var internalPort = new CombinedFluidTank(internalTanks);
        var externalPort = new CombinedFluidTank(allowOutput, externalTanks);
        return new PortInfo(type, externalPort, internalPort);
    }

    @Override
    public Layout getLayout() {
        return layout;
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
        this.layout = layout;
    }

    @Override
    public void resetLayout() {
        layout = Layout.EMPTY;
        for (var i = 0; i < menuItems.getSlots(); i++) {
            menuItems.disallowInput(i);
            externalItems.setAllowOutput(i, true);
        }
        for (var fluid : externalFluids) {
            fluid.allowInput = false;
            fluid.resetFilter();
        }
        ports.clear();
    }

    private void onUpdate() {
        invoke(blockEntity, CONTAINER_CHANGE);
        blockEntity.setChanged();
    }

    @Override
    public Optional<? extends ITeamProfile> getOwnerTeam() {
        return MACHINE.tryGet(blockEntity).flatMap(IMachine::owner);
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
        if (cap == LAYOUT_PROVIDER.get() || cap == CONTAINER.get()) {
            return myself();
        } else if (cap == FLUID_STACK_HANDLER.get()) {
            return fluidHandlerCap.cast();
        } else if (cap == ITEM_HANDLER.get()) {
            return itemHandlerCap.cast();
        } else if (cap == MENU_ITEM_HANDLER.get()) {
            return menuItemHandlerCap.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        tag.put("stack", StackHelper.serializeItemHandler(internalItems));
        tag.put("fluid", combinedFluids.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        StackHelper.deserializeItemHandler(internalItems, tag.getCompound("stack"));
        combinedFluids.deserializeNBT(tag.getCompound("fluid"));
    }
}
