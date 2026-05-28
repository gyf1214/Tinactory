package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Button;
import org.shsts.tinactory.integration.gui.client.GridViewPanel;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.Widgets;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternIngredientPanel extends GridViewPanel<MEPatternIngredientPanel.Row> {
    private static final Rect ROWS_OFFSET = PAGE_PANEL_OFFSET
        .offset(0, FONT_HEIGHT + SPACING)
        .enlarge(0, -FONT_HEIGHT - SPACING);
    private static final int PORT_COLUMN_WIDTH = 48;

    private final List<MEPatternIngredientDraft> drafts;

    private int draftIndex(int rowIndex) {
        return page * gridViewGroup.getRowCount() + rowIndex;
    }

    private class KeyButton extends Button {
        private final Row row;

        public KeyButton(MenuBase menu, Row row) {
            super(menu);
            this.row = row;
        }

        private Optional<MEPatternIngredientDraft> draft() {
            var index = draftIndex(row.index);
            return index >= drafts.size() ? Optional.empty() : Optional.of(drafts.get(index));
        }

        private Optional<IStackKey> key() {
            return draft().map(MEPatternIngredientDraft::key);
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return button == 0 || button == 1;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return key().flatMap(IStackKey::tooltip);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            RenderUtil.blit(poseStack, SLOT_BACKGROUND, getBlitOffset(), rect);
            var rect1 = rect.offset(1, 1).enlarge(-2, -2);
            key().ifPresent(key -> RenderUtil.renderDescriptor(
                poseStack, key.display(), rect1, getBlitOffset()));
            if (isHovered(mouseX, mouseY)) {
                RenderUtil.renderSlotHover(poseStack, rect1);
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);

            var carried = menu.getCarried();
            var index = draftIndex(row.index);

            if (button == 1 && index < drafts.size() && carried.isEmpty()) {
                removeDraft(index);
                return;
            }
            if (carried.isEmpty()) {
                return;
            }
            MEPatternIngredientDraft.fromItem(carried).ifPresent(draft -> {
                if (index >= drafts.size()) {
                    if (button != 0) {
                        draft.setAmount(parseLong(row.amountEdit.getValue()));
                    }
                    draft.setPort(parseInteger(row.portEdit.getValue()));
                    drafts.add(draft);
                } else {
                    drafts.get(index).setKey(draft.key());
                    if (button == 0) {
                        drafts.get(index).setAmount(draft.amount());
                    }
                }
                refresh();
            });
        }
    }

    protected class Row extends Panel {
        private final int index;
        private final EditBox amountEdit;
        private final EditBox portEdit;

        public Row(int index) {
            super(MEPatternIngredientPanel.this.screen);
            this.index = index;
            var keyButton = new KeyButton(menu, this);
            this.amountEdit = Widgets.editBox();
            this.portEdit = Widgets.editBox();

            addChild(RectD.corners(0d, 0.5d, 0d, 0.5d),
                Rect.corners(0, -SLOT_SIZE / 2, SLOT_SIZE, SLOT_SIZE / 2),
                keyButton);
            addVanillaWidget(RectD.corners(0d, 0.5d, 1d, 0.5d),
                Rect.corners(SLOT_SIZE + SPACING, -EDIT_HEIGHT / 2, -PORT_COLUMN_WIDTH - SPACING, EDIT_HEIGHT / 2),
                0, amountEdit);
            addVanillaWidget(RectD.corners(1d, 0.5d, 1d, 0.5d),
                Rect.corners(-PORT_COLUMN_WIDTH, -EDIT_HEIGHT / 2, 0, EDIT_HEIGHT / 2),
                0, portEdit);

            amountEdit.setResponder(value -> draft().ifPresent($ -> $.setAmount(parseLong(value))));
            portEdit.setResponder(value -> draft().ifPresent($ -> $.setPort(parseInteger(value))));
        }

        private Optional<MEPatternIngredientDraft> draft() {
            var draftIndex = draftIndex(index);
            return draftIndex < drafts.size() ? Optional.of(drafts.get(draftIndex)) : Optional.empty();
        }

        @Override
        protected void doRefresh() {
            var draft = draft();
            var amount = draft.map($ -> Long.toString($.amount())).orElse("");
            var port = draft.flatMap($ -> Optional.ofNullable($.port()))
                .map($ -> Integer.toString($))
                .orElse("");
            amountEdit.setValue(amount);
            portEdit.setValue(port);
        }
    }

    private MEPatternIngredientPanel(MenuScreen<?> screen, List<MEPatternIngredientDraft> drafts,
        String label) {
        super(screen, 0, SLOT_SIZE, SPACING, ROWS_OFFSET);
        this.drafts = drafts;

        var keyLabel = new Label(menu, tr(label));
        var amountLabel = new Label(menu, tr("amount"));
        var portLabel = new Label(menu, tr("port"));
        portLabel.horizontalAlign = amountLabel.horizontalAlign = Label.Alignment.MIDDLE;
        addChild(keyLabel);
        addChild(RectD.corners(0d, 0d, 1d, 0d),
            Rect.corners(SLOT_SIZE + SPACING, 0, -PORT_COLUMN_WIDTH - SPACING, 0),
            amountLabel);
        addChild(RectD.corners(1d, 0d, 1d, 0d),
            Rect.corners(-PORT_COLUMN_WIDTH, 0, 0, 0),
            portLabel);
    }

    public static MEPatternIngredientPanel input(MenuScreen<?> screen, List<MEPatternIngredientDraft> drafts) {
        return new MEPatternIngredientPanel(screen, drafts, "editor.input");
    }

    public static MEPatternIngredientPanel output(MenuScreen<?> screen, List<MEPatternIngredientDraft> drafts) {
        return new MEPatternIngredientPanel(screen, drafts, "editor.output");
    }

    @Override
    protected Row createSlot(int index) {
        return new Row(index);
    }

    @Override
    protected int getItemCount() {
        return drafts.size() + 1;
    }

    private void removeDraft(int draftIndex) {
        drafts.remove(draftIndex);
        refresh();
    }

    public void resetPage() {
        setPage(0);
    }

    private static long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return 1L;
        }
    }

    @Nullable
    private static Integer parseInteger(String value) {
        if (value.isBlank()) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}
