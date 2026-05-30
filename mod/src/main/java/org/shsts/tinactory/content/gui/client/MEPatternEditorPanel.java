package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.Tab;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.gui.client.Widgets;

import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Tab.TAB_OFFSET;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternEditorPanel extends Panel {
    private static final int BUTTON_WIDTH = 48;
    private static final int LABEL_LEFT = 64;
    private static final Rect LABEL_OFFSET = new Rect(0, 0, LABEL_LEFT, EDIT_HEIGHT);
    private static final RectD EDIT_ANCHOR = RectD.corners(0d, 0d, 1d, 0d);
    private static final Rect EDIT_OFFSET = Rect.corners(LABEL_LEFT, 0, 0, EDIT_HEIGHT);
    private static final Rect PANEL_OFFSET = Rect.corners(0, 0, 0, -BUTTON_HEIGHT - SPACING);

    private final VanillaButton deleteButton;
    private final EditBox recipeTypeEdit;
    private final EditBox targetRecipeEdit;
    private final EditBox voltageTierEdit;
    private final Tab tab;
    private final MEPatternIngredientPanel inputPanel;
    private final MEPatternIngredientPanel outputPanel;
    private final MEPatternDraft draft = MEPatternDraft.empty();
    @Nullable
    private UUID originalUuid;

    public MEPatternEditorPanel(MEPatternTerminalScreen screen, Runnable onCancel) {
        super(screen);
        this.recipeTypeEdit = Widgets.editBox();
        this.targetRecipeEdit = Widgets.editBox();
        this.voltageTierEdit = Widgets.editBox();
        this.deleteButton = new VanillaButton(menu, tr("delete"), null, this::delete);
        this.inputPanel = MEPatternIngredientPanel.input(screen, draft.inputRows());
        this.outputPanel = MEPatternIngredientPanel.output(screen, draft.outputRows());

        var machinePanel = new Panel(screen);
        var recipeTypeLabel = new Label(menu, tr("recipeType"));
        recipeTypeLabel.verticalAlign = Label.Alignment.MIDDLE;
        machinePanel.addChild(LABEL_OFFSET, recipeTypeLabel);
        machinePanel.addVanillaWidget(EDIT_ANCHOR, EDIT_OFFSET, 0, recipeTypeEdit);
        var lineOffset = EDIT_HEIGHT + SPACING;
        var targetRecipeLabel = new Label(menu, tr("targetRecipe"));
        targetRecipeLabel.verticalAlign = Label.Alignment.MIDDLE;
        machinePanel.addChild(LABEL_OFFSET.offset(0, lineOffset), targetRecipeLabel);
        machinePanel.addVanillaWidget(EDIT_ANCHOR, EDIT_OFFSET.offset(0, lineOffset), 0, targetRecipeEdit);
        lineOffset += EDIT_HEIGHT + SPACING;
        var voltageLabel = new Label(menu, tr("voltageTier"));
        voltageLabel.verticalAlign = Label.Alignment.MIDDLE;
        machinePanel.addChild(LABEL_OFFSET.offset(0, lineOffset), voltageLabel);
        machinePanel.addVanillaWidget(EDIT_ANCHOR, EDIT_OFFSET.offset(0, lineOffset), 0, voltageTierEdit);

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
        syncEditBoxesFromDraft();
        deleteButton.setActive(false);
        postReset(draft.inputRows().isEmpty() ? 1 : 0);
    }

    public void edit(CraftPattern pattern) {
        reset();
        originalUuid = pattern.patternUuid();
        draft.copyFrom(MEPatternDraft.fromPattern(pattern));
        syncEditBoxesFromDraft();
        deleteButton.setActive(true);
        postReset(0);
    }

    private void reset() {
        draft.copyFrom(MEPatternDraft.empty());
        recipeTypeEdit.setValue("");
        targetRecipeEdit.setValue("");
        voltageTierEdit.setValue("");
    }

    private void postReset(int index) {
        inputPanel.resetPage();
        outputPanel.resetPage();
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
        var recipeType = parseResourceLocation(recipeTypeEdit);
        if (recipeType.isEmpty() && !recipeTypeEdit.getValue().isBlank()) {
            return Optional.empty();
        }
        var targetRecipe = parseResourceLocation(targetRecipeEdit);
        if (targetRecipe.isEmpty() && !targetRecipeEdit.getValue().isBlank()) {
            return Optional.empty();
        }
        var voltageTier = parseVoltageTier();
        if (voltageTier.isEmpty() && !voltageTierEdit.getValue().isBlank()) {
            return Optional.empty();
        }
        draft.setRecipeTypeId(recipeType.orElse(null));
        draft.setTargetRecipeId(targetRecipe.orElse(null));
        draft.setVoltageTier(voltageTier.orElse(null));
        return draft.toPattern();
    }

    private static Optional<ResourceLocation> parseResourceLocation(EditBox editBox) {
        var value = editBox.getValue();
        if (value.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(value));
    }

    private Optional<Integer> parseVoltageTier() {
        var value = voltageTierEdit.getValue();
        if (value.isBlank()) {
            return Optional.empty();
        }
        var tier = parseInteger(value);
        return tier >= 0 ? Optional.of(tier) : Optional.empty();
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

    private void syncEditBoxesFromDraft() {
        recipeTypeEdit.setValue(Optional.ofNullable(draft.recipeTypeId())
            .map(ResourceLocation::toString)
            .orElse(""));
        targetRecipeEdit.setValue(Optional.ofNullable(draft.targetRecipeId())
            .map(ResourceLocation::toString)
            .orElse(""));
        voltageTierEdit.setValue(Optional.ofNullable(draft.voltageTier())
            .map(Object::toString)
            .orElse(""));
    }
}
