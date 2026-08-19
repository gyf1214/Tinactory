package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.sync.StorageEventPacket;
import org.shsts.tinactory.content.gui.sync.StorageSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.logistics.StorageEntry;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.STORAGE_SLOT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StoragePanel extends ButtonPanel {
    private static final Comparator<StorageEntry> DISPLAY_ORDER =
        Comparator.comparing(StorageEntry::key, StackHelper.KEY_DISPLAY_ORDER);

    private final List<StorageEntry> entries = new ArrayList<>();

    public StoragePanel(MenuScreen<?> screen) {
        super(screen, SLOT_SIZE, SLOT_SIZE, 0);
    }

    @Override
    protected int getItemCount() {
        var slotCount = gridViewGroup.getSlotCount();
        return Math.max(1, (entries.size() + slotCount) / slotCount) * slotCount;
    }

    @Override
    protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick,
        Rect rect, int index, boolean isHovering) {
        RenderUtil.blit(graphics, SLOT_BACKGROUND, rect);
        var rect1 = rect.offset(1, 1).enlarge(-2, -2);
        if (index < entries.size()) {
            var entry = entries.get(index);
            if (entry.isFilter() && entry.amount() == 0) {
                RenderUtil.renderGhostDescriptor(graphics, entry.key().display(), rect1);
            } else {
                var display = entry.key().display(entry.amount());
                RenderUtil.renderDescriptorWithDecoration(graphics, display, rect1);
            }
        }
        if (isHovering) {
            RenderUtil.renderSlotHover(graphics, rect1);
        }
    }

    @Override
    protected boolean canClickButton(int index, double mouseX, double mouseY, int button) {
        return button == 0 || button == 1;
    }

    @Override
    protected void playButtonSound() {}

    @Override
    protected void onSelect(int index, double mouseX, double mouseY, int button) {
        if (index < entries.size()) {
            var entry = entries.get(index);
            var isQuickMove = button == 0 && ClientUtil.shiftDown();
            menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(entry.key(), button, isQuickMove));
        } else {
            menu.triggerEvent(STORAGE_SLOT, () -> new StorageEventPacket(button));
        }
    }

    @Override
    protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
        if (index < entries.size()) {
            var entry = entries.get(index);
            return entry.isFilter() && entry.amount() == 0 ? entry.key().tooltip() :
                entry.key().tooltip(entry.amount());
        } else {
            return Optional.empty();
        }
    }

    public void sync(StorageSyncPacket packet) {
        entries.clear();
        entries.addAll(packet.entries());
        entries.sort(DISPLAY_ORDER);
    }
}
