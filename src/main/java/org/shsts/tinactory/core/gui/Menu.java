package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.network.PacketDistributor;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.MenuEventPacket;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.core.logistics.IFluidStackHandler;
import org.shsts.tinactory.core.logistics.ItemHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Menu<T extends BlockEntity> extends AbstractContainerMenu {
    public static final int WIDTH = 176;
    public static final int SLOT_SIZE = 18;
    public static final int CONTENT_WIDTH = 9 * SLOT_SIZE;
    public static final int MARGIN_HORIZONTAL = (WIDTH - CONTENT_WIDTH) / 2;
    public static final int FONT_HEIGHT = 9;
    public static final int SPACING_VERTICAL = 3;
    public static final int MARGIN_VERTICAL = 3 + SPACING_VERTICAL;
    public static final int MARGIN_TOP = MARGIN_VERTICAL + FONT_HEIGHT + SPACING_VERTICAL;

    public final boolean isClientSide;
    public final T blockEntity;
    public final Player player;
    public final Inventory inventory;

    protected final @Nullable IItemHandler container;
    protected final @Nullable IFluidStackHandler fluidContainer;
    protected int containerSlotCount;
    protected boolean hasInventory;
    protected int height;

    protected record EventHandler<P extends MenuEventPacket>(Class<P> clazz, Consumer<P> handler) {
        public void handle(MenuEventPacket packet) {
            if (clazz.isInstance(packet)) {
                handler.accept(clazz.cast(packet));
            }
        }
    }

    protected final Map<Integer, EventHandler<?>> eventHandlers = new HashMap<>();

    @FunctionalInterface
    public interface SyncPacketFactory<T extends BlockEntity, P extends MenuSyncPacket> {
        P create(int containerId, int index, T be);
    }

    protected static abstract class SyncSlot<T extends BlockEntity, P extends MenuSyncPacket> {
        private final Class<P> clazz;
        @Nullable
        private P packet = null;
        private final List<Consumer<P>> callbacks = new ArrayList<>();

        protected SyncSlot(Class<P> clazz) {
            this.clazz = clazz;
        }

        protected abstract P getPacket(Menu<T> menu, T be);

        public void syncPacket(Menu<T> menu, T be) {
            if (menu.player instanceof ServerPlayer player) {
                var packet1 = getPacket(menu, be);
                if (!packet1.equals(packet)) {
                    packet = packet1;
                    Tinactory.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet1);
                }
            }
        }

        public void setPacket(MenuSyncPacket packet) {
            this.packet = clazz.cast(packet);
            for (var cb : callbacks) {
                cb.accept(this.packet);
            }
        }

        public void addCallback(Consumer<P> cb) {
            callbacks.add(cb);
        }
    }

    protected final List<SyncSlot<T, ?>> syncSlots = new ArrayList<>();

    public Menu(SmartMenuType<T, ?> type, int id, Inventory inventory, T blockEntity) {
        super(type, id);
        this.player = inventory.player;
        this.inventory = inventory;
        this.blockEntity = blockEntity;
        assert blockEntity.getLevel() != null;
        this.isClientSide = blockEntity.getLevel().isClientSide;
        this.container = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                .resolve().orElse(null);
        this.fluidContainer = blockEntity.getCapability(AllCapabilities.FLUID_STACK_HANDLER.get())
                .resolve().orElse(null);
        this.height = 0;
    }

    @Override
    public boolean stillValid(Player player) {
        var be = blockEntity;
        var level = be.getLevel();
        var pos = be.getBlockPos();
        return player == this.player &&
                level == player.getLevel() &&
                level.getBlockEntity(pos) == be &&
                player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0;
    }

    private int addInventorySlots(int y) {
        var barY = y + 3 * SLOT_SIZE + SPACING_VERTICAL;
        for (var j = 0; j < 9; j++) {
            addSlot(new Slot(inventory, j, MARGIN_HORIZONTAL + j * SLOT_SIZE + 1, barY + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                addSlot(new Slot(inventory, 9 + i * 9 + j,
                        MARGIN_HORIZONTAL + j * SLOT_SIZE + 1, y + i * SLOT_SIZE + 1));
            }
        }
        return barY + SLOT_SIZE + MARGIN_VERTICAL;
    }

    /**
     * This is called before any menu callbacks
     */
    public void initLayout() {
        onEventPacket(MenuEventHandler.FLUID_CLICK, p ->
                clickFluidSlot(p.getTankIndex(), p.getButton()));
    }

    /**
     * This is called after all menu callbacks
     */
    public void setLayout(List<Rect> widgets, boolean hasInventory) {
        // here height does not include top margin and spacing
        var y = height;
        for (var widget : widgets) {
            y = Math.max(y, widget.endY());
        }
        y += MARGIN_TOP + SPACING_VERTICAL;
        containerSlotCount = slots.size();
        this.hasInventory = hasInventory;
        if (hasInventory) {
            y = addInventorySlots(y);
        } else {
            y += MARGIN_VERTICAL;
        }
        height = y;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!hasInventory || index < 0 || index >= slots.size() || isClientSide) {
            return ItemStack.EMPTY;
        }
        var slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        var inv = new PlayerMainInvWrapper(inventory);
        if (index < containerSlotCount) {
            if (!slot.mayPickup(player)) {
                return ItemStack.EMPTY;
            }
            var oldStack = slot.getItem().copy();
            var stack = oldStack.copy();
            var reminder = ItemHandlerHelper.insertItemStacked(inv, stack, true);
            stack.shrink(reminder.getCount());
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            var stack1 = slot.safeTake(stack.getCount(), Integer.MAX_VALUE, player);
            ItemHandlerHelper.insertItemStacked(inv, stack1, false);

            return oldStack;
        } else {
            var invIndex = index - containerSlotCount;
            var oldStack = inv.getStackInSlot(invIndex).copy();
            var stack = oldStack.copy();
            var amount = stack.getCount();
            for (var i = 0; i < containerSlotCount; i++) {
                var targetSlot = slots.get(i);
                if (!targetSlot.mayPlace(stack)) {
                    continue;
                }
                var reminder = targetSlot.safeInsert(stack);
                if (reminder.getCount() < amount) {
                    inv.extractItem(invIndex, amount - reminder.getCount(), false);
                    return oldStack;
                }
            }
            return ItemStack.EMPTY;
        }
    }

    public int getHeight() {
        return height;
    }

    @FunctionalInterface
    public interface BasicSlotFactory<T extends Slot> {
        T create(int posX, int posY);
    }

    @FunctionalInterface
    public interface SlotFactory<T extends Slot> {
        T create(IItemHandler itemHandler, int index, int posX, int posY);
    }

    public void addSlot(BasicSlotFactory<?> factory, int posX, int posY) {
        addSlot(factory.create(posX + MARGIN_HORIZONTAL + 1, posY + MARGIN_TOP + 1));
    }

    public void addSlot(SlotFactory<?> factory, int slotIndex, int posX, int posY) {
        assert container != null;
        addSlot(factory.create(container, slotIndex,
                posX + MARGIN_HORIZONTAL + 1, posY + MARGIN_TOP + 1));
    }

    public <P extends MenuSyncPacket>
    int addSyncSlot(Class<P> clazz, SyncPacketFactory<T, P> factory) {
        int index = syncSlots.size();
        syncSlots.add(new SyncSlot<>(clazz) {
            @Override
            protected P getPacket(Menu<T> menu, T be) {
                return factory.create(menu.containerId, index, be);
            }
        });
        return index;
    }

    public int addFluidSlot(int tank) {
        assert fluidContainer != null;
        return addSyncSlot(FluidSyncPacket.class, (containerId1, index, be) ->
                new FluidSyncPacket(containerId1, index, fluidContainer.getTank(tank).getFluid()));
    }

    protected enum FluidClickAction {
        NONE, FILL, DRAIN
    }

    protected record FluidClickResult(FluidClickAction action, ItemStack stack) {
        public FluidClickResult() {
            this(FluidClickAction.NONE, ItemStack.EMPTY);
        }
    }

    protected FluidClickResult doClickFluidSlot(ItemStack item, IFluidTank tank,
                                                boolean mayDrain, boolean mayFill) {
        var cap = FluidUtil.getFluidHandler(item).resolve();
        if (cap.isEmpty()) {
            return new FluidClickResult();
        }
        var handler = cap.get();
        if (mayFill) {
            var capacity = tank.getCapacity() - tank.getFluidAmount();
            var fluid1 = handler.drain(capacity, IFluidHandler.FluidAction.SIMULATE);
            if (!fluid1.isEmpty()) {
                int amount = tank.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
                if (amount > 0) {
                    var fluid2 = new FluidStack(fluid1, amount);
                    var fluid3 = handler.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                    tank.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                    return new FluidClickResult(FluidClickAction.FILL, handler.getContainer());
                }
            }
        }
        if (mayDrain && tank.getFluidAmount() > 0) {
            var fluid1 = tank.drain(tank.getFluidAmount(), IFluidHandler.FluidAction.SIMULATE);
            int amount = handler.fill(fluid1, IFluidHandler.FluidAction.SIMULATE);
            if (amount > 0) {
                var fluid2 = new FluidStack(fluid1, amount);
                var fluid3 = tank.drain(fluid2, IFluidHandler.FluidAction.EXECUTE);
                handler.fill(fluid3, IFluidHandler.FluidAction.EXECUTE);
                return new FluidClickResult(FluidClickAction.DRAIN, handler.getContainer());
            }
        }
        return new FluidClickResult();
    }

    public void clickFluidSlot(int tankIndex, int button) {
        if (fluidContainer == null) {
            return;
        }
        var tank = fluidContainer.getTank(tankIndex);
        var item = getCarried();
        var outputItem = ItemStack.EMPTY;
        var mayDrain = true;
        var mayFill = true;
        while (!item.isEmpty()) {
            var item1 = ItemHandlerHelper.copyStackWithSize(item, 1);
            var clickResult = doClickFluidSlot(item1, tank, mayDrain, mayFill);
            if (clickResult.action == FluidClickAction.NONE) {
                break;
            } else if (clickResult.action == FluidClickAction.FILL) {
                mayDrain = false;
            } else {
                mayFill = false;
            }
            item.shrink(1);
            var retItem = clickResult.stack;
            var combinedItem = ItemHelper.combineStack(outputItem, retItem);
            if (combinedItem.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, retItem);
            } else {
                outputItem = combinedItem.get();
            }
            if (button != 0) {
                break;
            }
        }
        if (item.isEmpty()) {
            setCarried(outputItem);
        } else {
            ItemHandlerHelper.giveItemToPlayer(player, outputItem);
        }
    }

    /**
     * Called by Screen to get the latest sync packet.
     */
    public <P extends MenuSyncPacket> Optional<P> getSyncPacket(int index, Class<P> clazz) {
        var slot = syncSlots.get(index);
        if (!clazz.isInstance(slot.packet)) {
            return Optional.empty();
        }
        return Optional.of(clazz.cast(slot.packet));
    }

    /**
     * Callback added by Screen.
     */
    @SuppressWarnings("unchecked")
    public <P extends MenuSyncPacket> void onSyncPacket(int index, Consumer<P> handler) {
        var slot = (SyncSlot<T, P>) syncSlots.get(index);
        slot.addCallback(handler);
    }

    /**
     * Called by client bound handler.
     */
    public void handleSyncPacket(int index, MenuSyncPacket packet) {
        var slot = syncSlots.get(index);
        if (slot == null || !slot.clazz.isInstance(packet)) {
            return;
        }
        slot.setPacket(packet);
    }

    /**
     * Callback added by server.
     */
    public <P extends MenuEventPacket>
    void onEventPacket(MenuEventHandler.Event<P> event, Consumer<P> handler) {
        assert !eventHandlers.containsKey(event.id());
        eventHandlers.put(event.id(), new EventHandler<>(event.clazz(), handler));
    }

    /**
     * Called on client to trigger event on server.
     */
    public <P extends MenuEventPacket>
    void triggerEvent(MenuEventHandler.Event<P> event, MenuEventPacket.Factory<P> factory) {
        Tinactory.CHANNEL.sendToServer(factory.create(containerId, event.id()));
    }

    /**
     * Called by server bound packet handler.
     */
    public void handleEventPacket(MenuEventPacket packet) {
        var handler = eventHandlers.get(packet.getEventId());
        if (handler != null) {
            handler.handle(packet);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (var slot : syncSlots) {
            slot.syncPacket(this, blockEntity);
        }
    }

    public interface Factory<T1 extends BlockEntity, M1 extends Menu<T1>> {
        M1 create(SmartMenuType<T1, ?> type, int id, Inventory inventory, T1 blockEntity);
    }
}
