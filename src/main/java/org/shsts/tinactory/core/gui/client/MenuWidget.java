package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.common.ISelf;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuWidget extends GuiComponent implements
        Widget, GuiEventListener, NarratableEntry, ISelf<MenuWidget> {

    protected final Menu<?, ?> menu;
    protected Rect rect;
    protected boolean active = true;

    public MenuWidget(Menu<?, ?> menu) {
        this.menu = menu;
        this.setBlitOffset(0);
    }

    public void setRect(Rect value) {
        rect = value;
    }

    public void setActive(boolean value) {
        active = value;
    }

    public abstract void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (active) {
            doRender(poseStack, mouseX, mouseY, partialTick);
        }
    }

    protected boolean canHover() {
        return false;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return active && canHover() && rect.in(mouseX, mouseY);
    }

    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        return Optional.empty();
    }

    protected boolean canClick(int button) {
        return false;
    }

    protected boolean isClicking(double mouseX, double mouseY, int button) {
        return active && canClick(button) && rect.in(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isClicking(mouseX, mouseY, button)) {
            onMouseClicked(mouseX, mouseY, button);
            return true;
        }
        return false;
    }

    /**
     * Do not consider drag now.
     */
    public void onMouseClicked(double mouseX, double mouseY, int button) {}

    @Override
    public NarrationPriority narrationPriority() {
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {}
}
