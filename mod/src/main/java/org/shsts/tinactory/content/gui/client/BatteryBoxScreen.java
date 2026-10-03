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
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.integration.gui.LayoutMenu;
import org.shsts.tinactory.integration.gui.client.Button;
import org.shsts.tinactory.integration.gui.client.LayoutScreen;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinycorelib.api.gui.MenuBase;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;

import java.util.List;
import java.util.Optional;

import static org.shsts.tinactory.AllCapabilities.MACHINE;
import static org.shsts.tinactory.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.AllNetworks.BATTERY_MODE;
import static org.shsts.tinactory.content.electric.BatteryBox.MODE_DEFAULT;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.BATTERY_MODE_BUTTON;
import static org.shsts.tinactory.core.util.I18n.tr;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryBoxScreen extends LayoutScreen<LayoutMenu> {
    public static class BatteryBoxModeButton extends Button {
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
            return config.get(type).orElse(MODE_DEFAULT);
        }

        private void setMode(BatteryBoxMode mode) {
            menu.triggerEvent(SET_MACHINE_CONFIG, SetMachineConfigPacket.builder().set(type, mode));
        }

        @Override
        public void doRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            var texY = switch (mode()) {
                case BUFFER -> 0;
                case DISCHARGE -> 18;
                case CHARGE -> 36;
            };
            RenderUtil.blit(graphics, texture, rect(), 0, texY);
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

    public BatteryBoxScreen(LayoutMenu menu, Component title) {
        super(menu, title);

        addProgressBar();

        var config = MACHINE.get(menu.blockEntity()).config();
        var buttonY = menu.layout().rect.endY() + SPACING;
        var button = new BatteryBoxModeButton(menu, config, BATTERY_MODE, BATTERY_MODE_BUTTON);
        rootPanel.addChild(RectD.corners(1d, 0d, 1d, 0d),
            new Rect(-SLOT_SIZE, buttonY, SLOT_SIZE, SLOT_SIZE), button);
    }
}
