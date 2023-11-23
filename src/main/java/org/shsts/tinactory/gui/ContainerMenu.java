package org.shsts.tinactory.gui;

import com.mojang.logging.LogUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import net.minecraftforge.network.PacketDistributor;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.logistics.IFluidStackHandler;
import org.shsts.tinactory.gui.layout.Rect;
import org.shsts.tinactory.gui.sync.ContainerEventHandler;
import org.shsts.tinactory.gui.sync.ContainerEventPacket;
import org.shsts.tinactory.gui.sync.ContainerSyncPacket;
import org.shsts.tinactory.gui.sync.FluidEventPacket;
import org.shsts.tinactory.gui.sync.FluidSyncPacket;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerMenu<T extends BlockEntity> extends AbstractContainerMenu {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int WIDTH = 176;
    public static final int SLOT_SIZE = 18;
    public static final int CONTENT_WIDTH = 9 * SLOT_SIZE;
    public static final int MARGIN_HORIZONTAL = (WIDTH - CONTENT_WIDTH) / 2;
    public static final int FONT_HEIGHT = 9;
    public static final int SPACING_VERTICAL = 3;
    public static final int MARGIN_VERTICAL = 3 + SPACING_VERTICAL;
    public static final int MARGIN_TOP = MARGIN_VERTICAL + FONT_HEIGHT + SPACING_VERTICAL;
    public static final int DEFAULT_Z_INDEX = 20;

    public final boolean isClientSide;
    public final T blockEntity;
    public final Player player;
    public final Inventory inventory;

    protected final LazyOptional<IItemHandler> container;
    protected final LazyOptional<IFluidStackHandler> fluidContainer;
    protected int containerSlotCount;
    protected boolean hasInventory;
    protected int height;

    protected record EventHandler<P extends ContainerEventPacket>(Class<P> clazz, Consumer<P> handler) {
        public void handle(ContainerEventPacket packet) {
            if (clazz.isInstance(packet)) {
                this.handler.accept(clazz.cast(packet));
            }
        }
    }

    protected final Map<Integer, EventHandler<?>> eventHandlers = new HashMap<>();

    @FunctionalInterface
    public interface SyncPacketFactory<T extends BlockEntity, P extends ContainerSyncPacket> {
        P create(int containerId, int index, ContainerMenu<T> menu, T be);
    }

    protected static abstract class SyncSlot<T extends BlockEntity, P extends ContainerSyncPacket> {
        private final Class<P> clazz;
        private @Nullable P packet = null;

        protected SyncSlot(Class<P> clazz) {
            this.clazz = clazz;
        }

        protected abstract P getPacket(ContainerMenu<T> menu, T be);

        public void syncPacket(ContainerMenu<T> menu, T be) {
            if (menu.player instanceof ServerPlayer player) {
                var packet = this.getPacket(menu, be);
                if (!packet.equals(this.packet)) {
                    this.packet = packet;
                    Tinactory.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
                }
            }
        }

        public void setPacket(ContainerSyncPacket packet) {
            this.packet = clazz.cast(packet);
        }
    }

    protected final List<SyncSlot<T, ?>> syncSlots = new ArrayList<>();

    public ContainerMenu(ContainerMenuType<T, ?> type, int id, Inventory inventory, T blockEntity) {
        super(type, id);
        this.player = inventory.player;
        this.inventory = inventory;
        this.blockEntity = blockEntity;
        assert blockEntity.getLevel() != null;
        this.isClientSide = blockEntity.getLevel().isClientSide;
        this.container = blockEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        this.fluidContainer = blockEntity.getCapability(AllCapabilities.FLUID_STACK_HANDLER.get());
        this.height = 0;
    }

    @Override
    public boolean stillValid(Player player) {
        var be = this.blockEntity;
        var level = be.getLevel();
        var pos = be.getBlockPos();
        return player == this.player &&
                level == player.getLevel() &&
                level.getBlockEntity(pos) == be &&
                player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 64.0;
    }

    protected int addInventorySlots(int y) {
        var barY = y + 3 * SLOT_SIZE + SPACING_VERTICAL;
        for (var j = 0; j < 9; j++) {
            this.addSlot(new Slot(this.inventory, j, MARGIN_HORIZONTAL + j * SLOT_SIZE + 1, barY + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                this.addSlot(new Slot(this.inventory, 9 + i * 9 + j,
                        MARGIN_HORIZONTAL + j * SLOT_SIZE + 1, y + i * SLOT_SIZE + 1));
            }
        }
        return barY + SLOT_SIZE + MARGIN_VERTICAL;
    }

    /**
     * This is called before any menu callbacks
     */
    public void initLayout() {
        this.registerEvent(ContainerEventHandler.FLUID_CLICK, this::handleFluidEvent);
    }

    /**
     * This is called after all menu callbacks
     */
    public void setLayout(List<Rect> widgets, boolean hasInventory) {
        // here height does not include top margin and spacing
        var y = this.height;
        for (var widget : widgets) {
            y = Math.max(y, widget.endY());
        }
        y += MARGIN_TOP + SPACING_VERTICAL;
        this.containerSlotCount = this.slots.size();
        this.hasInventory = hasInventory;
        if (hasInventory) {
            y = addInventorySlots(y);
        } else {
            y += MARGIN_VERTICAL;
        }
        this.height = y;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!this.hasInventory || index < 0 || index >= this.slots.size() || this.isClientSide) {
            return ItemStack.EMPTY;
        }
        var slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        var inv = new PlayerMainInvWrapper(this.inventory);
        if (index < this.containerSlotCount) {
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
            var invIndex = index - this.containerSlotCount;
            var oldStack = inv.getStackInSlot(invIndex).copy();
            var reminder = oldStack.copy();
            var amount = reminder.getCount();
            for (var i = 0; i < this.containerSlotCount; i++) {
                if (reminder.isEmpty()) {
                    break;
                }
                var targetSlot = this.slots.get(i);
                reminder = targetSlot.safeInsert(reminder);
            }
            inv.extractItem(invIndex, amount - reminder.getCount(), false);

            return oldStack;
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
        this.addSlot(factory.create(posX + MARGIN_HORIZONTAL + 1, posY + MARGIN_TOP + 1));
    }

    public void addSlot(SlotFactory<?> factory, int slotIndex, int posX, int posY) {
        var itemHandler = this.container.orElseThrow(NoSuchElementException::new);
        this.addSlot(factory.create(itemHandler, slotIndex,
                posX + MARGIN_HORIZONTAL + 1, posY + MARGIN_TOP + 1));
    }

    public <P extends ContainerSyncPacket>
    int addSyncSlot(Class<P> clazz, SyncPacketFactory<T, P> factory) {
        int index = this.syncSlots.size();
        this.syncSlots.add(new SyncSlot<>(clazz) {
            @Override
            protected P getPacket(ContainerMenu<T> menu, T be) {
                return factory.create(menu.containerId, index, menu, be);
            }
        });
        return index;
    }

    public int addFluidSlot(int tank) {
        var fluidHandler = fluidContainer.orElseThrow(NoSuchElementException::new);
        return this.addSyncSlot(FluidSyncPacket.class, (containerId1, index, menu, be) ->
                new FluidSyncPacket(containerId, index, fluidHandler.getTank(tank).getFluid()));
    }

    public void handleFluidEvent(FluidEventPacket packet) {
        this.fluidContainer.ifPresent(handler -> {
            var tank = handler.getTank(packet.getTankIndex());
            LOGGER.debug("fluid event tank={} item={} button={}", tank.getFluid(),
                    this.getCarried(), packet.getButton());
        });
    }

    public <P extends ContainerEventPacket>
    void registerEvent(ContainerEventHandler.Event<P> event, Consumer<P> handler) {
        assert !this.eventHandlers.containsKey(event.id());
        this.eventHandlers.put(event.id(), new EventHandler<>(event.clazz(), handler));
    }

    /**
     * Called by client bound handler.
     */
    public void onSyncPacket(int index, ContainerSyncPacket packet) {
        var slot = this.syncSlots.get(index);
        if (slot == null || !slot.clazz.isInstance(packet)) {
            return;
        }
        slot.setPacket(packet);
    }

    /**
     * Called by Screen.
     */
    public <P extends ContainerSyncPacket> Optional<P> getSyncPacket(int index, Class<P> clazz) {
        var slot = this.syncSlots.get(index);
        if (slot == null || !clazz.isInstance(slot.packet)) {
            return Optional.empty();
        }
        return Optional.of(clazz.cast(slot.packet));
    }

    @FunctionalInterface
    public interface EventPacketFactory<P extends ContainerEventPacket> {
        P create(int containerId, int eventId);
    }

    public <P extends ContainerEventPacket>
    void triggerEvent(ContainerEventHandler.Event<P> event, EventPacketFactory<P> factory) {
        Tinactory.CHANNEL.sendToServer(factory.create(this.containerId, event.id()));
    }

    public void onEventPacket(ContainerEventPacket packet) {
        var handler = this.eventHandlers.get(packet.getEventId());
        if (handler != null) {
            handler.handle(packet);
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        for (var slot : this.syncSlots) {
            slot.syncPacket(this, this.blockEntity);
        }
    }

    public interface Factory<T1 extends BlockEntity, M1 extends ContainerMenu<T1>> {
        M1 create(ContainerMenuType<T1, M1> type, int id, Inventory inventory, T1 blockEntity);
    }
}
