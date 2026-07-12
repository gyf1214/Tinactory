package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.IViewNode;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.gui.client.VanillaWidgetAdapter;
import org.shsts.tinactory.integration.gui.client.Widgets;

import java.util.function.Consumer;

import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternMachinePanel extends Panel {
    private final MEPatternDraft draft;
    private final EditBox recipeTypeEdit;
    private final EditBox targetRecipeEdit;
    private final VanillaButton voltageButton;

    public MEPatternMachinePanel(MenuScreen<?> screen, MEPatternDraft draft) {
        super(screen);
        this.draft = draft;
        this.recipeTypeEdit = Widgets.editBox();
        this.targetRecipeEdit = Widgets.editBox();
        this.voltageButton = new VanillaButton(menu, Component.empty(), null, () -> {}) {
            @Override
            protected boolean canClick(int button, double mouseX, double mouseY) {
                return true;
            }

            @Override
            public void onMouseClicked(double mouseX, double mouseY, int button) {
                if (!disabled) {
                    playDownSound();
                    onVoltageChange(button == 0 ? 1 : -1);
                }
            }
        };

        recipeTypeEdit.setResponder($ -> updateLoc($, draft::setRecipeTypeId));
        recipeTypeEdit.setMaxLength(Integer.MAX_VALUE);
        targetRecipeEdit.setResponder($ -> updateLoc($, draft::setTargetRecipeId));
        targetRecipeEdit.setMaxLength(Integer.MAX_VALUE);

        var line = 0;
        line = addRow(line, "recipeType", recipeTypeEdit);
        line = addRow(line, "targetRecipe", targetRecipeEdit);
        addRow(line, "voltageTier", voltageButton, BUTTON_HEIGHT);
    }

    private int addRow(int line, String labelKey, IViewNode child, int height) {
        var label = new Label(menu, tr(labelKey));
        addChild(new Rect(0, line, 0, FONT_HEIGHT), label);
        line += FONT_HEIGHT + SPACING;
        addChild(RectD.corners(0d, 0d, 1d, 0d), new Rect(0, line, 0, height), child);
        line += height + SPACING;
        return line;
    }

    private int addRow(int line, String labelKey, EditBox editBox) {
        return addRow(line, labelKey, new VanillaWidgetAdapter<>(editBox), EDIT_HEIGHT);
    }

    private void syncVoltageButton() {
        if (draft.voltageTier() != null &&
            (draft.voltageTier() <= 0 || draft.voltageTier() >= Voltage.MAX.rank)) {
            draft.setVoltageTier(null);
        }
        if (draft.voltageTier() != null) {
            var v = Voltage.fromRank(draft.voltageTier());
            voltageButton.setLabel(Component.literal(v.displayName()));
        } else {
            voltageButton.setLabel(tr("ignoreVoltage"));
        }
    }

    private void onVoltageChange(int change) {
        if (draft.voltageTier() == null) {
            draft.setVoltageTier(change > 0 ? 1 : Voltage.MAX.rank - 1);
        } else {
            var rank1 = draft.voltageTier() + change;
            draft.setVoltageTier(rank1 > 0 && rank1 < Voltage.MAX.rank ? rank1 : null);
        }
        syncVoltageButton();
    }

    @Override
    protected void doRefresh() {
        recipeTypeEdit.setValue(locString(draft.recipeTypeId()));
        targetRecipeEdit.setValue(locString(draft.targetRecipeId()));
        syncVoltageButton();
    }

    private static String locString(@Nullable ResourceLocation value) {
        return value == null ? "" : value.toString();
    }

    private void updateLoc(String value, Consumer<ResourceLocation> updater) {
        if (value.isBlank()) {
            updater.accept(null);
        } else {
            var loc = ResourceLocation.tryParse(value);
            if (loc != null) {
                updater.accept(loc);
            }
        }
    }
}
