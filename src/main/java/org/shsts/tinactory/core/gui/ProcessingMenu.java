package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IProcessor;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingMenu extends Menu<BlockEntity, ProcessingMenu> {
    public final Layout layout;
    private final Map<Layout.SlotInfo, Integer> fluidSyncIndex = new HashMap<>();
    private final int progressBarIndex;

    public ProcessingMenu(SmartMenuType<?, ?> type, int id, Inventory inventory, BlockEntity blockEntity,
                          Layout layout) {
        super(type, id, inventory, blockEntity);

        this.layout = layout;
        var xOffset = layout.getXOffset();
        for (var slot : layout.slots) {
            var x = xOffset + slot.x();
            var y = slot.y();
            switch (slot.type().portType) {
                case ITEM -> addSlot(slot.index(), x, y);
                case FLUID -> fluidSyncIndex.put(slot, addFluidSlot(slot.index()));
            }
        }
        if (layout.progressBar != null) {
            this.progressBarIndex = addSyncSlot(MenuSyncPacket.Double::new,
                    be -> Machine.getProcessor(be)
                            .map(IProcessor::getProgress)
                            .orElse(0d));
        } else {
            this.progressBarIndex = -1;
        }
        this.height = layout.rect.endY() + SLOT_SIZE + MARGIN_VERTICAL;
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && AllCapabilities.MACHINE.tryGet(blockEntity)
                .map(m -> m.canPlayerInteract(player))
                .orElse(true);
    }

    @OnlyIn(Dist.CLIENT)
    private class Screen extends MenuScreen<ProcessingMenu> {
        public Screen(Inventory inventory, Component title) {
            super(ProcessingMenu.this, inventory, title);

            var layoutPanel = new Panel(this);

            for (var slot : layout.slots) {
                if (slot.type().portType == PortType.FLUID) {
                    var syncSlot = fluidSyncIndex.get(slot);
                    var rect = new Rect(slot.x(), slot.y(), Menu.SLOT_SIZE, Menu.SLOT_SIZE);
                    var rect1 = rect.offset(1, 1).enlarge(-2, -2);
                    layoutPanel.addWidget(rect, new StaticWidget(menu, Texture.SLOT_BACKGROUND));
                    layoutPanel.addWidget(rect1, new FluidSlot(menu, slot.index(), syncSlot));
                }
            }

            for (var image : layout.images) {
                layoutPanel.addWidget(image.rect(), new StaticWidget(menu, image.texture()));
            }

            var progressBar = layout.progressBar;
            if (progressBar != null) {
                var widget = new ProgressBar(menu, progressBar.texture(), progressBarIndex);
                layoutPanel.addWidget(progressBar.rect(), widget);
            }

            addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public MenuScreen<ProcessingMenu> createScreen(Inventory inventory, Component title) {
        return new Screen(inventory, title);
    }

    public static <T extends BlockEntity> Menu.Factory<T, ProcessingMenu> factory(Layout layout) {
        return (type, id, inventory, be) -> new ProcessingMenu(type, id, inventory, be, layout);
    }
}
