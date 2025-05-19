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
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.IPort;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
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
import org.shsts.tinycorelib.api.blockentity.IEventManager;
import org.shsts.tinycorelib.api.blockentity.IEventSubscriber;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.content.AllCapabilities.CONTAINER;
import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.ITEM_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllEvents.CONTAINER_CHANGE;
import static org.shsts.tinactory.content.AllEvents.REMOVED_IN_WORLD;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StackProcessingContainer extends CapabilityProvider
    implements IContainer, ILayoutProvider, IEventSubscriber, INBTSerializable<CompoundTag> {
    public static final String ID = "logistics/stack_container";

    private record PortInfo(SlotType type, IPort externalPort, IPort internalPort) {
        public static final PortInfo EMPTY = new PortInfo(SlotType.NONE, IPort.EMPTY, IPort.EMPTY);
    }

    private final BlockEntity blockEntity;
    private final Layout layout;
    private final WrapperItemHandler internalItems;
    private final CombinedFluidTank combinedFluids;
    private final List<PortInfo> ports;

    private final LazyOptional<?> itemHandlerCap;
    private final LazyOptional<?> menuItemHandlerCap;
    private final LazyOptional<?> fluidHandlerCap;

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
        var externalItems = new WrapperItemHandler(menuItems);
        var allFluids = new WrapperFluidTank[fluidSlots];
        internalItems.onUpdate(this::onUpdate);

        var itemIdx = 0;
        var fluidIdx = 0;

        for (var port : portInfo) {
            var type = port.type();
            if (port.slots() <= 0 || type == SlotType.NONE) {
                ports.add(PortInfo.EMPTY);
                continue;
            }

            if (type.portType == PortType.ITEM) {
                var endIdx = itemIdx + port.slots();

                for (var i = itemIdx; i < endIdx; i++) {
                    if (type.direction == PortDirection.INPUT) {
                        externalItems.setAllowOutput(i, false);
                    } else {
                        menuItems.disallowInput(i);
                    }
                }

                var allowOutput = type.direction != PortDirection.INPUT;
                var internalPort = new ItemHandlerCollection(internalItems, itemIdx, endIdx);
                var externalPort = new ItemHandlerCollection(externalItems, itemIdx, endIdx, allowOutput);
                ports.add(new PortInfo(type, externalPort, internalPort));

                itemIdx = endIdx;
            } else {
                var slots = port.slots();
                var internalFluids = new WrapperFluidTank[slots];
                var externalFluids = new WrapperFluidTank[slots];

                for (var i = 0; i < slots; i++) {
                    internalFluids[i] = new WrapperFluidTank(CONFIG.fluidSlotSize.get());
                    internalFluids[i].onUpdate(this::onUpdate);

                    if (type.direction == PortDirection.INPUT) {
                        externalFluids[i] = internalFluids[i];
                    } else {
                        externalFluids[i] = new WrapperFluidTank(internalFluids[i]);
                        externalFluids[i].allowInput = false;
                    }

                    allFluids[fluidIdx + i] = externalFluids[i];
                }

                var allowOutput = type.direction != PortDirection.INPUT;
                var internalPort = new CombinedFluidTank(internalFluids);
                var externalPort = new CombinedFluidTank(allowOutput, externalFluids);
                ports.add(new PortInfo(type, externalPort, internalPort));

                fluidIdx += slots;
            }
        }

        this.combinedFluids = new CombinedFluidTank(allFluids);

        this.itemHandlerCap = LazyOptional.of(() -> externalItems);
        this.menuItemHandlerCap = LazyOptional.of(() -> menuItems);
        this.fluidHandlerCap = LazyOptional.of(() -> combinedFluids);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> factory(Layout layout) {
        return $ -> $.capability(ID, be -> new StackProcessingContainer(be, layout));
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
        return internal ? portInfo.internalPort : portInfo.externalPort;
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
