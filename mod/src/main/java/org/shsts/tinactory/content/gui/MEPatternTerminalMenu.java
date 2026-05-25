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
        return new MEPatternResultSyncPacket(lastResult);
    }

    private void onAction(MEPatternEventPacket packet) {
        lastResult = MEPatternResultSyncPacket.ResultCode.INVALID_PATTERN;
        resultScheduler.invokeUpdate();
    }
}
