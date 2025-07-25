package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ButtonPanel extends Panel {
    private static final RectD PAGE_ANCHOR = new RectD(0.5, 1d, 0d, 0d);
    private static final Rect PAGE_OFFSET = new Rect(0, -18, 12, 18);
    private static final int PAGE_MARGIN = 12;
    private static final int BOTTOM_MARGIN = 21;

    private final int buttonWidth;
    private final int buttonHeight;
    private final int verticalSpacing;
    private int page = 0;

    private final List<ItemButton> buttons = new ArrayList<>();
    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    public class ItemButton extends Button {
        private int index = 0;

        public ItemButton(IMenu menu) {
            super(menu);
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return buttonTooltip(index, mouseX - rect.x(), mouseY - rect.y());
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            renderButton(poseStack, mouseX, mouseY, partialTick, rect, index);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            onSelect(index, mouseX - rect.x(), mouseY - rect.y());
        }

        public int getIndex() {
            return index;
        }

        public ButtonPanel getParent() {
            return ButtonPanel.this;
        }
    }

    private class PageButton extends SimpleButton {
        private static final int TEX_Y = 208;

        private final int pageChange;

        public PageButton(IMenu menu, int texX, int pageChange) {
            super(menu, RECIPE_BOOK_BG, null, texX, TEX_Y,
                texX, TEX_Y + PAGE_OFFSET.height());
            this.pageChange = pageChange;
        }

        @Override
        protected void playDownSound() {
            ClientUtil.playSound(SoundEvents.BOOK_PAGE_TURN);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            setPage(page + pageChange);
        }
    }

    public ButtonPanel(MenuScreen screen, int buttonWidth, int buttonHeight, int verticalSpacing) {
        super(screen);
        this.buttonWidth = buttonWidth;
        this.buttonHeight = buttonHeight;
        this.verticalSpacing = verticalSpacing;
        this.leftPageButton = new PageButton(menu, 15, -1);
        this.rightPageButton = new PageButton(menu, 1, 1);

        addWidget(PAGE_ANCHOR, PAGE_OFFSET.offset(-PAGE_MARGIN - PAGE_OFFSET.width(), 0), leftPageButton);
        addWidget(PAGE_ANCHOR, PAGE_OFFSET.offset(PAGE_MARGIN, 0), rightPageButton);
    }

    @Override
    protected void setRect(Rect rect) {
        var columns = Math.max(1, rect.width() / buttonWidth);
        var rows = Math.max(1, (rect.height() + verticalSpacing - BOTTOM_MARGIN) /
            (buttonHeight + verticalSpacing));
        int horizontalSpacing = columns > 1 ? (rect.width() - columns * buttonWidth) / (columns - 1) : 0;
        var buttonCount = rows * columns;

        var curSize = buttons.size();
        if (curSize < buttonCount) {
            for (var i = curSize; i < buttonCount; i++) {
                var column = i % columns;
                var row = i / columns;

                var x = column * (buttonWidth + horizontalSpacing);
                var y = row * (buttonHeight + verticalSpacing);

                var offset = new Rect(x, y, buttonWidth, buttonHeight);
                var button = new ItemButton(menu);
                buttons.add(button);
                button.setActive(active);
                addWidget(offset, button);
            }
        } else {
            for (var i = buttonCount; i < curSize; i++) {
                var button = buttons.get(i);
                children.removeIf(child -> child.child() == button);
            }
            buttons.subList(buttonCount, curSize).clear();
        }
        super.setRect(rect);
        refresh();
    }

    protected abstract int getItemCount();

    protected abstract void renderButton(PoseStack poseStack, int mouseX, int mouseY,
        float partialTick, Rect rect, int index);

    protected abstract void onSelect(int index, double mouseX, double mouseY);

    protected abstract Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY);

    protected void setPage(int index) {
        var buttonCount = buttons.size();
        var itemCount = getItemCount();
        var maxPage = Math.max(1, (itemCount + buttonCount - 1) / buttonCount);
        var newPage = MathUtil.clamp(index, 0, maxPage - 1);
        leftPageButton.setActive(newPage != 0);
        rightPageButton.setActive(newPage != maxPage - 1);
        var offset = newPage * buttonCount;
        for (var i = 0; i < buttonCount; i++) {
            var button = buttons.get(i);
            var j = i + offset;

            if (j < itemCount) {
                button.index = j;
                button.setActive(true);
            } else {
                button.setActive(false);
            }
        }
        page = newPage;
    }

    @Override
    protected void doRefresh() {
        setPage(page);
    }
}
