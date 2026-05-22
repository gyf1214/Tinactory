package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.AutocraftTerminalMenu;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftEventPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Tab;

import java.util.UUID;
import java.util.function.Predicate;

import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.CPU_STATUS_SYNC;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.PREVIEW_SYNC;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.REQUESTABLES_SYNC;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalScreen extends MenuScreen<AutocraftTerminalMenu> {
    public static final int BUTTON_WIDTH = 64;

    private final Tab tab;
    private final AutocraftCpuStatusPanel cpuStatusPanel;
    private final AutocraftPreviewPanel previewPanel;

    public AutocraftTerminalScreen(AutocraftTerminalMenu menu, Component title) {
        super(menu, title);
        var requestPanel = new AutocraftRequestPanel(this);
        this.cpuStatusPanel = new AutocraftCpuStatusPanel(this);
        this.previewPanel = new AutocraftPreviewPanel(this);
        this.tab = new Tab(this, requestPanel, Items.WRITABLE_BOOK, cpuStatusPanel, Items.COMPARATOR);

        rootPanel.addGroup(new Rect(-MARGIN_X, -MARGIN_TOP, 0, 0), tab);
        rootPanel.addGroup(requestPanel);
        rootPanel.addGroup(cpuStatusPanel);
        rootPanel.addGroup(previewPanel);
        this.contentHeight = 144;

        menu.onSyncPacket(REQUESTABLES_SYNC, requestPanel::updateRequestables);
        menu.onSyncPacket(CPU_STATUS_SYNC, cpuStatusPanel::updateStatus);
        menu.onSyncPacket(PREVIEW_SYNC, this::onPreviewSync);

        tab.onSelect(this::onTabChange);
        previewPanel.setActive(false);
        tab.select(0);
    }

    public static TranslatableComponent tr(String key, Object... args) {
        return I18n.tr("tinactory.gui.autocraft." + key, args);
    }

    private void onTabChange(int i) {
        previewPanel.setActive(i < 0);
        cpuStatusPanel.onSelectCpu(null);
    }

    public void selectCpu(Predicate<AutocraftCpuSyncPacket.CpuInfo> cb) {
        tab.select(1);
        cpuStatusPanel.onSelectCpu(cpu -> {
            if (cb.test(cpu)) {
                tab.select(-1);
            }
        });
    }

    public void executePreview(UUID cpu) {
        var packet = AutocraftEventPacket.execute(cpu);
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, () -> packet);
        tab.select(1);
    }

    public void cancelPreview() {
        tab.select(0);
    }

    private void onPreviewSync(AutocraftPreviewSyncPacket packet) {
        if (packet.state() == AutocraftPreviewSyncPacket.PreviewState.EMPTY) {
            return;
        }
        previewPanel.onPreviewSync(packet);
        tab.select(-1);
    }
}
