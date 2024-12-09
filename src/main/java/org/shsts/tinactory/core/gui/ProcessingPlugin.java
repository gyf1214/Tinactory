package org.shsts.tinactory.core.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.sync.FluidSyncPacket;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.Optional;

import static org.shsts.tinactory.content.AllCapabilities.FLUID_STACK_HANDLER;
import static org.shsts.tinactory.content.AllCapabilities.MENU_ITEM_HANDLER;
import static org.shsts.tinactory.content.AllMenus.FLUID_SLOT_CLICK;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ProcessingPlugin extends LayoutPlugin<ProcessingScreen> {
    protected ProcessingPlugin(IMenu menu, int extraHeight) {
        super(menu, extraHeight);
        var blockEntity = menu.blockEntity();
        var items = MENU_ITEM_HANDLER.get(blockEntity);
        var fluids = FLUID_STACK_HANDLER.get(blockEntity);
        var xOffset = layout.getXOffset();
        for (var slot : layout.slots) {
            var x = xOffset + slot.x() + MARGIN_HORIZONTAL + 1;
            var y = slot.y() + MARGIN_TOP + 1;
            switch (slot.type().portType) {
                case ITEM -> menu.addSlot(new SlotItemHandler(items, slot.index(), x, y));
                case FLUID -> menu.addSyncSlot("fluidSlot_" + slot.index(),
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

    protected abstract Optional<RecipeType<?>> recipeType();

    @Override
    @OnlyIn(Dist.CLIENT)
    public Class<ProcessingScreen> menuScreenClass() {
        return ProcessingScreen.class;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(ProcessingScreen screen) {
        super.applyMenuScreen(screen);
        recipeType().ifPresent(screen::setRecipeType);
    }
}
