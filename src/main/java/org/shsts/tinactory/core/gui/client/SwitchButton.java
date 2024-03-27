package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.BiConsumer;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SwitchButton extends Button {
    private final Texture texture;
    private final int syncSlot;
    private boolean value;
    private final BiConsumer<? extends ContainerMenu<?>, Boolean> onSwitch;

    public SwitchButton(ContainerMenu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, int syncSlot,
                        BiConsumer<? extends ContainerMenu<?>, Boolean> onSwitch) {
        super(menu, rect, tooltip);
        this.texture = texture;
        this.onSwitch = onSwitch;
        this.syncSlot = syncSlot;
    }

    public SwitchButton(ContainerMenu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, boolean initialValue,
                        BiConsumer<? extends ContainerMenu<?>, Boolean> onSwitch) {
        super(menu, rect, tooltip);
        this.texture = texture;
        this.onSwitch = onSwitch;
        this.syncSlot = -1;
        this.value = initialValue;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            this.value = !this.value;
            ((BiConsumer<ContainerMenu<?>, Boolean>) this.onSwitch).accept(this.menu, this.value);
        }
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.syncSlot >= 0) {
            this.menu.getSyncPacket(this.syncSlot, ContainerSyncPacket.Boolean.class)
                    .ifPresent(value -> this.value = value.getValue());
        }
        if (this.value) {
            RenderUtil.blit(poseStack, this.texture, this.zIndex, this.rect, 0, this.rect.height());
        } else {
            RenderUtil.blit(poseStack, this.texture, this.zIndex, this.rect);
        }
    }
}
