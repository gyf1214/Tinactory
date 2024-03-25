package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Button extends ContainerWidget {
    private final @Nullable Component tooltip;

    public Button(ContainerMenu<?> menu, Rect rect, @Nullable Component tooltip) {
        super(menu, rect);
        this.tooltip = tooltip;
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip() {
        return Optional.ofNullable(this.tooltip).map(List::of);
    }

    @Override
    protected boolean canClick() {
        return true;
    }

    @Override
    public abstract void onMouseClicked(double mouseX, double mouseY, int button);
}
