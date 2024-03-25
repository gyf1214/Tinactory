package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SwitchButton extends ContainerWidget {
    private final Texture texture;
    private final int syncIndex;
    private final BiConsumer<? extends ContainerMenu<?>, Boolean> onSwitch;

    public SwitchButton(ContainerMenu<?> menu, Rect rect, Texture texture,
                        int syncIndex, BiConsumer<? extends ContainerMenu<?>, Boolean> onSwitch) {
        super(menu, rect);
        this.texture = texture;
        this.syncIndex = syncIndex;
        this.onSwitch = onSwitch;
    }

    private boolean getValue() {
        return this.menu.getSyncPacket(this.syncIndex, ContainerSyncPacket.Boolean.class)
                .map(ContainerSyncPacket.Boolean::getValue).orElse(false);
    }

    @Override
    protected boolean canClick() {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        var newValue = !this.getValue();
        ((BiConsumer<ContainerMenu<?>, Boolean>) this.onSwitch).accept(this.menu, newValue);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.getValue()) {
            RenderUtil.blit(poseStack, this.texture, this.zIndex, this.rect, 0, this.rect.height());
        } else {
            RenderUtil.blit(poseStack, this.texture, this.zIndex, this.rect);
        }
    }
}
