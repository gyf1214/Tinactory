package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.machine.MachineConfig;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.Button;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PortConfigButton extends Button {
    private final MachineConfig machineConfig;
    private final String key;
    protected final PortDirection direction;

    public PortConfigButton(Menu<?, ?> menu, MachineConfig machineConfig,
                            String key, PortDirection direction) {
        super(menu);
        this.machineConfig = machineConfig;
        this.key = key;
        this.direction = direction;
    }

    protected MachineConfig.PortConfig getCurrentConfig() {
        return machineConfig.getPortConfig(key);
    }

    protected MachineConfig.PortConfig getNextConfig(int button) {
        var config = getCurrentConfig();
        var inc = button == 0 ? 1 : 2;
        var nextConfig = MachineConfig.PortConfig.fromIndex((config.index + inc) % 3);
        // Disallow active input request
        if (nextConfig == MachineConfig.PortConfig.ACTIVE && direction != PortDirection.OUTPUT) {
            nextConfig = MachineConfig.PortConfig.NONE;
        }
        return nextConfig;
    }

    @Override
    public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        var config = getCurrentConfig();

        var z = getBlitOffset();
        if (config == MachineConfig.PortConfig.NONE) {
            RenderUtil.blit(poseStack, Texture.CLEAR_GRID_BUTTON, z, rect);
        } else {
            RenderUtil.blit(poseStack, Texture.SWITCH_BUTTON, z, rect);

            var uv = direction == PortDirection.INPUT ?
                    Rect.corners(rect.width(), rect.height(), 0, 0) :
                    new Rect(0, 0, rect.width(), rect.height());
            var color = config == MachineConfig.PortConfig.PASSIVE ? 0xFF5555FF : 0xFFFFAA00;
            RenderUtil.blit(poseStack, Texture.IMPORT_EXPORT_BUTTON, z, color, rect, uv);
        }
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        var subKey = I18n.tr("tinactory.tooltip.portConfig." + direction.name().toLowerCase());
        var tooltip = switch (getCurrentConfig()) {
            case NONE -> I18n.tr("tinactory.tooltip.portConfig.none", subKey);
            case PASSIVE -> I18n.tr("tinactory.tooltip.portConfig.passive", subKey);
            case ACTIVE -> I18n.tr("tinactory.tooltip.portConfig.active", subKey);
        };
        return Optional.of(List.of(tooltip));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);

        var nextConfig = getNextConfig(button);
        var packet = SetMachineConfigPacket.builder().setPort(key, nextConfig);
        menu.triggerEvent(MenuEventHandler.SET_MACHINE_CONFIG, packet);
    }
}
