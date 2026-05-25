package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.MEPatternTerminal;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternResultSyncPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.content.gui.sync.RevisionScheduler;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalMenu extends MenuBase {
    public static final String PATTERN_SYNC = "mePattern";
    public static final String PATTERN_RESULT_SYNC = "mePatternResult";

    private final IMachine machine;
    @Nullable
    private final IPatternRepository repository;
    private final ActiveScheduler<MEPatternResultSyncPacket> resultScheduler;
    private MEPatternResultSyncPacket.ResultCode lastResult = MEPatternResultSyncPacket.ResultCode.SUCCESS;
    @Nullable
    private String lastResultPatternId;

    public MEPatternTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), MEPatternTerminal.ID, MEPatternTerminal.class);
        this.repository = world.isClientSide ? null : terminal.patternRepository();
        this.resultScheduler = new ActiveScheduler<>(this::resultPacket);

        addSyncSlot(PATTERN_SYNC, new RevisionScheduler<>(this::patternRevision, this::patternPacket));
        addSyncSlot(PATTERN_RESULT_SYNC, resultScheduler);
        onEventPacket(ME_PATTERN_ACTION, this::onAction);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    private long patternRevision() {
        return repository == null ? 0L : repository.revision();
    }

    private MEPatternSyncPacket patternPacket() {
        return new MEPatternSyncPacket(repository == null ? List.of() : repository.listPatterns());
    }

    private MEPatternResultSyncPacket resultPacket() {
        return new MEPatternResultSyncPacket(lastResult, lastResultPatternId);
    }

    private void onAction(MEPatternEventPacket packet) {
        publishResult(handleAction(packet), packet.patternId());
    }

    private MEPatternResultSyncPacket.ResultCode handleAction(MEPatternEventPacket packet) {
        if (repository == null || packet.patternId().isBlank()) {
            return MEPatternResultSyncPacket.ResultCode.INVALID_PATTERN;
        }
        return switch (packet.action()) {
            case CREATE -> createPattern(packet.patternId(), packet.pattern());
            case UPDATE -> updatePattern(packet.patternId(), packet.pattern());
            case DELETE -> deletePattern(packet.patternId(), packet.pattern());
        };
    }

    private MEPatternResultSyncPacket.ResultCode createPattern(String patternId, @Nullable CraftPattern pattern) {
        if (!hasValidPayload(pattern) || !patternId.equals(pattern.patternId())) {
            return MEPatternResultSyncPacket.ResultCode.INVALID_PATTERN;
        }
        if (repository.containsPatternId(patternId)) {
            return MEPatternResultSyncPacket.ResultCode.DUPLICATE_PATTERN_ID;
        }
        return repository.addPattern(pattern) ?
            MEPatternResultSyncPacket.ResultCode.SUCCESS :
            MEPatternResultSyncPacket.ResultCode.NO_CAPACITY;
    }

    private MEPatternResultSyncPacket.ResultCode updatePattern(String patternId, @Nullable CraftPattern pattern) {
        if (!hasValidPayload(pattern)) {
            return MEPatternResultSyncPacket.ResultCode.INVALID_PATTERN;
        }
        if (!repository.containsPatternId(patternId)) {
            return MEPatternResultSyncPacket.ResultCode.PATTERN_NOT_FOUND;
        }
        if (!patternId.equals(pattern.patternId())) {
            return MEPatternResultSyncPacket.ResultCode.STALE_PATTERN;
        }
        return repository.updatePattern(pattern) ?
            MEPatternResultSyncPacket.ResultCode.SUCCESS :
            MEPatternResultSyncPacket.ResultCode.NO_CAPACITY;
    }

    private MEPatternResultSyncPacket.ResultCode deletePattern(String patternId, @Nullable CraftPattern pattern) {
        if (pattern != null) {
            return MEPatternResultSyncPacket.ResultCode.INVALID_PATTERN;
        }
        return repository.removePattern(patternId) ?
            MEPatternResultSyncPacket.ResultCode.SUCCESS :
            MEPatternResultSyncPacket.ResultCode.PATTERN_NOT_FOUND;
    }

    private boolean hasValidPayload(@Nullable CraftPattern pattern) {
        return pattern != null && !pattern.outputs().isEmpty();
    }

    private void publishResult(MEPatternResultSyncPacket.ResultCode result, String patternId) {
        lastResult = result;
        lastResultPatternId = patternId.isBlank() ? null : patternId;
        resultScheduler.invokeUpdate();
    }
}
