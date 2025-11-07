package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.shsts.tinactory.api.logistics.ContainerAccess;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.logistics.CombinedFluidTank;
import org.shsts.tinactory.core.logistics.IFlexibleContainer;
import org.shsts.tinactory.core.logistics.IFluidTanksHandler;
import org.shsts.tinactory.core.logistics.IMenuItemHandler;
import org.shsts.tinactory.core.logistics.ItemHandlerCollection;
import org.shsts.tinactory.core.logistics.StackHelper;
import org.shsts.tinactory.core.logistics.WrapperFluidTank;
import org.shsts.tinactory.core.logistics.WrapperItemHandler;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_FLUID_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FlexibleStackContainer extends CapabilityProvider
    implements IFlexibleContainer, ILayoutProvider, IEventSubscriber, INBTSerializable<CompoundTag> {
    private static final String ID = StackProcessingContainer.ID;

    private final BlockEntity blockEntity;
    private final WrapperItemHandler internalItems;
    private final WrapperItemHandler menuItems;
    private final WrapperItemHandler externalItems;
    private final WrapperFluidTank[] internalFluids;
    private final WrapperFluidTank[] menuFluids;
    private final WrapperFluidTank[] externalFluids;
    private final CombinedFluidTank combinedFluids;
    private final List<ContainerPort> ports = new ArrayList<>();
    private final LazyOptional<IItemHandler> itemHandlerCap;
    private final LazyOptional<IMenuItemHandler> menuItemHandlerCap;
    private final LazyOptional<IFluidHandler> fluidHandlerCap;
    private final LazyOptional<IFluidTanksHandler> menuFluidHandlerCap;

    private Layout layout = Layout.EMPTY;

    public FlexibleStackContainer(BlockEntity blockEntity, int maxItemSlots, int maxFluidSlots) {
        this.blockEntity = blockEntity;

        this.internalItems = new WrapperItemHandler(maxItemSlots);
        this.menuItems = new WrapperItemHandler(internalItems);
        this.externalItems = new WrapperItemHandler(internalItems);
        for (var i = 0; i < maxItemSlots; i++) {
            menuItems.disallowInput(i);
            externalItems.disallowInput(i);
        }
        internalItems.onUpdate(this::onUpdate);

        this.internalFluids = new WrapperFluidTank[maxFluidSlots];
        this.menuFluids = new WrapperFluidTank[maxFluidSlots];
        this.externalFluids = new WrapperFluidTank[maxFluidSlots];
        var fluidSize = CONFIG.fluidSlotSize.get();
        for (var i = 0; i < maxFluidSlots; i++) {
            internalFluids[i] = new WrapperFluidTank(fluidSize);
            internalFluids[i].onUpdate(this::onUpdate);
            menuFluids[i] = new WrapperFluidTank(internalFluids[i]);
            menuFluids[i].disallowInput();
            externalFluids[i] = new WrapperFluidTank(internalFluids[i]);
            externalFluids[i].disallowInput();
        }

        this.combinedFluids = new CombinedFluidTank(internalFluids);
        var combinedMenuFluids = new CombinedFluidTank(menuFluids);
        var combinedExternalFluids = new CombinedFluidTank(externalFluids);

        this.itemHandlerCap = LazyOptional.of(() -> externalItems);
        this.menuItemHandlerCap = IMenuItemHandler.cap(menuItems);
        this.fluidHandlerCap = LazyOptional.of(() -> combinedExternalFluids);
        this.menuFluidHandlerCap = LazyOptional.of(() -> combinedMenuFluids);
    }

    public static <P> IBlockEntityTypeBuilder<P> factory(
        IBlockEntityTypeBuilder<P> builder) {
        return builder.capability(ID, be -> new FlexibleStackContainer(be, 16, 8));
    }

    private ContainerPort createItemPort(SlotType type, List<Layout.SlotInfo> slots) {
        if (slots.isEmpty()) {
            return ContainerPort.EMPTY;
        }

        var minSlot = slots.get(0).index();
        var maxSlot = slots.get(slots.size() - 1).index() + 1;
        assert minSlot >= 0 && maxSlot <= menuItems.getSlots() && minSlot < maxSlot;
        if (type == SlotType.ITEM_INPUT) {
            for (var i = minSlot; i < maxSlot; i++) {
                menuItems.resetFilter(i);
                externalItems.resetFilter(i);
                externalItems.setAllowOutput(i, false);
            }
        }

        var internalPort = new ItemHandlerCollection(internalItems, minSlot, maxSlot);
        var menuPort = new ItemHandlerCollection(menuItems, minSlot, maxSlot);
        var externalPort = new ItemHandlerCollection(externalItems, minSlot, maxSlot);
        return new ContainerPort(type, internalPort, menuPort, externalPort);
    }

    private ContainerPort createFluidPort(SlotType type, List<Layout.SlotInfo> slots) {
        if (slots.isEmpty()) {
            return ContainerPort.EMPTY;
        }

        var externalTanks = new WrapperFluidTank[slots.size()];
        var menuTanks = new WrapperFluidTank[slots.size()];
        var internalTanks = new WrapperFluidTank[slots.size()];
        var k = 0;
        for (var slot : slots) {
            var i = slot.index();
            if (type == SlotType.FLUID_INPUT) {
                menuFluids[i].resetFilter();
                externalFluids[i].resetFilter();
                externalFluids[i].allowOutput = false;
            }
            externalTanks[k] = externalFluids[i];
            menuTanks[k] = menuFluids[i];
            internalTanks[k] = internalFluids[i];
            k++;
        }

        var internalPort = new CombinedFluidTank(internalTanks);
        var menuPort = new CombinedFluidTank(menuTanks);
        var externalPort = new CombinedFluidTank(externalTanks);
        return new ContainerPort(type, internalPort, menuPort, externalPort);
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

            var containerPort = switch (type.portType) {
                case ITEM -> createItemPort(type, slots);
                case FLUID -> createFluidPort(type, slots);
                default -> ContainerPort.EMPTY;
            };

            ports.add(containerPort);
        }
        this.layout = layout;
    }

    @Override
    public void resetLayout() {
        layout = Layout.EMPTY;
        for (var i = 0; i < menuItems.getSlots(); i++) {
            menuItems.disallowInput(i);
            externalItems.disallowInput(i);
            externalItems.setAllowOutput(i, true);
        }
        for (var menuFluid : menuFluids) {
            menuFluid.disallowInput();
        }
        for (var externalFluid : externalFluids) {
            externalFluid.disallowInput();
            externalFluid.allowOutput = true;
        }
        ports.clear();
    }

    private void onUpdate() {
        invoke(blockEntity, CONTAINER_CHANGE);
        blockEntity.setChanged();
    }

    @Override
    public int portSize() {
        return ports.size();
    }

    @Override
    public boolean hasPort(int port) {
        return port >= 0 && port < ports.size() && ports.get(port).type() != SlotType.NONE;
    }

    @Override
    public PortDirection portDirection(int port) {
        return ports.get(port).type().direction;
    }

    @Override
    public IPort getPort(int port, ContainerAccess access) {
        return ports.get(port).get(access);
    }

    @Override
    public void subscribeEvents(IEventManager eventManager) {
        eventManager.subscribe(REMOVED_IN_WORLD.get(),
            world -> StackHelper.dropItemHandler(world, blockEntity.getBlockPos(), internalItems));
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == LAYOUT_PROVIDER.get() || cap == CONTAINER.get()) {
            return myself();
        } else if (cap == ITEM_HANDLER.get()) {
            return itemHandlerCap.cast();
        } else if (cap == MENU_ITEM_HANDLER.get()) {
            return menuItemHandlerCap.cast();
        } else if (cap == FLUID_HANDLER.get()) {
            return fluidHandlerCap.cast();
        } else if (cap == MENU_FLUID_HANDLER.get()) {
            return menuFluidHandlerCap.cast();
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
