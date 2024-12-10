package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;
import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.util.I18n.tr;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingPlugin extends LayoutPlugin<ProcessingScreen> {
    protected ProcessingPlugin(IMenu menu, int extraHeight) {
        super(menu, extraHeight);
        addSlots(menu, layout);

        var fluids = FLUID_STACK_HANDLER.get(menu.blockEntity());
        for (var slot : layout.slots) {
            if (slot.type().portType == PortType.FLUID) {
                menu.addSyncSlot("fluidSlot_" + slot.index(),
                    $ -> new FluidSyncPacket(fluids.getFluidInTank(slot.index())));
            }
        }

        if (layout.progressBar != null) {
            menu.addSyncSlot("progress", be -> new SyncPackets.Double(Machine.getProcessor(be)
                .map(IProcessor::getProgress)
                .orElse(0d)));
        }

        menu.onEventPacket(FLUID_SLOT_CLICK, p -> clickFluidSlot(fluids, p.getIndex(), p.getButton()));
    }

    public static Component getTitle(BlockEntity be) {
        return AllCapabilities.MACHINE.tryGet(be)
            .map(Machine::getTitle)
            .orElseGet(() -> I18n.name(be.getBlockState().getBlock()));
    }

    public static Component portLabel(PortType type, int index) {
        var key = "tinactory.gui.portName." + type.name().toLowerCase() + "Label";
        return tr(key, index);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<ProcessingScreen> menuScreenClass() {
        return ProcessingScreen.class;
    }
}
