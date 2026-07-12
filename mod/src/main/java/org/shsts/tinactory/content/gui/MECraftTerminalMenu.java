package org.shsts.tinactory.content.gui;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.MECraftTerminal;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.MECraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftEventPacket;
import org.shsts.tinactory.content.gui.sync.MECraftPreviewSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftRequestSyncPacket;
import org.shsts.tinactory.content.gui.sync.RevisionScheduler;
import org.shsts.tinactory.core.autocraft.api.ICpuRuntime;
import org.shsts.tinactory.core.autocraft.service.AutocraftTerminalService;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.ME_CRAFT_ACTION;
import static org.shsts.tinactory.AllMenus.ME_CRAFT_CPU_SYNC;
import static org.shsts.tinactory.AllMenus.ME_CRAFT_PREVIEW_SYNC;
import static org.shsts.tinactory.AllMenus.ME_CRAFT_REQUEST_SYNC;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getContainer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftTerminalMenu extends MenuBase {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String REQUEST_SYNC = "request";
    public static final String CPU_STATUS_SYNC = "cpuStatus";
    public static final String PREVIEW_SYNC = "preview";

    private final IMachine machine;
    @Nullable
    private final AutocraftTerminalService service;
    @Nullable
    private final ICpuRuntime cpuRuntime;
    private final ActiveScheduler<MECraftPreviewSyncPacket> previewScheduler;

    public MECraftTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getContainer(blockEntity(), MECraftTerminal.ID, MECraftTerminal.class);
        this.service = world.isClientSide ? null : terminal.createService();
        this.cpuRuntime = world.isClientSide ? null : terminal.cpuRuntime();
        this.previewScheduler = new ActiveScheduler<>(ME_CRAFT_PREVIEW_SYNC, this::previewPacket);

        addSyncSlot(REQUEST_SYNC, new RevisionScheduler<>(ME_CRAFT_REQUEST_SYNC, this::requestRevision,
            this::requestPacket));
        addSyncSlot(CPU_STATUS_SYNC, ME_CRAFT_CPU_SYNC, this::cpuStatusPacket);
        addSyncSlot(PREVIEW_SYNC, previewScheduler);
        onEventPacket(ME_CRAFT_ACTION, this::onAction);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    private MECraftRequestSyncPacket requestPacket() {
        return new MECraftRequestSyncPacket(service == null ? List.of() : service.listRequestables());
    }

    private long requestRevision() {
        return service == null ? 0L : service.requestablesRevision();
    }

    private MECraftCpuSyncPacket cpuStatusPacket() {
        if (service == null || cpuRuntime == null) {
            return new MECraftCpuSyncPacket(List.of());
        }
        var entries = new ArrayList<MECraftCpuSyncPacket.CpuInfo>();
        for (var status : service.listCpuStatuses()) {
            var cpuMachine = cpuRuntime.findVisibleCpuMachine(status.cpuId());
            if (cpuMachine.isEmpty()) {
                LOGGER.warn("{}: Missing autocraft CPU machine for {}", this, status.cpuId());
                continue;
            }
            var machine1 = cpuMachine.get();
            entries.add(new MECraftCpuSyncPacket.CpuInfo(status, machine1.title(), machine1.icon()));
        }
        return new MECraftCpuSyncPacket(entries);
    }

    private MECraftPreviewSyncPacket previewPacket() {
        return service == null ?
            MECraftPreviewSyncPacket.empty() :
            MECraftPreviewSyncPacket.of(service.previewResult());
    }

    private void onAction(MECraftEventPacket packet) {
        if (service == null) {
            return;
        }
        if (packet.action() == MECraftEventPacket.Action.PREVIEW && packet.target() != null) {
            service.preview(packet.target(), packet.quantity());
            previewScheduler.invokeUpdate();
            return;
        }
        if (packet.action() == MECraftEventPacket.Action.EXECUTE && packet.cpuId() != null) {
            if (service.execute(packet.cpuId())) {
                previewScheduler.invokeUpdate();
            }
            return;
        }
        if (packet.action() == MECraftEventPacket.Action.CANCEL && packet.cpuId() != null) {
            service.cancelCpu(packet.cpuId());
        }
    }
}
