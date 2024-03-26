package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class Panel extends ContainerWidget {
    private final List<ContainerWidget> children = new ArrayList<>();
    @Nullable
    private ContainerWidget hoveringChild = null;
    @Nullable
    private ContainerWidget clickingChild = null;

    public Panel(ContainerMenu<?> menu, int x, int y) {
        super(menu, new Rect(x, y, 0, 0));
    }

    public void addWidget(ContainerWidget widget) {
        this.children.add(widget);
    }

    @Override
    public void init(int parentX, int parentY) {
        super.init(parentX, parentY);
        for (var child : this.children) {
            child.init(this.rect.x(), this.rect.y());
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        for (var child : this.children) {
            child.render(poseStack, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public boolean isHovering(double mouseX, double mouseY) {
        this.hoveringChild = null;
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
