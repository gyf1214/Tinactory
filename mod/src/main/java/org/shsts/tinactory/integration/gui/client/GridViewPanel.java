package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.GridViewGroup;
import org.shsts.tinactory.core.gui.client.IViewNode;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class GridViewPanel<T extends IViewNode> extends Panel {
    private static final int PAGE_WIDTH = 12;
    private static final int PAGE_HEIGHT = 18;
    private static final int PAGE_MARGIN = 12;
    private static final Rect PAGE_PANEL_OFFSET = Rect.corners(0, 0, 0, -PAGE_HEIGHT - SPACING);
    private static final RectD PAGE_ANCHOR = new RectD(0.5, 1d, 0d, 0d);
    private static final Rect PAGE_OFFSET = Rect.corners(0, -PAGE_HEIGHT, PAGE_WIDTH, 0);
    private static final Rect PAGE_OFFSET_LEFT = PAGE_OFFSET.offset(-PAGE_MARGIN - PAGE_WIDTH, 0);
    private static final Rect PAGE_OFFSET_RIGHT = PAGE_OFFSET.offset(PAGE_MARGIN, 0);

    private class PageButton extends SimpleButton {
        private static final int TEX_Y = 208;

        private final int pageChange;

        private PageButton(int texX, int pageChange) {
            super(GridViewPanel.this.menu, RECIPE_BOOK_BG, null, texX, TEX_Y, texX, TEX_Y + PAGE_HEIGHT);
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
    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    protected GridViewPanel(MenuScreen<?> screen, GridViewGroup<T> gridViewGroup) {
        super(screen, gridViewGroup);
        this.gridViewGroup = gridViewGroup;
        this.leftPageButton = new PageButton(15, -1);
        this.rightPageButton = new PageButton(1, 1);

        gridViewGroup.setSlotFactory(this::createSlot);

        addChild(PAGE_ANCHOR, PAGE_OFFSET_LEFT, leftPageButton);
        addChild(PAGE_ANCHOR, PAGE_OFFSET_RIGHT, rightPageButton);
    }

    protected GridViewPanel(MenuScreen<?> screen, int itemWidth, int itemHeight, int verticalSpacing) {
        this(screen, new GridViewGroup<>(itemWidth, itemHeight, verticalSpacing, PAGE_PANEL_OFFSET));
    }

    protected abstract T createSlot(int index);

    protected abstract int getItemCount();

    protected void setPage(int val) {
        var itemCount = getItemCount();
        var slotCount = gridViewGroup.getSlotCount();
        var maxPage = Math.max(1, (itemCount + slotCount - 1) / slotCount);
        page = MathUtil.clamp(val, 0, maxPage - 1);
        leftPageButton.setActive(page != 0);
        rightPageButton.setActive(page != maxPage - 1);
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
