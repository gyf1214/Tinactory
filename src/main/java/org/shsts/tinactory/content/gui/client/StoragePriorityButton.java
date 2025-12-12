package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.util.MathUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.util.I18n.tr;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StoragePriorityButton extends Button {
    private static final String DISABLE_TOOLTIP = "tinactory.tooltip.chestNotStorage";
    private static final String PRIORITY_TOOLTIP = "tinactory.tooltip.chestStorage.";

    private final IMachineConfig config;
    private final String key;
    private final int defaultVal;

    public StoragePriorityButton(MenuBase menu, IMachineConfig config,
        String key, int defaultVal) {
        super(menu);
        this.config = config;
        this.key = key;
        this.defaultVal = defaultVal;
    }

    private int getValue() {
        return MathUtil.clamp(config.getInt(key, defaultVal), -1, 4);
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var z = getBlitOffset();
        RenderUtil.blit(poseStack, Texture.SWITCH_BUTTON, z, rect);
        var x = (getValue() + 1) * SLOT_SIZE;
        RenderUtil.blit(poseStack, Texture.PRIORITY_OVERLAY, z, rect, x, 0);
    }

    @Override
    protected boolean canClick(int button, double mouseX, double mouseY) {
        return button == 0 || button == 1;
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        int val2;
        if (button == 0) {
            var val1 = getValue() + 1;
            val2 = val1 > 4 ? -1 : val1;
        } else {
            var val1 = getValue() - 1;
            val2 = val1 < -1 ? 4 : val1;
        }
        menu.triggerEvent(SET_MACHINE_CONFIG, SetMachineConfigPacket.builder().set(key, val2));
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        var val = getValue();
        if (val >= 0) {
            return Optional.of(List.of(tr(PRIORITY_TOOLTIP + val)));
        } else {
            return Optional.of(List.of(tr(DISABLE_TOOLTIP)));
        }
    }
}
