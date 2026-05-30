package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.Widgets;

import java.util.Optional;

import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SPACING;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternMachinePanel extends Panel {
    private static final int LABEL_LEFT = 64;
    private static final Rect LABEL_OFFSET = new Rect(0, 0, LABEL_LEFT, EDIT_HEIGHT);
    private static final RectD EDIT_ANCHOR = RectD.corners(0d, 0d, 1d, 0d);
    private static final Rect EDIT_OFFSET = Rect.corners(LABEL_LEFT, 0, 0, EDIT_HEIGHT);

    private final MEPatternDraft draft;
    private final EditBox recipeTypeEdit;
    private final EditBox targetRecipeEdit;
    private final EditBox voltageTierEdit;

    public MEPatternMachinePanel(MenuScreen<?> screen, MEPatternDraft draft) {
        super(screen);
        this.draft = draft;
        this.recipeTypeEdit = Widgets.editBox();
        this.targetRecipeEdit = Widgets.editBox();
        this.voltageTierEdit = Widgets.editBox();

        addRow(0, "recipeType", recipeTypeEdit);
        addRow(EDIT_HEIGHT + SPACING, "targetRecipe", targetRecipeEdit);
        addRow((EDIT_HEIGHT + SPACING) * 2, "voltageTier", voltageTierEdit);

        recipeTypeEdit.setResponder(this::updateRecipeType);
        targetRecipeEdit.setResponder(this::updateTargetRecipe);
        voltageTierEdit.setResponder(this::updateVoltageTier);
    }

    private void addRow(int y, String labelKey, EditBox editBox) {
        var label = new Label(menu, tr(labelKey));
        label.verticalAlign = Label.Alignment.MIDDLE;
        addChild(LABEL_OFFSET.offset(0, y), label);
        addVanillaWidget(EDIT_ANCHOR, EDIT_OFFSET.offset(0, y), 0, editBox);
    }

    @Override
    protected void doRefresh() {
        recipeTypeEdit.setValue(resourceLocationString(draft.recipeTypeId()));
        targetRecipeEdit.setValue(resourceLocationString(draft.targetRecipeId()));
        voltageTierEdit.setValue(Optional.ofNullable(draft.voltageTier())
            .map(Object::toString)
            .orElse(""));
    }

    private static String resourceLocationString(@Nullable ResourceLocation value) {
        return value == null ? "" : value.toString();
    }

    private void updateRecipeType(String value) {
        updateResourceLocation(value).ifPresentOrElse(draft::setRecipeTypeId, () -> {
            if (value.isBlank()) {
                draft.setRecipeTypeId(null);
            }
        });
    }

    private void updateTargetRecipe(String value) {
        updateResourceLocation(value).ifPresentOrElse(draft::setTargetRecipeId, () -> {
            if (value.isBlank()) {
                draft.setTargetRecipeId(null);
            }
        });
    }

    private static Optional<ResourceLocation> updateResourceLocation(String value) {
        if (value.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(ResourceLocation.tryParse(value));
    }

    private void updateVoltageTier(String value) {
        if (value.isBlank()) {
            draft.setVoltageTier(null);
            return;
        }
        var tier = parseInteger(value);
        if (tier >= 0) {
            draft.setVoltageTier(tier);
        }
    }

    private static int parseInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return -1;
        }
    }

}
