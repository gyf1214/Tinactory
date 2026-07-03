package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.MECraftTerminalMenu;
import org.shsts.tinactory.content.gui.sync.MECraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftEventPacket;
import org.shsts.tinactory.content.gui.sync.MECraftPreviewSyncPacket;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Tab;

import java.util.UUID;
import java.util.function.Predicate;

import static org.shsts.tinactory.AllMenus.ME_CRAFT_ACTION;
import static org.shsts.tinactory.content.gui.MECraftTerminalMenu.CPU_STATUS_SYNC;
import static org.shsts.tinactory.content.gui.MECraftTerminalMenu.PREVIEW_SYNC;
import static org.shsts.tinactory.content.gui.MECraftTerminalMenu.REQUEST_SYNC;
import static org.shsts.tinactory.integration.gui.client.Tab.TAB_OFFSET;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftTerminalScreen extends MenuScreen<MECraftTerminalMenu> {
    public static final int BUTTON_WIDTH = 64;
    private static final int HEIGHT = 153;

    private final Tab tab;
    private final MECraftCpuStatusPanel cpuStatusPanel;
    private final MECraftPreviewPanel previewPanel;

    public MECraftTerminalScreen(MECraftTerminalMenu menu, Component title) {
        super(menu, title);
        var requestPanel = new MECraftRequestPanel(this);
        this.cpuStatusPanel = new MECraftCpuStatusPanel(this);
        this.previewPanel = new MECraftPreviewPanel(this);
        this.tab = new Tab(this, requestPanel, Items.WRITABLE_BOOK, cpuStatusPanel, Items.COMPARATOR);

        rootPanel.addGroup(TAB_OFFSET, tab);
        rootPanel.addGroup(requestPanel);
        rootPanel.addGroup(cpuStatusPanel);
        rootPanel.addGroup(previewPanel);
        this.contentHeight = HEIGHT;

        menu.onSyncPacket(REQUEST_SYNC, requestPanel::updateRequestables);
        menu.onSyncPacket(CPU_STATUS_SYNC, cpuStatusPanel::updateStatus);
        menu.onSyncPacket(PREVIEW_SYNC, this::onPreviewSync);

        tab.onSelect(this::onTabChange);
        previewPanel.setActive(false);
        tab.select(0);
    }

    public static Component tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.autocraft." + key, args);
    }

    private void onTabChange(int i) {
        previewPanel.setActive(i < 0);
        cpuStatusPanel.onSelectCpu(null);
    }

    public void selectCpu(Predicate<MECraftCpuSyncPacket.CpuInfo> cb) {
        tab.select(1);
        cpuStatusPanel.onSelectCpu(cpu -> {
            if (cb.test(cpu)) {
                tab.select(-1);
            }
        });
    }

    public void executePreview(UUID cpu) {
        var packet = MECraftEventPacket.execute(cpu);
        menu.triggerEvent(ME_CRAFT_ACTION, () -> packet);
        tab.select(1);
    }

    public void cancelPreview() {
        tab.select(0);
    }

    private void onPreviewSync(MECraftPreviewSyncPacket packet) {
        if (packet.state() == MECraftPreviewSyncPacket.PreviewState.EMPTY) {
            return;
        }
        previewPanel.onPreviewSync(packet);
        tab.select(-1);
    }
}
