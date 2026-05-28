package org.shsts.tinactory.content.gui;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.autocraft.MEPatternTerminal;
import org.shsts.tinactory.content.gui.sync.ActiveScheduler;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.content.gui.sync.RevisionScheduler;
import org.shsts.tinactory.core.autocraft.api.IPatternRepository;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.sync.SyncPackets;
import org.shsts.tinactory.core.util.LocHelper;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.UUID;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.common.CapabilityProvider.getProvider;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternTerminalMenu extends MenuBase {
    public static final String PATTERN_SYNC = "mePattern";
    public static final String PATTERN_RESULT_SYNC = "mePatternResult";
    public static final int PANEL_HEIGHT = 148;
    private static final int INVENTORY_Y = PANEL_HEIGHT + SPACING;
    public static final int INVENTORY_BAR_Y = INVENTORY_Y + SLOT_SIZE * 3 + SPACING;

    private final IMachine machine;
    @Nullable
    private final IPatternRepository repository;
    private final ActiveScheduler<SyncPackets.LongPacket> resultScheduler;

    public enum Result {
        SUCCESS,
        PATTERN_NOT_FOUND,
        NO_CAPACITY,
        INVALID_PATTERN;

        public final String id;

        Result() {
            this.id = LocHelper.constantToId(name());
        }
    }

    private Result lastResult = Result.SUCCESS;
    private boolean editorActive = false;

    private class EditorInventorySlot extends Slot {
        public EditorInventorySlot(int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public boolean isActive() {
            return editorActive;
        }
    }

    public MEPatternTerminalMenu(Properties properties) {
        super(properties);
        this.machine = MACHINE.get(blockEntity());
        var terminal = getProvider(blockEntity(), MEPatternTerminal.ID, MEPatternTerminal.class);
        this.repository = world.isClientSide ? null : terminal.patternRepository();
        this.resultScheduler = new ActiveScheduler<>(this::resultPacket);

        addSyncSlot(PATTERN_SYNC, new RevisionScheduler<>(this::patternRevision, this::patternPacket));
        addSyncSlot(PATTERN_RESULT_SYNC, resultScheduler);
        onEventPacket(ME_PATTERN_ACTION, this::onAction);
        addEditorInventorySlots();
    }

    public static Result resultOf(long val) {
        return Result.values()[(int) val];
    }

    @Override
    public boolean stillValid(Player player) {
        return super.stillValid(player) && machine.canPlayerInteract(player);
    }

    public void setEditorActive(boolean value) {
        editorActive = value;
    }

    private void addEditorInventorySlots() {
        for (var j = 0; j < 9; j++) {
            var x = MARGIN_X + j * SLOT_SIZE;
            var y = MARGIN_TOP + INVENTORY_BAR_Y;
            addSlot(new EditorInventorySlot(j, x + 1, y + 1));
        }
        for (var i = 0; i < 3; i++) {
            for (var j = 0; j < 9; j++) {
                var x = MARGIN_X + j * SLOT_SIZE;
                var y = MARGIN_TOP + INVENTORY_Y + i * SLOT_SIZE;
                addSlot(new EditorInventorySlot(9 + i * 9 + j, x + 1, y + 1));
            }
        }
    }

    private long patternRevision() {
        return repository == null ? 0L : repository.revision();
    }

    private MEPatternSyncPacket patternPacket() {
        return new MEPatternSyncPacket(repository == null ? List.of() : repository.listPatterns());
    }

    private SyncPackets.LongPacket resultPacket() {
        return new SyncPackets.LongPacket(lastResult.ordinal());
    }

    private void onAction(MEPatternEventPacket packet) {
        lastResult = handleAction(packet);
        resultScheduler.invokeUpdate();
    }

    private Result handleAction(MEPatternEventPacket packet) {
        if (repository == null) {
            return Result.INVALID_PATTERN;
        }
        return switch (packet.action()) {
            case CREATE -> createPattern(packet.patternUuid(), packet.pattern());
            case UPDATE -> updatePattern(packet.patternUuid(), packet.pattern());
            case DELETE -> deletePattern(packet.patternUuid(), packet.pattern());
        };
    }

    private Result createPattern(@Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        if (repository == null || patternUuid != null || !hasValidPayload(pattern)) {
            return Result.INVALID_PATTERN;
        }
        return repository.addPattern(pattern.withUuid(UUID.randomUUID())) ?
            Result.SUCCESS :
            Result.NO_CAPACITY;
    }

    private Result updatePattern(@Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        if (repository == null || patternUuid == null || !hasValidPayload(pattern)) {
            return Result.INVALID_PATTERN;
        }
        if (!repository.containsPatternUuid(patternUuid)) {
            return Result.PATTERN_NOT_FOUND;
        }
        return repository.updatePattern(pattern.withUuid(patternUuid)) ?
            Result.SUCCESS :
            Result.NO_CAPACITY;
    }

    private Result deletePattern(@Nullable UUID patternUuid, @Nullable CraftPattern pattern) {
        if (repository == null || patternUuid == null || pattern != null) {
            return Result.INVALID_PATTERN;
        }
        return repository.removePattern(patternUuid) ?
            Result.SUCCESS :
            Result.PATTERN_NOT_FOUND;
    }

    private static boolean hasValidPayload(@Nullable CraftPattern pattern) {
        return pattern != null && !pattern.outputs().isEmpty();
    }
}
