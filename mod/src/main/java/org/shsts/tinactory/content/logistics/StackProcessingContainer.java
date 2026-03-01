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
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.core.common.CapabilityProvider;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.ILayoutProvider;
import org.shsts.tinactory.integration.logistics.CombinedFluidTank;
import org.shsts.tinactory.integration.logistics.IFluidTanksHandler;
import org.shsts.tinactory.integration.logistics.IMenuItemHandler;
import org.shsts.tinactory.integration.logistics.ItemHandlerPort;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.logistics.WrapperFluidTank;
import org.shsts.tinactory.integration.logistics.WrapperItemHandler;
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MENU_FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.AllEvents.REMOVED_IN_WORLD;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackProcessingContainer extends CapabilityProvider
    implements IContainer, ILayoutProvider, IEventSubscriber, INBTSerializable<CompoundTag> {
    public static final String ID = "logistics/stack_container";

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final WrapperItemHandler internalItems;
    private final CombinedFluidTank combinedFluids;
    private final List<ContainerPort> ports;

    private final LazyOptional<IItemHandler> itemHandlerCap;
    private final LazyOptional<IMenuItemHandler> menuItemHandlerCap;
    private final LazyOptional<IFluidHandler> fluidHandlerCap;
    private final LazyOptional<IFluidTanksHandler> menuFluidHandlerCap;

    private StackProcessingContainer(BlockEntity blockEntity, Layout layout) {
        this.blockEntity = blockEntity;
        this.layout = layout;
        var portInfo = layout.ports;
        this.ports = new ArrayList<>(portInfo.size());

        var itemSlots = 0;
        var fluidSlots = 0;
        for (var port : portInfo) {
            switch (port.type().portType) {
                case ITEM -> itemSlots += port.slots();
                case FLUID -> fluidSlots += port.slots();
            }
        }

        this.internalItems = new WrapperItemHandler(itemSlots);
        var menuItems = new WrapperItemHandler(internalItems);
        var externalItems = new WrapperItemHandler(internalItems);
        var allInternalFluids = new WrapperFluidTank[fluidSlots];
        var allMenuFluids = new WrapperFluidTank[fluidSlots];
        var allExternalFluids = new WrapperFluidTank[fluidSlots];
        internalItems.onUpdate(this::onUpdate);

        var itemIdx = 0;
        var fluidIdx = 0;

        for (var port : portInfo) {
            var type = port.type();
            if (port.slots() <= 0 || type == SlotType.NONE) {
                ports.add(ContainerPort.EMPTY);
                continue;
            }

            if (type.portType == PortType.ITEM) {
                var endIdx = itemIdx + port.slots();

                for (var i = itemIdx; i < endIdx; i++) {
                    if (type.direction == PortDirection.INPUT) {
                        externalItems.setAllowOutput(i, false);
                    } else {
                        menuItems.disallowInput(i);
                        externalItems.disallowInput(i);
                    }
                }

                var internalPort = new ItemHandlerPort(internalItems, itemIdx, endIdx);
                var menuPort = new ItemHandlerPort(menuItems, itemIdx, endIdx);
                var externalPort = new ItemHandlerPort(externalItems, itemIdx, endIdx);
                ports.add(new ContainerPort(type, internalPort, menuPort, externalPort));

                itemIdx = endIdx;
            } else {
                var slots = port.slots();
                var internalFluids = new WrapperFluidTank[slots];
                var menuFluids = new WrapperFluidTank[slots];
                var externalFluids = new WrapperFluidTank[slots];

                for (var i = 0; i < slots; i++) {
                    internalFluids[i] = new WrapperFluidTank(CONFIG.fluidSlotSize.get());
                    internalFluids[i].onUpdate(this::onUpdate);

                    if (type.direction == PortDirection.INPUT) {
                        menuFluids[i] = internalFluids[i];
                        externalFluids[i] = new WrapperFluidTank(internalFluids[i]);
                        externalFluids[i].allowOutput = false;
                    } else {
                        menuFluids[i] = new WrapperFluidTank(internalFluids[i]);
                        menuFluids[i].disallowInput();
                        externalFluids[i] = menuFluids[i];
                    }

                    allInternalFluids[fluidIdx + i] = internalFluids[i];
                    allMenuFluids[fluidIdx + i] = menuFluids[i];
                    allExternalFluids[fluidIdx + i] = externalFluids[i];
                }

                var internalPort = new CombinedFluidTank(internalFluids);
                var menuPort = new CombinedFluidTank(menuFluids);
                var externalPort = new CombinedFluidTank(externalFluids);
                ports.add(new ContainerPort(type, internalPort, menuPort, externalPort));

                fluidIdx += slots;
            }
        }

        this.combinedFluids = new CombinedFluidTank(allInternalFluids);
        var combinedMenuFluids = new CombinedFluidTank(allMenuFluids);
        var combinedExternalFluids = new CombinedFluidTank(allExternalFluids);

        this.itemHandlerCap = LazyOptional.of(() -> externalItems);
        this.menuItemHandlerCap = IMenuItemHandler.cap(menuItems);
        this.fluidHandlerCap = LazyOptional.of(() -> combinedExternalFluids);
        this.menuFluidHandlerCap = LazyOptional.of(() -> combinedMenuFluids);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout) {
        return $ -> $.capability(ID, be -> new StackProcessingContainer(be, layout));
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
        return port >= 0 && port < ports.size() &&
            ports.get(port).type() != SlotType.NONE;
    }

    @Override
    public PortDirection portDirection(int port) {
        return ports.get(port).type().direction;
    }

    @Override
    public IPort<?> getPort(int port, ContainerAccess access) {
        if (!hasPort(port)) {
            return IPort.empty();
        }
        return ports.get(port).get(access);
    }

    @Override
    public Layout getLayout() {
        return layout;
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
