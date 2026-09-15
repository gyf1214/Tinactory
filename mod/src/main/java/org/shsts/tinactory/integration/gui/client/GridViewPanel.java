package org.shsts.tinactory.integration.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.GridViewGroup;
import org.shsts.tinactory.core.gui.client.IViewNode;
import org.shsts.tinactory.integration.util.ClientUtil;

import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.PAGE_BACKWARD;
import static org.shsts.tinactory.core.gui.Texture.PAGE_BACKWARD_HOVERED;
import static org.shsts.tinactory.core.gui.Texture.PAGE_FORWARD;
import static org.shsts.tinactory.core.gui.Texture.PAGE_FORWARD_HOVERED;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class GridViewPanel<T extends IViewNode> extends Panel {
    private static final int PAGE_WIDTH = 12;
    private static final int PAGE_HEIGHT = 17;
    private static final int PAGE_MARGIN = 12;
    public static final Rect PAGE_PANEL_OFFSET = Rect.corners(0, 0, 0, -PAGE_HEIGHT - SPACING - 1);
    private static final RectD PAGE_ANCHOR = new RectD(0.5, 1d, 0d, 0d);
    private static final Rect PAGE_OFFSET = Rect.corners(0, -PAGE_HEIGHT, PAGE_WIDTH, 0);
    private static final Rect PAGE_OFFSET_LEFT = PAGE_OFFSET.offset(-PAGE_MARGIN - PAGE_WIDTH, 0);
    private static final Rect PAGE_OFFSET_RIGHT = PAGE_OFFSET.offset(PAGE_MARGIN, 0);

    private class PageButton extends SimpleButton {
        private final int pageChange;

        private PageButton(Texture texture, Texture hoverTexture, int pageChange) {
            super(GridViewPanel.this.menu, texture, hoverTexture, null);
            this.pageChange = pageChange;
        }

        @Override
        protected void playDownSound() {
            ClientUtil.playSound(SoundEvents.BOOK_PAGE_TURN);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            super.onMouseClicked(mouseX, mouseY, button);
            changePage(pageChange);
        }
    }

    protected final GridViewGroup<T> gridViewGroup;
    protected int page = 0;
    @Nullable
    private final PageButton leftPageButton;
    @Nullable
    private final PageButton rightPageButton;

    private GridViewPanel(MenuScreen<?> screen, GridViewGroup<T> gridViewGroup, boolean showPageButton) {
        super(screen, gridViewGroup);
        this.gridViewGroup = gridViewGroup;
        gridViewGroup.setSlotFactory(this::createSlot);

        if (showPageButton) {
            this.leftPageButton = new PageButton(PAGE_BACKWARD, PAGE_BACKWARD_HOVERED, -1);
            this.rightPageButton = new PageButton(PAGE_FORWARD, PAGE_FORWARD_HOVERED, 1);
            addChild(PAGE_ANCHOR, PAGE_OFFSET_LEFT, leftPageButton);
            addChild(PAGE_ANCHOR, PAGE_OFFSET_RIGHT, rightPageButton);
        } else {
            this.leftPageButton = null;
            this.rightPageButton = null;
        }
    }

    protected GridViewPanel(MenuScreen<?> screen, int itemWidth, int itemHeight,
        int verticalSpacing, Rect offset, boolean showPageButton) {
        this(screen, new GridViewGroup<>(itemWidth, itemHeight, verticalSpacing, offset), showPageButton);
    }

    protected GridViewPanel(MenuScreen<?> screen, int itemWidth, int itemHeight, int verticalSpacing,
        boolean showPageButton) {
        this(screen, itemWidth, itemHeight, verticalSpacing,
            showPageButton ? PAGE_PANEL_OFFSET : Rect.ZERO, showPageButton);
    }

    protected abstract T createSlot(int index);

    protected abstract int getItemCount();

    protected void setPage(int val) {
        var itemCount = getItemCount();
        var slotCount = gridViewGroup.getSlotCount();
        var maxPage = Math.max(1, (itemCount + slotCount - 1) / slotCount);
        page = Math.clamp(val, 0, maxPage - 1);
        if (leftPageButton != null) {
            leftPageButton.setActive(page != 0);
        }
        if (rightPageButton != null) {
            rightPageButton.setActive(page != maxPage - 1);
        }
        for (var i = 0; i < gridViewGroup.getSlotCount(); i++) {
            var button = gridViewGroup.getSlot(i);
            var itemIndex = page * slotCount + i;
            button.setActive(itemIndex >= 0 && itemIndex < itemCount);
        }
    }

    protected void changePage(int change) {
        setPage(page + change);
    }

    @Override
    protected void postLayout() {
        refresh();
    }

    @Override
    protected void doRefresh() {
        changePage(0);
    }
}
