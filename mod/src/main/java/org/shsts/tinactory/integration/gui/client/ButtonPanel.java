package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.GridViewGroup;
import org.shsts.tinactory.core.util.ClientUtil;

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

    protected final GridViewGroup gridViewGroup;
    protected int page = 0;

    protected final List<ItemButton> buttons = new ArrayList<>();
    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    public class ItemButton extends Button {
        private int index = 0;

        public ItemButton() {
            super(ButtonPanel.this.menu);
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return buttonTooltip(index, mouseX - rect.x(), mouseY - rect.y());
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            renderButton(poseStack, mouseX - rect.x(), mouseY - rect.y(), partialTick,
                rect, index, isHovering(mouseX, mouseY));
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return canClickButton(index, mouseX - rect.x(), mouseY - rect.y(), button);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            playButtonSound();
            onSelect(index, mouseX - rect.x(), mouseY - rect.y(), button);
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

        public PageButton(int texX, int pageChange) {
            super(ButtonPanel.this.menu, RECIPE_BOOK_BG, null, texX, TEX_Y,
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

    public ButtonPanel(MenuScreen<?> screen, int buttonWidth, int buttonHeight, int verticalSpacing) {
        super(screen, createGridViewGroup(buttonWidth, buttonHeight, verticalSpacing));
        this.gridViewGroup = (GridViewGroup) viewGroup;
        this.leftPageButton = new PageButton(15, -1);
        this.rightPageButton = new PageButton(1, 1);

        addChild(gridViewGroup.getPageButtonAnchor(), gridViewGroup.getLeftPageButtonOffset(), leftPageButton);
        addChild(gridViewGroup.getPageButtonAnchor(), gridViewGroup.getRightPageButtonOffset(), rightPageButton);
    }

    @Override
    public void setRect(Rect rect) {
        gridViewGroup.setRect(rect);
        var buttonCount = gridViewGroup.getButtonCount();

        var curSize = buttons.size();
        if (curSize < buttonCount) {
            for (var i = curSize; i < buttonCount; i++) {
                var button = new ItemButton();
                buttons.add(button);
                button.setActive(active);
                addChild(gridViewGroup.getButtonRect(i), button);
            }
        } else {
            for (var i = buttonCount; i < curSize; i++) {
                var button = buttons.get(i);
                removeChild(button);
            }
            buttons.subList(buttonCount, curSize).clear();
        }
        super.setRect(rect);
        refresh();
    }

    protected abstract int getItemCount();

    /**
     * mouseX and mouseY are relative to the button rect
     */
    protected abstract void renderButton(PoseStack poseStack, int mouseX, int mouseY,
        float partialTick, Rect rect, int index, boolean isHovering);

    protected boolean canClickButton(int index, double mouseX, double mouseY, int button) {
        return button == 0;
    }

    protected void playButtonSound() {
        ClientUtil.playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    /**
     * mouseX and mouseY are relative to the button rect
     */
    protected abstract void onSelect(int index, double mouseX, double mouseY, int button);

    /**
     * mouseX and mouseY are relative to the button rect
     */
    protected abstract Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY);

    protected void setPage(int index) {
        gridViewGroup.setItemCount(getItemCount());
        gridViewGroup.setPage(index);
        leftPageButton.setActive(gridViewGroup.isLeftPageEnabled());
        rightPageButton.setActive(gridViewGroup.isRightPageEnabled());
        for (var i = 0; i < buttons.size(); i++) {
            var button = buttons.get(i);
            var j = gridViewGroup.getVisibleIndex(i);

            if (j >= 0) {
                button.index = j;
                button.setActive(true);
            } else {
                button.setActive(false);
            }
        }
        page = gridViewGroup.getPage();
    }

    @Override
    protected void doRefresh() {
        setPage(page);
    }

    private static GridViewGroup createGridViewGroup(int buttonWidth, int buttonHeight, int verticalSpacing) {
        return new GridViewGroup(buttonWidth, buttonHeight, verticalSpacing, BOTTOM_MARGIN,
            PAGE_ANCHOR, PAGE_OFFSET, PAGE_MARGIN);
    }
}
