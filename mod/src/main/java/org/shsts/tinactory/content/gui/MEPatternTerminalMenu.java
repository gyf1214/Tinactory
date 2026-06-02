package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.MEPatternTerminal;
import org.shsts.tinactory.content.gui.client.MEPatternDraft;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.content.gui.sync.RevisionScheduler;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.integration.gui.InventoryMenu;

import java.util.List;
import java.util.UUID;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalMenu extends InventoryMenu {
    public static final String PATTERN_SYNC = "mePattern";
    public static final String PATTERN_RESULT_SYNC = "mePatternResult";
    public static final int PANEL_HEIGHT = 116;

    private final IMachine machine;
    @Nullable
    private final IPatternRepository repository;
    private final ActiveScheduler<SyncPackets.UnitPacket> resultScheduler;
    @Nullable
    private IRecipeDraftImporter recipeDraftImporter = null;

    public MEPatternTerminalMenu(Properties properties) {
        super(properties, PANEL_HEIGHT);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), MEPatternTerminal.ID, MEPatternTerminal.class);
        this.repository = world.isClientSide ? null : terminal.patternRepository();
        this.resultScheduler = new ActiveScheduler<>(() -> SyncPackets.UnitPacket.INSTANCE);

        addSyncSlot(PATTERN_SYNC, new RevisionScheduler<>(this::patternRevision, this::patternPacket));
        addSyncSlot(PATTERN_RESULT_SYNC, resultScheduler);
        onEventPacket(ME_PATTERN_ACTION, this::onAction);
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    public void setRecipeDraftImporter(@Nullable IRecipeDraftImporter importer) {
        recipeDraftImporter = importer;
    }

    public boolean importRecipeDraft(MEPatternDraft draft, boolean doImport) {
        return recipeDraftImporter != null && recipeDraftImporter.importRecipeDraft(draft, doImport);
    }

    private long patternRevision() {
        return repository == null ? 0L : repository.revision();
    }

    private MEPatternSyncPacket patternPacket() {
        return new MEPatternSyncPacket(repository == null ? List.of() : repository.listPatterns());
    }

    private void onAction(MEPatternEventPacket packet) {
        if (handleAction(packet)) {
            resultScheduler.invokeUpdate();
        }
    }

    private boolean handleAction(MEPatternEventPacket packet) {
        if (repository == null) {
            return false;
        }
        return switch (packet.action()) {
            case CREATE -> createPattern(packet.patternUuid(), packet.pattern());
            case UPDATE -> updatePattern(packet.patternUuid(), packet.pattern());
            case DELETE -> deletePattern(packet.patternUuid(), packet.pattern());
        };
    }

    private boolean createPattern(@Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        if (repository == null || patternUuid != null || !hasValidPayload(pattern)) {
            return false;
        }
        return repository.addPattern(pattern.withUuid(UUID.randomUUID()));
    }

    private boolean updatePattern(@Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        if (repository == null || patternUuid == null || !hasValidPayload(pattern)) {
            return false;
        }
        if (!repository.containsPatternUuid(patternUuid)) {
            return false;
        }
        return repository.updatePattern(pattern.withUuid(patternUuid));
    }

    private boolean deletePattern(@Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        if (repository == null || patternUuid == null || pattern != null) {
            return false;
        }
        return repository.removePattern(patternUuid);
    }

    private static boolean hasValidPayload(@Nullable CraftPattern pattern) {
        return pattern != null && !pattern.outputs().isEmpty();
    }

    @OnlyIn(Dist.CLIENT)
    @FunctionalInterface
    public interface IRecipeDraftImporter {
        boolean importRecipeDraft(MEPatternDraft draft, boolean doImport);
    }
}
