package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.content.gui.sync.MEPatternEventPacket;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.Tab;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.gui.client.Widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternEditorPanel extends Panel {
    private static final int BUTTON_WIDTH = 48;
    private static final int BODY_Y = 46;

    private final EditBox patternIdEdit;
    private final Label patternIdLabel;
    private final EditBox recipeTypeEdit;
    private final EditBox voltageTierEdit;
    private final Label feedbackLabel;
    private final MEPatternIngredientRowPanel inputPanel;
    private final MEPatternIngredientRowPanel outputPanel;
    private final List<MEPatternIngredientDraft> inputRows = new ArrayList<>();
    private final List<MEPatternIngredientDraft> outputRows = new ArrayList<>();
    private boolean createMode = true;
    @Nullable
    private String originalPatternId;

    public MEPatternEditorPanel(MEPatternTerminalScreen screen, Runnable onCancel) {
        super(screen);
        this.patternIdEdit = Widgets.editBox();
        this.patternIdLabel = new Label(menu);
        this.recipeTypeEdit = Widgets.editBox();
        this.voltageTierEdit = Widgets.editBox();
        this.feedbackLabel = new Label(menu);
        this.inputPanel = new MEPatternIngredientRowPanel(screen, inputRows);
        this.outputPanel = new MEPatternIngredientRowPanel(screen, outputRows);
        var machinePanel = machinePanel();
        var tab = new Tab(screen, inputPanel, Items.HOPPER, outputPanel, Items.CHEST, machinePanel, Items.COMPARATOR);

        addChild(new Rect(0, 0, 64, FONT_HEIGHT), new Label(menu, tr("id")));
        addVanillaWidget(RectD.ZERO, new Rect(70, -2, 104, EDIT_HEIGHT), 0, patternIdEdit);
        addChild(new Rect(70, 0, 104, FONT_HEIGHT), patternIdLabel);
        addChild(RectD.corners(1d, 0d, 1d, 0d), new Rect(-BUTTON_WIDTH * 3 - SPACING * 2, 0,
            BUTTON_WIDTH, BUTTON_HEIGHT), new VanillaButton(menu, tr("save"), null, this::save));
        addChild(RectD.corners(1d, 0d, 1d, 0d), new Rect(-BUTTON_WIDTH * 2 - SPACING, 0,
            BUTTON_WIDTH, BUTTON_HEIGHT), new VanillaButton(menu, tr("cancel"), null, onCancel));
        addChild(RectD.corners(1d, 0d, 1d, 0d), new Rect(-BUTTON_WIDTH, 0, BUTTON_WIDTH, BUTTON_HEIGHT),
            new VanillaButton(menu, tr("delete"), null, this::delete));
        addChild(new Rect(0, 24, 170, FONT_HEIGHT), feedbackLabel);
        addGroup(new Rect(0, BODY_Y, 0, 0), inputPanel);
        addGroup(new Rect(0, BODY_Y, 0, 0), outputPanel);
        addGroup(new Rect(0, BODY_Y, 0, 0), machinePanel);
        addGroup(new Rect(0, BODY_Y, 0, 0), tab);
    }

    public void create() {
        createMode = true;
        originalPatternId = null;
        patternIdEdit.setValue("");
        patternIdLabel.setLines(TextComponent.EMPTY);
        inputRows.clear();
        outputRows.clear();
        recipeTypeEdit.setValue("");
        voltageTierEdit.setValue("");
        feedbackLabel.setLines(TextComponent.EMPTY);
        refreshRows();
    }

    public void edit(CraftPattern pattern) {
        createMode = false;
        originalPatternId = pattern.patternId();
        patternIdEdit.setValue(pattern.patternId());
        patternIdLabel.setLines(new TextComponent(pattern.patternId()));
        inputRows.clear();
        outputRows.clear();
        inputRows.addAll(fromAmounts(pattern.inputs(), pattern.constraints(), PortDirection.INPUT));
        outputRows.addAll(fromAmounts(pattern.outputs(), pattern.constraints(), PortDirection.OUTPUT));
        recipeTypeEdit.setValue(recipeType(pattern.constraints()).map(ResourceLocation::toString)
            .orElse(""));
        voltageTierEdit.setValue(voltageConstraintTier(pattern.constraints()).map(Object::toString).orElse(""));
        feedbackLabel.setLines(TextComponent.EMPTY);
        refreshRows();
    }

    public void showFeedback(Component message) {
        feedbackLabel.setLines(message);
    }

    private Panel machinePanel() {
        var ret = new Panel(screen);
        ret.addChild(new Rect(0, 0, 72, FONT_HEIGHT), new Label(menu, tr("recipeType")));
        ret.addVanillaWidget(RectD.ZERO, new Rect(82, -2, 132, EDIT_HEIGHT), 0, recipeTypeEdit);
        ret.addChild(new Rect(0, 24, 72, FONT_HEIGHT), new Label(menu, tr("voltageTier")));
        ret.addVanillaWidget(RectD.ZERO, new Rect(82, 22, 48, EDIT_HEIGHT), 0, voltageTierEdit);
        return ret;
    }

    private void save() {
        toPattern().ifPresentOrElse(pattern -> {
            if (createMode) {
                menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.createPattern(pattern));
            } else if (originalPatternId != null) {
                menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.updatePattern(originalPatternId,
                    pattern));
            }
        }, () -> feedbackLabel.setLines(tr("invalid")));
    }

    private void delete() {
        if (!createMode && originalPatternId != null) {
            menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.deletePattern(originalPatternId));
        }
    }

    private Optional<CraftPattern> toPattern() {
        var patternId = patternIdEdit.getValue();
        if (patternId.isBlank()) {
            return Optional.empty();
        }
        var outputs = toAmounts(outputRows);
        if (outputs.isEmpty()) {
            return Optional.empty();
        }
        var constraints = constraints();
        var recipeType = parseRecipeType();
        if (recipeType.isEmpty() && !recipeTypeEdit.getValue().isBlank()) {
            return Optional.empty();
        }
        var voltageTier = parseVoltageTier();
        if (voltageTier.isEmpty() && !voltageTierEdit.getValue().isBlank()) {
            return Optional.empty();
        }
        recipeType.ifPresent(value -> constraints.add(new RecipeTypeConstraint(value)));
        voltageTier.ifPresent(value -> constraints.add(new VoltageConstraint(value)));
        return Optional.of(new CraftPattern(patternId, toAmounts(inputRows), outputs, constraints));
    }

    private List<IMachineConstraint> constraints() {
        var ret = new ArrayList<IMachineConstraint>();
        addPortConstraints(ret, inputRows, PortDirection.INPUT);
        addPortConstraints(ret, outputRows, PortDirection.OUTPUT);
        return ret;
    }

    private static void addPortConstraints(List<IMachineConstraint> ret, List<MEPatternIngredientDraft> rows,
        PortDirection direction) {
        var amountIndex = 0;
        for (var row : rows) {
            if (row.isEmpty()) {
                continue;
            }
            if (row.port() != null) {
                ret.add(row.toConstraint(direction, amountIndex));
            }
            amountIndex++;
        }
    }

    private static List<CraftAmount> toAmounts(List<MEPatternIngredientDraft> rows) {
        return rows.stream()
            .filter(row -> !row.isEmpty())
            .map(MEPatternIngredientDraft::toAmount)
            .toList();
    }

    private static List<MEPatternIngredientDraft> fromAmounts(List<CraftAmount> amounts,
        List<IMachineConstraint> constraints, PortDirection direction) {
        var ret = new ArrayList<MEPatternIngredientDraft>();
        for (var amount : amounts) {
            ret.add(MEPatternIngredientDraft.from(amount));
        }
        for (var constraint : constraints) {
            if (constraint instanceof PortConstraint port && port.direction() == direction &&
                port.index() < ret.size()) {
                ret.get(port.index()).setPort(port.port());
            }
        }
        return ret;
    }

    private void refreshRows() {
        inputPanel.ensureTrailingEmpty();
        outputPanel.ensureTrailingEmpty();
        inputPanel.resetPage();
        outputPanel.resetPage();
    }

    private Optional<ResourceLocation> parseRecipeType() {
        var value = recipeTypeEdit.getValue();
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

    private static Optional<ResourceLocation> recipeType(List<IMachineConstraint> constraints) {
        return constraints.stream()
            .filter(RecipeTypeConstraint.class::isInstance)
            .map(RecipeTypeConstraint.class::cast)
            .map(RecipeTypeConstraint::recipeTypeId)
            .findFirst();
    }

    private static Optional<Integer> voltageConstraintTier(List<IMachineConstraint> constraints) {
        return constraints.stream()
            .filter(VoltageConstraint.class::isInstance)
            .map(VoltageConstraint.class::cast)
            .map(VoltageConstraint::tier)
            .findFirst();
    }
}
