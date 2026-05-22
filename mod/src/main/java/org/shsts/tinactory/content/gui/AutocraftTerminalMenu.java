package org.shsts.tinactory.content.gui;

import com.mojang.logging.LogUtils;
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
import org.shsts.tinactory.content.gui.sync.RevisionScheduler;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftTerminalMenu extends MenuBase {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String REQUESTABLES_SYNC = "requestables";
    public static final String CPU_STATUS_SYNC = "cpuStatus";
    public static final String PREVIEW_SYNC = "preview";

    private final IMachine machine;
    @Nullable
    private final AutocraftTerminalService service;
    @Nullable
    private final ICpuRuntime cpuRuntime;
    private final RevisionScheduler<AutocraftRequestablesSyncPacket> requestablesScheduler;
    private final ActiveScheduler<AutocraftPreviewSyncPacket> previewScheduler;

    public AutocraftTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), AutocraftTerminal.ID, AutocraftTerminal.class);
        this.service = world.isClientSide ? null : terminal.createService();
        this.cpuRuntime = world.isClientSide ? null : terminal.cpuRuntime();
        this.requestablesScheduler = new RevisionScheduler<>(
            this::requestablesRevision,
            this::requestablesPacket);
        this.previewScheduler = new ActiveScheduler<>(this::previewPacket);

        addSyncSlot(REQUESTABLES_SYNC, requestablesScheduler);
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

    private long requestablesRevision() {
        return service == null ? 0L : service.requestablesRevision();
    }

    private AutocraftCpuSyncPacket cpuStatusPacket() {
        if (service == null || cpuRuntime == null) {
            return new AutocraftCpuSyncPacket(List.of());
        }
        var entries = new ArrayList<AutocraftCpuSyncPacket.CpuInfo>();
        for (var status : service.listCpuStatuses()) {
            var cpuMachine = cpuRuntime.findVisibleCpuMachine(status.cpuId());
            if (cpuMachine.isEmpty()) {
                LOGGER.warn("{}: Missing autocraft CPU machine for {}", this, status.cpuId());
                continue;
            }
            var machine1 = cpuMachine.get();
            entries.add(new AutocraftCpuSyncPacket.CpuInfo(status, machine1.title(), machine1.icon()));
        }
        return new AutocraftCpuSyncPacket(entries);
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
        if (packet.action() == AutocraftEventPacket.Action.PREVIEW && packet.target() != null) {
            service.preview(packet.target(), packet.quantity());
            previewScheduler.invokeUpdate();
            return;
        }
        if (packet.action() == AutocraftEventPacket.Action.EXECUTE && packet.cpuId() != null) {
            if (service.execute(packet.cpuId()).isSuccess()) {
                previewScheduler.invokeUpdate();
            }
            return;
        }
        if (packet.action() == AutocraftEventPacket.Action.CANCEL && packet.cpuId() != null) {
            service.cancelCpu(packet.cpuId());
        }
    }
}
