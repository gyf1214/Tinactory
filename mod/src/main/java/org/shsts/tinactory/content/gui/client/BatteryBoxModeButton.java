package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.api.machine.IMachineConfigType;
import org.shsts.tinactory.content.electric.BatteryBoxMode;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.gui.client.Button;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.core.util.I18n.tr;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBoxModeButton extends Button {
    private final IMachineConfig config;
    private final IEntry<IMachineConfigType<BatteryBoxMode>> type;
    private final Texture texture;

    public BatteryBoxModeButton(MenuBase menu, IMachineConfig config,
        IEntry<IMachineConfigType<BatteryBoxMode>> type, Texture texture) {
        super(menu);
        this.config = config;
        this.type = type;
        this.texture = texture;
    }

    private BatteryBoxMode mode() {
        return config.get(type).orElse(BatteryBoxMode.BUFFER);
    }

    private void setMode(BatteryBoxMode mode) {
        menu.triggerEvent(SET_MACHINE_CONFIG, SetMachineConfigPacket.builder().set(type, mode));
    }

    @Override
    public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        var current = mode();
        if (current != BatteryBoxMode.CHARGE) {
            var texY = current == BatteryBoxMode.DISCHARGE ? 18 : 0;
            RenderUtil.blit(graphics, texture, rect(), 0, texY);
        }
    }

    @Override
    public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
        return Optional.of(List.of(tr("tinactory.tooltip." + mode().getSerializedName() + "Mode")));
    }

    @Override
    public void onMouseClicked(double mouseX, double mouseY, int button) {
        super.onMouseClicked(mouseX, mouseY, button);
        setMode(mode().next());
    }
}
