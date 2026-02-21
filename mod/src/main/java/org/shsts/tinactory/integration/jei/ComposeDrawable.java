package org.shsts.tinactory.integration.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.MethodsReturnNonnullByDefault;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ComposeDrawable implements IDrawable {
    private record ComponentInfo(IDrawable drawable, int xOffset, int yOffset) {}

    private final List<ComponentInfo> components;
    private final int width;
    private final int height;

    private ComposeDrawable(List<ComponentInfo> components) {
        this.components = components;
        int width = 0;
        int height = 0;
        for (var component : components) {
            width = Math.max(width, component.drawable.getWidth() + component.xOffset);
            height = Math.max(height, component.drawable.getHeight() + component.yOffset);
        }
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void draw(PoseStack poseStack, int xOffset, int yOffset) {
        for (var component : components) {
            component.drawable.draw(poseStack, xOffset + component.xOffset, yOffset + component.yOffset);
        }
    }

    public static class Builder {
        private final List<ComponentInfo> components = new ArrayList<>();

        private Builder() {}

        public Builder add(IDrawable component, int xOffset, int yOffset) {
            components.add(new ComponentInfo(component, xOffset, yOffset));
            return this;
        }

        public Builder add(IDrawable component) {
            components.add(new ComponentInfo(component, 0, 0));
            return this;
        }

        public ComposeDrawable build() {
            return new ComposeDrawable(components);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
