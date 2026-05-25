package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Button;
import org.shsts.tinactory.integration.gui.client.Label;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.gui.client.Widgets;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.ArrayList;
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
public class MEPatternIngredientRowPanel extends Panel {
    private static final int ROWS = 5;
    private static final int ROW_HEIGHT = SLOT_SIZE + SPACING;
    private static final int AMOUNT_X = 26;
    private static final int AMOUNT_WIDTH = 70;
    private static final int PORT_X = 104;
    private static final int PORT_WIDTH = 38;
    private static final int PAGE_Y = FONT_HEIGHT + SPACING + ROWS * ROW_HEIGHT;
    private static final int PAGE_BUTTON_WIDTH = 24;

    private final List<MEPatternIngredientDraft> drafts;
    private final List<Row> rows = new ArrayList<>();
    private int page = 0;

    private class KeyButton extends Button {
        private final int rowIndex;

        public KeyButton(MenuBase menu, int rowIndex) {
            super(menu);
            this.rowIndex = rowIndex;
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return button == 0 || button == 1;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            var draft = rows.get(rowIndex).draft();
            return draft.key() == null ? Optional.empty() : draft.key().tooltip();
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            RenderUtil.blit(poseStack, SLOT_BACKGROUND, getBlitOffset(), rect);
            var key = rows.get(rowIndex).draft().key();
            if (key != null) {
                RenderUtil.renderDescriptor(poseStack, key.display(), rect.offset(1, 1).resize(16, 16),
                    getBlitOffset());
            }
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            var draft = rows.get(rowIndex).draft();
            if (button == 1 && menu.getCarried().isEmpty() && draft.key() != null) {
                remove(rowIndex);
                return;
            }
            if (button != 0 || menu.getCarried().isEmpty()) {
                return;
            }
            stackKeyFromCarried().ifPresent(key -> {
                draft.setKey(key);
                ensureTrailingEmpty();
                refresh();
            });
        }
    }

    private class Row {
        private final KeyButton keyButton;
        private final EditBox amountEdit;
        private final EditBox portEdit;

        public Row(int index) {
            this.keyButton = new KeyButton(menu, index);
            this.amountEdit = Widgets.editBox();
            this.portEdit = Widgets.editBox();
            amountEdit.setResponder(value -> draft().setAmount(parseLong(value, 1L)));
            portEdit.setResponder(value -> draft().setPort(parseInteger(value)));
        }

        private MEPatternIngredientDraft draft() {
            var index = page * ROWS + rows.indexOf(this);
            ensureIndex(index);
            return drafts.get(index);
        }
    }

    public MEPatternIngredientRowPanel(MEPatternTerminalScreen screen, List<MEPatternIngredientDraft> drafts) {
        super(screen);
        this.drafts = drafts;
        ensureTrailingEmpty();
        addChild(new Rect(AMOUNT_X, 0, AMOUNT_WIDTH, FONT_HEIGHT), new Label(menu, tr("amount")));
        addChild(new Rect(PORT_X, 0, PORT_WIDTH, FONT_HEIGHT), new Label(menu, tr("port")));
        addChild(new Rect(0, PAGE_Y, PAGE_BUTTON_WIDTH, Widgets.BUTTON_HEIGHT),
            new VanillaButton(menu, tr("previous"), null, this::previousPage));
        addChild(new Rect(PAGE_BUTTON_WIDTH + SPACING, PAGE_Y, PAGE_BUTTON_WIDTH, Widgets.BUTTON_HEIGHT),
            new VanillaButton(menu, tr("next"), null, this::nextPage));
        for (var i = 0; i < ROWS; i++) {
            var row = new Row(i);
            rows.add(row);
            var y = FONT_HEIGHT + SPACING + i * ROW_HEIGHT;
            addChild(new Rect(0, y, SLOT_SIZE, SLOT_SIZE), row.keyButton);
            addVanillaWidget(RectD.ZERO, new Rect(AMOUNT_X, y, AMOUNT_WIDTH, EDIT_HEIGHT), 0, row.amountEdit);
            addVanillaWidget(RectD.ZERO, new Rect(PORT_X, y, PORT_WIDTH, EDIT_HEIGHT), 0, row.portEdit);
        }
    }

    public void resetPage() {
        page = 0;
        refresh();
    }

    private void previousPage() {
        if (page > 0) {
            page--;
            refresh();
        }
    }

    private void nextPage() {
        if ((page + 1) * ROWS < drafts.size()) {
            page++;
            refresh();
        }
    }

    private Optional<IStackKey> stackKeyFromCarried() {
        var stack = menu.getCarried();
        var fluid = StackHelper.getFluidHandlerFromItem(stack)
            .map(handler -> handler.drain(Integer.MAX_VALUE, IFluidHandler.FluidAction.SIMULATE))
            .filter(drained -> !drained.isEmpty());
        if (fluid.isPresent()) {
            return Optional.of(StackHelper.FLUID_ADAPTER.keyOf(fluid.get()));
        }
        return stack.isEmpty() ? Optional.empty() : Optional.of(StackHelper.ITEM_ADAPTER.keyOf(stack));
    }

    private void remove(int rowIndex) {
        var index = page * ROWS + rowIndex;
        if (index < drafts.size()) {
            drafts.remove(index);
            ensureTrailingEmpty();
            refresh();
        }
    }

    private void ensureIndex(int index) {
        while (drafts.size() <= index) {
            drafts.add(new MEPatternIngredientDraft());
        }
    }

    public void ensureTrailingEmpty() {
        drafts.removeIf(draft -> draft.isEmpty() && drafts.indexOf(draft) < drafts.size() - 1);
        if (drafts.isEmpty() || !drafts.get(drafts.size() - 1).isEmpty()) {
            drafts.add(new MEPatternIngredientDraft());
        }
    }

    @Override
    protected void doRefresh() {
        ensureTrailingEmpty();
        for (var row : rows) {
            var draft = row.draft();
            row.amountEdit.setValue(draft.isEmpty() ? "" : Long.toString(draft.amount()));
            row.portEdit.setValue(draft.port() == null ? "" : Integer.toString(draft.port()));
        }
    }

    private static long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

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
