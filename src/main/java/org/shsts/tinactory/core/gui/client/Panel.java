package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel extends MenuWidget {
    protected final List<MenuWidget> children = new ArrayList<>();
    @Nullable
    private MenuWidget hoveringChild = null;
    @Nullable
    private MenuWidget clickingChild = null;

    public boolean visible = true;

    public Panel(Menu<?> menu) {
        super(menu, RectD.FULL, Rect.ZERO);
    }

    public Panel(Menu<?> menu, RectD anchor, Rect offset) {
        super(menu, anchor, offset);
    }

    public void addWidget(MenuWidget widget) {
        children.add(widget);
    }

    protected void initChildren() {
        for (var child : children) {
            child.init(rect);
        }
    }

    @Override
    public void init(Rect parent) {
        super.init(parent);
        initChildren();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }
        for (var child : children) {
            child.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        hoveringChild = null;
        if (!visible) {
            return false;
        }
        for (var child : children) {
            if (child.isHovering(mouseX, mouseY)) {
                hoveringChild = child;
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<List<Component>> getTooltip() {
        return Optional.ofNullable(hoveringChild).flatMap(MenuWidget::getTooltip);
    }

    @Override
    public boolean isClicking(double mouseX, double mouseY, int button) {
        clickingChild = null;
        if (!visible) {
            return false;
        }
        for (var child : children) {
            if (child.isClicking(mouseX, mouseY, button)) {
                clickingChild = child;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        if (clickingChild != null) {
            clickingChild.onMouseClicked(mouseX, mouseY, button);
        }
    }
}
