package org.shsts.tinactory.core.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;

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
    private final BiConsumer<? extends Menu<?>, Boolean> onSwitch;

    public SwitchButton(Menu<?> menu, Rect rect, Texture texture,
                        @Nullable Component tooltip, int syncSlot,
                        BiConsumer<? extends Menu<?>, Boolean> onSwitch) {
        super(menu, rect, tooltip);
        this.texture = texture;
        this.onSwitch = onSwitch;
        this.syncSlot = syncSlot;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        value = !value;
        ((BiConsumer<Menu<?>, Boolean>) onSwitch).accept(menu, value);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (syncSlot >= 0) {
            menu.getSyncPacket(syncSlot, MenuSyncPacket.Boolean.class)
                    .ifPresent(packet -> value = packet.getValue());
        }
        if (value) {
            RenderUtil.blit(poseStack, texture, zIndex, rect, 0, rect.height());
        } else {
            RenderUtil.blit(poseStack, texture, zIndex, rect);
        }
    }
}
