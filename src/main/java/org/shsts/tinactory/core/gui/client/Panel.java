package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
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
public class Panel extends ContainerWidget {
    protected final List<ContainerWidget> children = new ArrayList<>();
    @Nullable
    private ContainerWidget hoveringChild = null;
    @Nullable
    private ContainerWidget clickingChild = null;

    public boolean visible = true;

    public Panel(ContainerMenu<?> menu) {
        super(menu, RectD.FULL, Rect.ZERO);
    }

    public Panel(ContainerMenu<?> menu, RectD anchor, Rect offset) {
        super(menu, anchor, offset);
    }

    public void addWidget(ContainerWidget widget) {
        this.children.add(widget);
    }

    protected void initChildren() {
        for (var child : this.children) {
            child.init(this.rect);
        }
    }

    @Override
    public void init(Rect parent) {
        super.init(parent);
        this.initChildren();
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }
        for (var child : this.children) {
            child.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        this.hoveringChild = null;
        if (!this.visible) {
            return false;
        }
        for (var child : this.children) {
            if (child.isHovering(mouseX, mouseY)) {
                this.hoveringChild = child;
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<List<Component>> getTooltip() {
        return Optional.ofNullable(this.hoveringChild).flatMap(ContainerWidget::getTooltip);
    }

    @Override
    public boolean isClicking(double mouseX, double mouseY) {
        this.clickingChild = null;
        if (!this.visible) {
            return false;
        }
        for (var child : this.children) {
            if (child.isClicking(mouseX, mouseY)) {
                this.clickingChild = child;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        if (this.clickingChild != null) {
            this.clickingChild.onMouseClicked(mouseX, mouseY, button);
        }
    }
}
