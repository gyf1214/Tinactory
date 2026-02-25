package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.AutocraftTerminal;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalActionPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalCpuSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalCpuStatusSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalPreviewSyncSlot;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalRequestablesSyncSlot;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftRequestableEntry;
import org.shsts.tinactory.core.autocraft.integration.AutocraftTerminalService;
import org.shsts.tinactory.core.autocraft.model.CraftAmount;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.UUID;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.core.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalMenu extends MenuBase {
    public static final String REQUESTABLES_SYNC = "autocraftTerminalRequestables";
    public static final String CPU_SYNC = "autocraftTerminalCpus";
    public static final String CPU_STATUS_SYNC = "autocraftTerminalCpuStatuses";
    public static final String PREVIEW_SYNC = "autocraftTerminalPreview";

    private List<AutocraftRequestableEntry> requestables = List.of();
    private List<UUID> availableCpus = List.of();
    private List<AutocraftTerminalCpuStatusSyncSlot.Row> cpuStatuses = List.of();
    @Nullable
    private UUID previewPlanId;
    private List<CraftAmount> previewOutputs = List.of();
    @Nullable
    private AutocraftPreviewErrorCode previewError;
    @Nullable
    private AutocraftExecuteErrorCode executeError;
    private final IMachine machine;
    @Nullable
    private final AutocraftTerminalService service;

    private final ActiveScheduler<AutocraftTerminalRequestablesSyncSlot> requestablesScheduler;
    private final ActiveScheduler<AutocraftTerminalCpuSyncSlot> cpuScheduler;
    private final ActiveScheduler<AutocraftTerminalPreviewSyncSlot> previewScheduler;

    public AutocraftTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), AutocraftTerminal.ID, AutocraftTerminal.class);
        this.service = world.isClientSide ? null : terminal.service();

        this.requestablesScheduler = new ActiveScheduler<>(
            () -> new AutocraftTerminalRequestablesSyncSlot(requestables));
        this.cpuScheduler = new ActiveScheduler<>(() -> new AutocraftTerminalCpuSyncSlot(availableCpus));
        this.previewScheduler = new ActiveScheduler<>(() -> new AutocraftTerminalPreviewSyncSlot(
            previewPlanId, previewOutputs, previewError, executeError));

        addSyncSlot(REQUESTABLES_SYNC, requestablesScheduler);
        addSyncSlot(CPU_SYNC, cpuScheduler);
        addSyncSlot(CPU_STATUS_SYNC, () -> new AutocraftTerminalCpuStatusSyncSlot(service == null ? List.of() :
            service.listCpuStatuses().stream()
                .map(status -> new AutocraftTerminalCpuStatusSyncSlot.Row(
                    status.cpuId(),
                    status.available(),
                    status.targetSummary(),
                    status.currentStep(),
                    status.blockedReason(),
                    status.cancellable()))
                .toList()));
        addSyncSlot(PREVIEW_SYNC, previewScheduler);
        onEventPacket(AUTOCRAFT_TERMINAL_ACTION, this::onAction);
        refreshCatalog();
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    private void refreshCatalog() {
        if (service == null) {
            requestables = List.of();
            availableCpus = List.of();
            cpuStatuses = List.of();
        } else {
            requestables = service.listRequestables();
            availableCpus = service.listAvailableCpus();
            cpuStatuses = service.listCpuStatuses().stream()
                .map(status -> new AutocraftTerminalCpuStatusSyncSlot.Row(
                    status.cpuId(),
                    status.available(),
                    status.targetSummary(),
                    status.currentStep(),
                    status.blockedReason(),
                    status.cancellable()))
                .toList();
        }
        requestablesScheduler.invokeUpdate();
        cpuScheduler.invokeUpdate();
    }

    private void setPreviewState(
        @Nullable UUID planId,
        List<CraftAmount> outputs,
        @Nullable AutocraftPreviewErrorCode previewError,
        @Nullable AutocraftExecuteErrorCode executeError) {

        this.previewPlanId = planId;
        this.previewOutputs = List.copyOf(outputs);
        this.previewError = previewError;
        this.executeError = executeError;
        previewScheduler.invokeUpdate();
    }

    private void onAction(AutocraftTerminalActionPacket packet) {
        if (service == null) {
            return;
        }
        if (packet.action() == AutocraftTerminalActionPacket.Action.PREVIEW &&
            packet.target() != null && packet.cpuId() != null) {
            var result = service.preview(new AutocraftPreviewRequest(
                packet.target(), packet.quantity(), packet.cpuId()));
            setPreviewState(result.planId(), result.summaryOutputs(), result.errorCode(), null);
            return;
        }
        if (packet.action() == AutocraftTerminalActionPacket.Action.EXECUTE &&
            packet.planId() != null && packet.cpuId() != null) {
            var result = service.execute(new AutocraftExecuteRequest(packet.planId(), packet.cpuId()));
            setPreviewState(null, List.of(), null, result.errorCode());
            return;
        }
        if (packet.action() == AutocraftTerminalActionPacket.Action.CANCEL && packet.planId() != null) {
            service.cancelPreview(packet.planId());
            setPreviewState(null, List.of(), null, null);
            return;
        }
        if (packet.action() == AutocraftTerminalActionPacket.Action.CANCEL_CPU && packet.cpuId() != null) {
            service.cancelCpu(packet.cpuId());
            refreshCatalog();
        }
    }
}
