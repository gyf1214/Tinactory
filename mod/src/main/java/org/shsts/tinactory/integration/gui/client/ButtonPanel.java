package org.shsts.tinactory.integration.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ButtonPanel extends GridViewPanel<ButtonPanel.ItemButton> {
    public class ItemButton extends Button {
        private final int slotIndex;

        public ItemButton(int slotIndex) {
            super(ButtonPanel.this.menu);
            this.slotIndex = slotIndex;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            return buttonTooltip(itemIndex(), mouseX - rect().x(), mouseY - rect().y());
        }

        @Override
        public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var rect = rect();
            renderButton(graphics, mouseX - rect.x(), mouseY - rect.y(), partialTick,
                rect, itemIndex(), isHovered(mouseX, mouseY));
        }

        @Override
        protected boolean canClick(int button, double mouseX, double mouseY) {
            return canClickButton(itemIndex(), mouseX - rect().x(), mouseY - rect().y(), button);
        }

        @Override
        public void onMouseClicked(double mouseX, double mouseY, int button) {
            playButtonSound();
            onSelect(itemIndex(), mouseX - rect().x(), mouseY - rect().y(), button);
        }

        public int itemIndex() {
            return page * gridViewGroup.getSlotCount() + slotIndex;
        }

        public ButtonPanel parent() {
            return ButtonPanel.this;
        }
    }

    protected ButtonPanel(MenuScreen<?> screen, int buttonWidth, int buttonHeight, int verticalSpacing,
        Rect offset) {
        super(screen, buttonWidth, buttonHeight, verticalSpacing, offset);
    }

    public ButtonPanel(MenuScreen<?> screen, int buttonWidth, int buttonHeight, int verticalSpacing) {
        super(screen, buttonWidth, buttonHeight, verticalSpacing);
    }

    @Override
    protected ItemButton createSlot(int index) {
        return new ItemButton(index);
    }

    /**
     * mouseX and mouseY are relative to the button rect
     */
    protected abstract void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
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
}
