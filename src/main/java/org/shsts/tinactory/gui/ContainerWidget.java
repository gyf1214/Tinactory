package org.shsts.tinactory.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public abstract class ContainerWidget extends GuiComponent implements Widget {
    public final Rect rect;

    public ContainerWidget(int x, int y, int width, int height) {
        this.rect = new Rect(x, y, width, height);
    }

    public ContainerWidget(Rect rect) {
        this.rect = rect;
    }

    @Override
    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    public record Builder(Rect rect, Function<Rect, ContainerWidget> factory) {}
}
