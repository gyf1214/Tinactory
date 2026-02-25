package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.AutocraftTerminal;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalActionPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalCpuStatusSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalRequestablesSyncPacket;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftExecuteErrorCode;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewRequest;
import org.shsts.tinactory.core.autocraft.integration.AutocraftPreviewErrorCode;
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
    public static final String CPU_STATUS_SYNC = "autocraftTerminalCpuStatuses";
    public static final String PREVIEW_SYNC = "autocraftTerminalPreview";

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

    private final ActiveScheduler<AutocraftTerminalPreviewSyncPacket> previewScheduler;

    public AutocraftTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), AutocraftTerminal.ID, AutocraftTerminal.class);
        this.service = world.isClientSide ? null : terminal.service();
        this.previewScheduler = new ActiveScheduler<>(() -> new AutocraftTerminalPreviewSyncPacket(
            previewPlanId, previewOutputs, previewError, executeError));

        addSyncSlot(REQUESTABLES_SYNC, this::requestablesPacket);
        addSyncSlot(CPU_STATUS_SYNC, this::cpuStatusPacket);
        addSyncSlot(PREVIEW_SYNC, previewScheduler);
        onEventPacket(AUTOCRAFT_TERMINAL_ACTION, this::onAction);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    private AutocraftTerminalRequestablesSyncPacket requestablesPacket() {
        return new AutocraftTerminalRequestablesSyncPacket(service == null ? List.of() : service.listRequestables());
    }

    private AutocraftTerminalCpuStatusSyncPacket cpuStatusPacket() {
        if (service == null) {
            return new AutocraftTerminalCpuStatusSyncPacket(List.of());
        }
        return new AutocraftTerminalCpuStatusSyncPacket(service.listCpuStatuses().stream().map(
            status -> new AutocraftTerminalCpuStatusSyncPacket.Row(
                status.cpuId(),
                status.available(),
                status.targetSummary(),
                status.currentStep(),
                status.blockedReason(),
                status.cancellable())).toList());
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
        }
    }
}
