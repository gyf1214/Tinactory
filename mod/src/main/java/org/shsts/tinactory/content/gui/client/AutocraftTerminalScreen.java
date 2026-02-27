package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.AutocraftTerminalMenu;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftEventPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftRequestablesSyncPacket;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Tab;

import java.util.List;
import java.util.UUID;

import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.CPU_STATUS_SYNC;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.PREVIEW_SYNC;
import static org.shsts.tinactory.content.gui.AutocraftTerminalMenu.REQUESTABLES_SYNC;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalScreen extends MenuScreen<AutocraftTerminalMenu> {
    private final AutocraftRequestPanel requestPanel;
    private final AutocraftCpuStatusPanel cpuStatusPanel;
    private final AutocraftPreviewPanel previewPanel;
    private final Tab tabs;

    private List<CraftKey> requestables = List.of();
    private List<UUID> availableCpus = List.of();
    private List<AutocraftCpuSyncPacket.Row> cpuStatuses = List.of();
    @Nullable
    private UUID previewPlanId;

    public AutocraftTerminalScreen(AutocraftTerminalMenu menu, Component title) {
        super(menu, title);
        this.requestPanel = new AutocraftRequestPanel(this);
        this.cpuStatusPanel = new AutocraftCpuStatusPanel(this);
        this.previewPanel = new AutocraftPreviewPanel(this);
        this.tabs = new Tab(this, requestPanel, Items.WRITABLE_BOOK, cpuStatusPanel, Items.COMPARATOR);

        addPanel(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, 72), requestPanel);
        addPanel(RectD.corners(0d, 0d, 1d, 0d), Rect.corners(0, 0, 0, 72), cpuStatusPanel);
        addPanel(new Rect(0, 0, 0, 0), tabs);
        addPanel(RectD.corners(0d, 0d, 1d, 1d), Rect.corners(0, 56, 0, -88), previewPanel);
        this.contentHeight = 180;

        menu.onSyncPacket(REQUESTABLES_SYNC, this::onRequestablesSync);
        menu.onSyncPacket(CPU_STATUS_SYNC, this::onCpuStatusSync);
        menu.onSyncPacket(PREVIEW_SYNC, this::onPreviewSync);
        tabs.select(0);
        refreshRequestTitle();
    }

    public void requestPreview() {
        var targetIndex = requestPanel.targetIndex(requestables.size());
        var cpuIndex = requestPanel.cpuIndex(availableCpus.size());
        var quantity = requestPanel.quantity();
        if (targetIndex.isEmpty() || cpuIndex.isEmpty() || quantity.isEmpty()) {
            return;
        }
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, () -> AutocraftEventPacket.preview(
            requestables.get(targetIndex.getAsInt()),
            quantity.getAsLong(),
            availableCpus.get(cpuIndex.getAsInt())));
    }

    public void executePreview() {
        var cpuIndex = requestPanel.cpuIndex(availableCpus.size());
        if (previewPlanId == null || cpuIndex.isEmpty()) {
            return;
        }
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION,
            () -> AutocraftEventPacket.execute(availableCpus.get(cpuIndex.getAsInt())));
    }

    public void cancelPreview() {
        if (previewPlanId == null) {
            return;
        }
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, AutocraftEventPacket::cancel);
    }

    public void cancelCpuJob() {
        var index = cpuStatusPanel.selectedIndex(cpuStatuses.size());
        if (index.isEmpty()) {
            return;
        }
        var row = cpuStatuses.get(index.getAsInt());
        if (!row.cancellable()) {
            return;
        }
        menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, () -> AutocraftEventPacket.cancelCpu(row.cpuId()));
    }

    private void onRequestablesSync(AutocraftRequestablesSyncPacket packet) {
        requestables = packet.requestables();
        refreshRequestTitle();
    }

    private void onCpuStatusSync(AutocraftCpuSyncPacket packet) {
        cpuStatuses = packet.rows();
        availableCpus = cpuStatuses.stream()
            .filter(AutocraftCpuSyncPacket.Row::available)
            .map(AutocraftCpuSyncPacket.Row::cpuId)
            .toList();
        refreshRequestTitle();
        cpuStatusPanel.refreshSummary(cpuStatuses);
    }

    private void onPreviewSync(AutocraftPreviewSyncPacket packet) {
        if (packet.previewError() != null) {
            previewPanel.setSummary(new TextComponent("Preview error: " + packet.previewError().name()));
            return;
        }
        if (packet.executeError() != null) {
            previewPanel.setSummary(new TextComponent("Execute error: " + packet.executeError().name()));
            return;
        }
        if (packet.targets() != null) {
            previewPanel.setSummary(new TextComponent("Plan outputs: " + packet.targets().size()));
        }
    }

    private void refreshRequestTitle() {
        requestPanel.setTitle(new TextComponent(
            "Requestables: " + requestables.size() + ", CPUs: " + availableCpus.size()));
        requestPanel.updateSelectionSummary(requestables, availableCpus);
    }
}
