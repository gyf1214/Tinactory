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
public class ProcessingMenu<T extends BlockEntity> extends Menu<T> {
    private final Layout layout;
    private final Map<Layout.SlotInfo, Integer> fluidSyncIndex = new HashMap<>();
    private final int progressBarIndex;

    public ProcessingMenu(SmartMenuType<T, ?> type, int id, Inventory inventory, T blockEntity,
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
            this.progressBarIndex = addSyncSlot(MenuSyncPacket.Double.class, (containerId, index, be) -> {
                var progress = be.getCapability(AllCapabilities.PROCESSOR.get())
                        .map(IProcessor::getProgress)
                        .orElse(0.0d);
                return new MenuSyncPacket.Double(containerId, index, progress);
            });
        } else {
            this.progressBarIndex = -1;
        }
        this.height = layout.rect.endY();
    }

    @Override
    public boolean stillValid(Player player) {
        return Machine.get(blockEntity).canPlayerInteract(player) && super.stillValid(player);
    }

    @OnlyIn(Dist.CLIENT)
    private class Screen extends MenuScreen<ProcessingMenu<T>> {
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

            rootPanel.addPanel(new Rect(layout.getXOffset(), 0, 0, 0), layoutPanel);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SuppressWarnings("unchecked")
    @Override
    public <M1 extends Menu<?>> MenuScreen<M1> createScreen(Inventory inventory, Component title) {
        return (MenuScreen<M1>) new Screen(inventory, title);
    }

    public static <T extends BlockEntity> Menu.Factory<T, ProcessingMenu<T>>
    factory(Layout layout) {
        return (type, id, inventory, be) -> new ProcessingMenu<>(type, id, inventory, be, layout);
    }
}
