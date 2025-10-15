package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;
import static org.shsts.tinactory.content.AllCapabilities.MENU_FLUID_HANDLER;
import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.sync.SyncPackets.doublePacket;
import static org.shsts.tinactory.core.machine.Machine.getProcessor;
import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingMenu extends LayoutMenu {
    public static final String FLUID_SLOT = "fluidSlot_";

    protected ProcessingMenu(Properties properties, int extraHeight) {
        super(properties, extraHeight);
        addSlots();
    }

    /**
     * Called during constructor.
     */
    protected void addFluidSlots() {
        var fluids = MENU_FLUID_HANDLER.get(blockEntity);
        for (var slot : layout.slots) {
            if (slot.type().portType == PortType.FLUID) {
                addSyncSlot(FLUID_SLOT + slot.index(),
                    () -> new FluidSyncPacket(fluids.getFluidInTank(slot.index())));
            }
        }
        onEventPacket(FLUID_SLOT_CLICK, p -> clickFluidSlot(fluids, p.getIndex(), p.getButton()));
    }

    /**
     * Called during constructor.
     */
    protected void addProgressBar() {
        if (layout.progressBar != null) {
            addSyncSlot("progress", () -> doublePacket(getProcessor(blockEntity)
                .map(IProcessor::getProgress)
                .orElse(0d)));
        }
    }

    /**
     * Called during constructor.
     */
    private void addSlots() {
        addLayoutSlots(layout);
        addFluidSlots();
        addProgressBar();
    }

    public Optional<Block> machineBlock() {
        return MACHINE.tryGet(blockEntity)
            .flatMap(IMachine::workBlock)
            .map(BlockState::getBlock);
    }

    public static Component getTitle(BlockEntity be) {
        return MACHINE.tryGet(be)
            .map(IMachine::title)
            .orElse(TextComponent.EMPTY);
    }

    public static Component portLabel(PortType type, int index) {
        var key = "tinactory.gui.portName." + type.name().toLowerCase() + "Label";
        return tr(key, index);
    }

    public static class Primitive extends ProcessingMenu {
        public Primitive(Properties properties) {
            super(properties, SLOT_SIZE / 2);
        }
    }

    public static ProcessingMenu primitive(Properties properties) {
        return new Primitive(properties);
    }
}
