package org.shsts.tinactory.integration.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.network.IPacket;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllCapabilities.MENU_FLUID_HANDLER;
import static org.shsts.tinactory.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutMenu extends InventoryMenu {
    public static final String FLUID_SYNC = "fluidSlot_";
    public static final String PROGRESS_SYNC = "progress";

    protected final Layout layout;
    @Nullable
    private final Function<FluidStack, IPacket> fluidSyncPacketFactory;

    protected LayoutMenu(Properties properties, Layout layout, int extraHeight,
        @Nullable Function<FluidStack, IPacket> fluidSyncPacketFactory) {
        super(properties, layout.rect.endY() + extraHeight);
        this.layout = layout;
        this.fluidSyncPacketFactory = fluidSyncPacketFactory;
    }

    protected LayoutMenu(Properties properties, int extraHeight,
        @Nullable Function<FluidStack, IPacket> fluidSyncPacketFactory) {
        this(properties,
            LAYOUT_PROVIDER.get(Objects.requireNonNull(properties.blockEntity())).getLayout(),
            extraHeight, fluidSyncPacketFactory);
    }

    /**
     * Called during constructor.
     */
    protected void addLayoutSlots(Layout layout) {
        MENU_ITEM_HANDLER.tryGet(blockEntity()).ifPresent(items -> {
            var xOffset = layout.getXOffset();
            for (var slot : layout.slots) {
                var x = xOffset + slot.x() + MARGIN_X + 1;
                var y = slot.y() + MARGIN_TOP + 1;
                if (slot.type().portType == PortType.ITEM) {
                    addSlot(new SlotItemHandler(items.asItemHandler(), slot.index(), x, y));
                }
            }
        });
    }

    /**
     * Called during constructor.
     */
    protected void addProgressBar() {
        if (layout.progressBar != null) {
            addSyncSlot(PROGRESS_SYNC, () -> doublePacket(getProcessor(blockEntity())
                .map(IProcessor::getProgress)
                .orElse(0d)));
        }
    }

    /**
     * Called during constructor.
     */
    protected void addFluidSlots() {
        MENU_FLUID_HANDLER.tryGet(blockEntity()).ifPresent(fluids -> {
            var packetFactory = Objects.requireNonNull(fluidSyncPacketFactory);
            for (var slot : layout.slots) {
                if (slot.type().portType == PortType.FLUID) {
                    addSyncSlot(FLUID_SYNC + slot.index(),
                        () -> packetFactory.apply(fluids.getFluidInTank(slot.index())));
                }
            }
            onEventPacket(FLUID_SLOT_CLICK, p -> clickFluidSlot(fluids, p.getIndex(), p.getButton()));
        });
    }

    public Layout layout() {
        return layout;
    }

    public static Optional<IProcessor> getProcessor(BlockEntity be) {
        var machine = MACHINE.tryGet(be);
        return machine.map(IMachine::processor)
            .orElseGet(() -> PROCESSOR.tryGet(be));
    }
}
