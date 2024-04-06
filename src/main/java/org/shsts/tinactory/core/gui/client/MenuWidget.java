package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.common.ISelf;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class MenuWidget extends GuiComponent implements ISelf<MenuWidget>, Widget {
    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private final RectD anchor;
    private final Rect offset;

    protected final Menu<?> menu;
    protected final int zIndex;
    protected Rect rect;

    public MenuWidget(Menu<?> menu, RectD anchor, Rect rect, int zIndex) {
        this.menu = menu;
        this.offset = rect;
        this.anchor = anchor;
        this.rect = rect;
        this.zIndex = zIndex;
    }

    public MenuWidget(Menu<?> menu, RectD anchor, Rect offset) {
        this(menu, anchor, offset, Menu.DEFAULT_Z_INDEX);
    }

    public MenuWidget(Menu<?> menu, Rect rect) {
        this(menu, RectD.ZERO, rect);
    }

    public void init(Rect parent) {
        var sx = parent.inX(anchor.x()) + offset.x();
        var tx = parent.inX(anchor.endX()) + offset.endX();
        var sy = parent.inY(anchor.y()) + offset.y();
        var ty = parent.inY(anchor.endY()) + offset.endY();

        rect = Rect.corners(sx, sy, tx, ty);
    }

    @Override
    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    protected boolean canHover() {
        return false;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return canHover() && rect.in(mouseX, mouseY);
    }

    public Optional<List<Component>> getTooltip() {
        return Optional.empty();
    }

    protected boolean canClick(int button) {
        return false;
    }

    public boolean isClicking(double mouseX, double mouseY, int button) {
        return canClick(button) && rect.in(mouseX, mouseY);
    }

    /**
     * Do not consider drag now.
     */
    public void onMouseClicked(double mouseX, double mouseY, int button) {}
}
