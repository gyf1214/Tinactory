package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.Tab;
import org.shsts.tinactory.integration.gui.client.VanillaButton;

import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Tab.TAB_OFFSET;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternEditorPanel extends Panel {
    private static final int BUTTON_WIDTH = 48;
    private static final Rect PANEL_OFFSET = Rect.corners(0, 0, 0, -BUTTON_HEIGHT - SPACING);

    private final VanillaButton deleteButton;
    private final Tab tab;
    private final MEPatternIngredientPanel inputPanel;
    private final MEPatternIngredientPanel outputPanel;
    private final MEPatternMachinePanel machinePanel;
    private final MEPatternDraft draft = MEPatternDraft.empty();
    @Nullable
    private UUID originalUuid;

    public MEPatternEditorPanel(MEPatternTerminalScreen screen, Runnable onCancel) {
        super(screen);
        this.deleteButton = new VanillaButton(menu, tr("delete"), null, this::delete);
        this.inputPanel = MEPatternIngredientPanel.input(screen, draft.inputRows());
        this.outputPanel = MEPatternIngredientPanel.output(screen, draft.outputRows());
        this.machinePanel = new MEPatternMachinePanel(screen, draft);

        this.tab = new Tab(screen, inputPanel, Items.HOPPER, outputPanel, Items.CHEST, machinePanel, Items.COMPARATOR);

        addChild(RectD.corners(1d, 1d, 1d, 1d), Rect.corners(-BUTTON_WIDTH, -BUTTON_HEIGHT, 0, 0),
            new VanillaButton(menu, tr("save"), null, this::save));
        addChild(RectD.corners(0d, 1d, 0d, 1d), Rect.corners(0, -BUTTON_HEIGHT, BUTTON_WIDTH, 0),
            new VanillaButton(menu, tr("cancel"), null, onCancel));
        addChild(RectD.corners(0.5d, 1d, 0.5d, 1d),
            Rect.corners(-BUTTON_WIDTH / 2, -BUTTON_HEIGHT, BUTTON_WIDTH / 2, 0),
            deleteButton);

        addGroup(PANEL_OFFSET, inputPanel);
        addGroup(PANEL_OFFSET, outputPanel);
        addGroup(PANEL_OFFSET, machinePanel);
        addGroup(TAB_OFFSET, tab);
    }

    public void create(ItemStack stack) {
        reset();
        originalUuid = null;
        MEPatternIngredientDraft.fromItem(stack).ifPresent(draft.outputRows()::add);
        deleteButton.setActive(false);
        postReset(1);
    }

    public void createFromDraft(MEPatternDraft value) {
        reset();
        originalUuid = null;
        draft.copyFrom(value);
        deleteButton.setActive(false);
        postReset(draft.inputRows().isEmpty() ? 1 : 0);
    }

    public void edit(CraftPattern pattern) {
        reset();
        originalUuid = pattern.patternUuid();
        draft.copyFrom(MEPatternDraft.fromPattern(pattern));
        deleteButton.setActive(true);
        postReset(0);
    }

    private void reset() {
        draft.copyFrom(MEPatternDraft.empty());
    }

    private void postReset(int index) {
        inputPanel.resetPage();
        outputPanel.resetPage();
        machinePanel.refresh();
        tab.select(index);
    }

    private void save() {
        toPattern().ifPresent(pattern -> {
            if (originalUuid == null) {
                menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.create(pattern));
            } else {
                var target = originalUuid;
                menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.update(target, pattern));
            }
        });
    }

    private void delete() {
        if (originalUuid != null) {
            var target = originalUuid;
            menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.delete(target));
        }
    }

    private Optional<CraftPattern> toPattern() {
        return draft.toPattern();
    }
}
