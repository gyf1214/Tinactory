package org.shsts.tinactory.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.Rect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiFunction;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public abstract class ContainerWidget extends GuiComponent implements Widget {
    public final Rect rect;
    public final int zIndex;

    protected final ContainerMenu<?> menu;

    public ContainerWidget(ContainerMenu<?> menu, Rect rect, int zIndex) {
        this.menu = menu;
        this.rect = rect;
        this.zIndex = zIndex;
    }

    @Override
    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    public record Builder<M extends ContainerMenu<?>>
            (Rect rect, BiFunction<M, Rect, ContainerWidget> factory) {}
}
