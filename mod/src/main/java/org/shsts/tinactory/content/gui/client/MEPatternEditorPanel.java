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
import java.util.UUID;

import static org.shsts.tinactory.AllMenus.ME_PATTERN_ACTION;
import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Tab.TAB_OFFSET;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternEditorPanel extends Panel {
    private static final int BUTTON_WIDTH = 48;
    private static final UUID DRAFT_UUID = new UUID(0L, 0L);
    private static final int LABEL_LEFT = 64;
    private static final Rect LABEL_OFFSET = new Rect(0, 0, LABEL_LEFT, EDIT_HEIGHT);
    private static final RectD EDIT_ANCHOR = RectD.corners(0d, 0d, 1d, 0d);
    private static final Rect EDIT_OFFSET = Rect.corners(LABEL_LEFT, 0, 0, EDIT_HEIGHT);

    private final VanillaButton deleteButton;
    private final EditBox recipeTypeEdit;
    private final EditBox voltageTierEdit;
    private final Label feedbackLabel;
    private final Tab tab;
    private final MEPatternIngredientPanel inputPanel;
    private final MEPatternIngredientPanel outputPanel;
    private final List<MEPatternIngredientDraft> inputRows = new ArrayList<>();
    private final List<MEPatternIngredientDraft> outputRows = new ArrayList<>();
    @Nullable
    private UUID originalUuid;

    public MEPatternEditorPanel(MEPatternTerminalScreen screen, Runnable onCancel) {
        super(screen);
        this.recipeTypeEdit = Widgets.editBox();
        this.voltageTierEdit = Widgets.editBox();
        this.feedbackLabel = new Label(menu);
        this.deleteButton = new VanillaButton(menu, tr("delete"), null, this::delete);
        this.inputPanel = new MEPatternIngredientPanel(screen, inputRows);
        this.outputPanel = new MEPatternIngredientPanel(screen, outputRows);

        var machinePanel = new Panel(screen);
        var recipeTypeLabel = new Label(menu, tr("recipeType"));
        recipeTypeLabel.verticalAlign = Label.Alignment.MIDDLE;
        machinePanel.addChild(LABEL_OFFSET, recipeTypeLabel);
        machinePanel.addVanillaWidget(EDIT_ANCHOR, EDIT_OFFSET, 0, recipeTypeEdit);
        var voltageLabel = new Label(menu, tr("voltageTier"));
        voltageLabel.verticalAlign = Label.Alignment.MIDDLE;
        var lineOffset = EDIT_HEIGHT + SPACING;
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
        feedbackLabel.horizontalAlign = Label.Alignment.MIDDLE;
        addChild(RectD.corners(0d, 1d, 1d, 1d), new Rect(0, -BUTTON_HEIGHT - SPACING - FONT_HEIGHT, 0, FONT_HEIGHT),
            feedbackLabel);

        var panelOffset = Rect.corners(0, 0, 0, -BUTTON_HEIGHT - FONT_HEIGHT - SPACING * 2);
        addGroup(panelOffset, inputPanel);
        addGroup(panelOffset, outputPanel);
        addGroup(panelOffset, machinePanel);
        addGroup(TAB_OFFSET, tab);
    }

    public void create() {
        reset();
        originalUuid = null;
        deleteButton.setActive(false);
        postReset();
    }

    public void edit(CraftPattern pattern) {
        reset();
        originalUuid = pattern.patternUuid();
        inputRows.addAll(fromAmounts(pattern.inputs(), pattern.constraints(), PortDirection.INPUT));
        outputRows.addAll(fromAmounts(pattern.outputs(), pattern.constraints(), PortDirection.OUTPUT));
        recipeTypeEdit.setValue(recipeType(pattern.constraints()).map(ResourceLocation::toString).orElse(""));
        voltageTierEdit.setValue(voltageConstraintTier(pattern.constraints()).map(Object::toString).orElse(""));
        deleteButton.setActive(true);
        postReset();
    }

    public void showFeedback(Component message) {
        feedbackLabel.setLines(message);
    }

    private void reset() {
        inputRows.clear();
        outputRows.clear();
        recipeTypeEdit.setValue("");
        voltageTierEdit.setValue("");
        feedbackLabel.setLines(TextComponent.EMPTY);
    }

    private void postReset() {
        inputPanel.resetPage();
        outputPanel.resetPage();
        tab.select(0);
    }

    private void save() {
        toPattern().ifPresentOrElse(pattern -> {
            if (originalUuid == null) {
                menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.create(pattern));
            } else {
                var target = originalUuid;
                menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.update(target, pattern));
            }
        }, () -> feedbackLabel.setLines(tr("invalid")));
    }

    private void delete() {
        if (originalUuid != null) {
            var target = originalUuid;
            menu.triggerEvent(ME_PATTERN_ACTION, () -> MEPatternEventPacket.delete(target));
        }
    }

    private Optional<CraftPattern> toPattern() {
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
        return Optional.of(new CraftPattern(DRAFT_UUID, toAmounts(inputRows), outputs, constraints));
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
