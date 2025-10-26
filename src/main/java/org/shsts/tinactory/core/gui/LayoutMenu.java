package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;

import static org.shsts.tinactory.content.AllCapabilities.LAYOUT_PROVIDER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_FLUID_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;
import static org.shsts.tinactory.core.machine.Machine.getProcessor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutMenu extends InventoryMenu {
    public static final String FLUID_SYNC = "fluidSlot_";
    public static final String PROGRESS_SYNC = "progress";

    protected final Layout layout;

    protected LayoutMenu(Properties properties, Layout layout, int extraHeight) {
        super(properties, layout.rect.endY() + extraHeight);
        this.layout = layout;
    }

    protected LayoutMenu(Properties properties, int extraHeight) {
        this(properties,
            LAYOUT_PROVIDER.get(properties.blockEntity()).getLayout(),
            extraHeight);
    }

    /**
     * Called during constructor.
     */
    protected void addLayoutSlots(Layout layout) {
        var items = MENU_ITEM_HANDLER.get(blockEntity);
        var xOffset = layout.getXOffset();
        for (var slot : layout.slots) {
            var x = xOffset + slot.x() + MARGIN_X + 1;
            var y = slot.y() + MARGIN_TOP + 1;
            if (slot.type().portType == PortType.ITEM) {
                addSlot(new SlotItemHandler(items, slot.index(), x, y));
            }
        }
    }

    /**
     * Called during constructor.
     */
    protected void addProgressBar() {
        if (layout.progressBar != null) {
            addSyncSlot(PROGRESS_SYNC, () -> doublePacket(getProcessor(blockEntity)
                .map(IProcessor::getProgress)
                .orElse(0d)));
        }
    }

    /**
     * Called during constructor.
     */
    protected void addFluidSlots() {
        var fluids = MENU_FLUID_HANDLER.get(blockEntity);
        for (var slot : layout.slots) {
            if (slot.type().portType == PortType.FLUID) {
                addSyncSlot(FLUID_SYNC + slot.index(),
                    () -> new FluidSyncPacket(fluids.getFluidInTank(slot.index())));
            }
        }
        onEventPacket(FLUID_SLOT_CLICK, p -> clickFluidSlot(fluids, p.getIndex(), p.getButton()));
    }

    public Layout layout() {
        return layout;
    }
}
