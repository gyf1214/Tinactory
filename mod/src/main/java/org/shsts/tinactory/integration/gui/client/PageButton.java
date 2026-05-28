package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.util.ClientUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.function.IntConsumer;

import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PageButton extends SimpleButton {
    private static final int PAGE_WIDTH = 12;
    private static final int PAGE_HEIGHT = 18;
    private static final int PAGE_MARGIN = 12;
    public static final Rect PAGE_PANEL_OFFSET = Rect.corners(0, 0, 0, -PAGE_HEIGHT - SPACING);
    public static final RectD PAGE_ANCHOR = new RectD(0.5, 1d, 0d, 0d);
    private static final Rect PAGE_OFFSET = Rect.corners(0, -PAGE_HEIGHT, PAGE_WIDTH, 0);
    public static final Rect PAGE_OFFSET_LEFT = PAGE_OFFSET.offset(-PAGE_MARGIN - PAGE_WIDTH, 0);
    public static final Rect PAGE_OFFSET_RIGHT = PAGE_OFFSET.offset(PAGE_MARGIN, 0);

    private static final int TEX_Y = 208;

    private final int pageChange;
    private final IntConsumer pageChanger;

    private PageButton(MenuBase menu, int texX, int pageChange, IntConsumer pageChanger) {
        super(menu, RECIPE_BOOK_BG, null, texX, TEX_Y, texX, TEX_Y + PAGE_HEIGHT);
        this.pageChange = pageChange;
        this.pageChanger = pageChanger;
    }

    public static PageButton previousPage(MenuBase menu, IntConsumer pageChanger) {
        return new PageButton(menu, 15, -1, pageChanger);
    }

    public static PageButton nextPage(MenuBase menu, IntConsumer pageChanger) {
        return new PageButton(menu, 1, 1, pageChanger);
    }

    @Override
    protected void playDownSound() {
        ClientUtil.playSound(SoundEvents.BOOK_PAGE_TURN);
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        pageChanger.accept(pageChange);
    }
}
