package org.shsts.tinactory.core.gui.client;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Button extends MenuWidget {
    private final @Nullable Component tooltip;

    public Button(Menu<?, ?> menu, @Nullable Component tooltip) {
        super(menu);
        this.tooltip = tooltip;
    }

    public Button(Menu<?, ?> menu) {
        super(menu);
        this.tooltip = null;
    }

    protected void playDownSound() {
        ClientUtil.playSound(SoundEvents.UI_BUTTON_CLICK);
    }

    @Override
    protected boolean canHover() {
        return true;
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
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
