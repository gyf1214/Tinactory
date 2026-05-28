package org.shsts.tinactory.integration.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.client.GridViewGroup;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.integration.gui.client.PageButton.PAGE_ANCHOR;
import static org.shsts.tinactory.integration.gui.client.PageButton.PAGE_OFFSET_LEFT;
import static org.shsts.tinactory.integration.gui.client.PageButton.PAGE_OFFSET_RIGHT;
import static org.shsts.tinactory.integration.gui.client.PageButton.PAGE_PANEL_OFFSET;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ButtonPanel extends Panel {
    protected final GridViewGroup<ItemButton> gridViewGroup;
    protected int page = 0;

    private final PageButton leftPageButton;
    private final PageButton rightPageButton;

    public class ItemButton extends Button {
        private final int slotIndex;

        public ItemButton(int slotIndex) {
            super(ButtonPanel.this.menu);
            this.slotIndex = slotIndex;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return buttonTooltip(itemIndex(), mouseX - rect.x(), mouseY - rect.y());
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            renderButton(poseStack, mouseX - rect.x(), mouseY - rect.y(), partialTick,
                rect, itemIndex(), isHovered(mouseX, mouseY));
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return canClickButton(itemIndex(), mouseX - rect.x(), mouseY - rect.y(), button);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            playButtonSound();
            onSelect(itemIndex(), mouseX - rect.x(), mouseY - rect.y(), button);
        }

        public int itemIndex() {
            return page * gridViewGroup.getSlotCount() + slotIndex;
        }

        public ButtonPanel parent() {
            return ButtonPanel.this;
        }
    }

    private ButtonPanel(MenuScreen<?> screen, GridViewGroup<ItemButton> viewGroup) {
        super(screen, viewGroup);
        this.gridViewGroup = viewGroup;
        this.leftPageButton = PageButton.previousPage(menu, this::changePage);
        this.rightPageButton = PageButton.nextPage(menu, this::changePage);

        gridViewGroup.setSlotFactory(ItemButton::new);

        addChild(PAGE_ANCHOR, PAGE_OFFSET_LEFT, leftPageButton);
        addChild(PAGE_ANCHOR, PAGE_OFFSET_RIGHT, rightPageButton);
    }

    public ButtonPanel(MenuScreen<?> screen, int buttonWidth, int buttonHeight, int verticalSpacing) {
        this(screen, new GridViewGroup<>(buttonWidth, buttonHeight, verticalSpacing, PAGE_PANEL_OFFSET));
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

    protected void setPage(int val) {
        var itemCount = getItemCount();
        var slotCount = gridViewGroup.getSlotCount();
        var maxPage = Math.max(1, (itemCount + slotCount - 1) / slotCount);
        page = MathUtil.clamp(val, 0, maxPage - 1);
        leftPageButton.setActive(page != 0);
        rightPageButton.setActive(page != maxPage - 1);
        for (var i = 0; i < gridViewGroup.getSlotCount(); i++) {
            var button = gridViewGroup.getSlot(i);
            var itemIndex = page * slotCount + button.slotIndex;
            button.setActive(itemIndex >= 0 && itemIndex < itemCount);
        }
    }

    private void changePage(int change) {
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
