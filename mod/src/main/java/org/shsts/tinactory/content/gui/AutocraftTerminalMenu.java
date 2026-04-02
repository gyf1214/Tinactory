package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.AutocraftTerminal;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftEventPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftRequestablesSyncPacket;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.core.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalMenu extends MenuBase {
    public static final String REQUESTABLES_SYNC = "autocraftTerminalRequestables";
    public static final String CPU_STATUS_SYNC = "autocraftTerminalCpuStatuses";
    public static final String PREVIEW_SYNC = "autocraftTerminalPreview";

    private final IMachine machine;
    @Nullable
    private final AutocraftTerminalService service;
    private final ActiveScheduler<AutocraftPreviewSyncPacket> previewScheduler;

    public AutocraftTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), AutocraftTerminal.ID, AutocraftTerminal.class);
        this.service = world.isClientSide ? null : terminal.service();
        this.previewScheduler = new ActiveScheduler<>(this::previewPacket);

        addSyncSlot(REQUESTABLES_SYNC, this::requestablesPacket);
        addSyncSlot(CPU_STATUS_SYNC, this::cpuStatusPacket);
        addSyncSlot(PREVIEW_SYNC, previewScheduler);
        onEventPacket(AUTOCRAFT_TERMINAL_ACTION, this::onAction);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    private AutocraftRequestablesSyncPacket requestablesPacket() {
        return new AutocraftRequestablesSyncPacket(service == null ? List.of() : service.listRequestables());
    }

    private AutocraftCpuSyncPacket cpuStatusPacket() {
        if (service == null) {
            return new AutocraftCpuSyncPacket(List.of());
        }
        return new AutocraftCpuSyncPacket(service.listCpuStatuses());
    }

    private AutocraftPreviewSyncPacket previewPacket() {
        return service == null ?
            AutocraftPreviewSyncPacket.empty() :
            AutocraftPreviewSyncPacket.of(service.previewResult());
    }

    private void onAction(AutocraftEventPacket packet) {
        if (service == null) {
            return;
        }
        if (packet.action() == AutocraftEventPacket.Action.PREVIEW &&
            packet.target() != null) {
            service.preview(packet.target(), packet.quantity());
            previewScheduler.invokeUpdate();
            return;
        }
        if (packet.action() == AutocraftEventPacket.Action.EXECUTE &&
            packet.cpuId() != null) {
            if (service.execute(packet.cpuId()).isSuccess()) {
                previewScheduler.invokeUpdate();
            }
            return;
        }
        if (packet.action() == AutocraftEventPacket.Action.CANCEL) {
            service.cancelPreview();
            previewScheduler.invokeUpdate();
            return;
        }
        if (packet.action() == AutocraftEventPacket.Action.CANCEL_CPU && packet.cpuId() != null) {
            service.cancelCpu(packet.cpuId());
        }
    }
}
