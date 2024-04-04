package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Button extends ContainerWidget {
    private final @Nullable Component tooltip;

    public Button(ContainerMenu<?> menu, RectD anchor, Rect offset, @Nullable Component tooltip) {
        super(menu, anchor, offset);
        this.tooltip = tooltip;
    }

    public Button(ContainerMenu<?> menu, Rect rect, @Nullable Component tooltip) {
        super(menu, rect);
        this.tooltip = tooltip;
    }

    protected void playDownSound() {
        ClientUtil.playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip() {
        return Optional.ofNullable(tooltip).map(List::of);
    }

    @Override
    protected boolean canClick(int button) {
        return button == 0;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        playDownSound();
    }
}
