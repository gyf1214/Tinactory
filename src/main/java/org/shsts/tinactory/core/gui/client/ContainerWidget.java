package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ContainerWidget extends GuiComponent implements Widget {
    protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    public final Rect rect;
    public final int zIndex;

    protected final ContainerMenu<?> menu;

    public ContainerWidget(ContainerMenu<?> menu, Rect rect, int zIndex) {
        this.menu = menu;
        this.rect = rect;
        this.zIndex = zIndex;
    }

    public ContainerWidget(ContainerMenu<?> menu, Rect rect) {
        this(menu, rect, ContainerMenu.DEFAULT_Z_INDEX);
    }

    @Override
    public abstract void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick);

    protected boolean canHover() {
        return false;
    }

    public boolean isHovering(double mouseX, double mouseY) {
        return this.canHover() && this.rect.in(mouseX, mouseY);
    }

    public Optional<List<Component>> getTooltip() {
        return Optional.empty();
    }

    protected boolean canClick() {
        return false;
    }

    public boolean isClicking(double mouseX, double mouseY) {
        return this.canClick() && this.rect.in(mouseX, mouseY);
    }

    /**
     * Do not consider drag now.
     */
    public void onMouseClicked(double mouseX, double mouseY, int button) {}

    public record Builder<M extends ContainerMenu<?>>
            (Rect rect, BiFunction<M, Rect, ContainerWidget> factory) {}
}
