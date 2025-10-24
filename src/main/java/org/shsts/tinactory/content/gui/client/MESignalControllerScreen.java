package org.shsts.tinactory.content.gui.client;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachineConfig;
import org.shsts.tinactory.content.gui.MESignalControllerMenu;
import org.shsts.tinactory.content.gui.sync.MESignalControllerSyncPacket;
import org.shsts.tinactory.content.gui.sync.SetMachineConfigPacket;
import org.shsts.tinactory.content.logistics.SignalConfig;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;
import org.shsts.tinactory.core.util.I18n;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.content.AllMenus.SET_MACHINE_CONFIG;
import static org.shsts.tinactory.content.gui.MESignalControllerMenu.SIGNAL_SYNC;
import static org.shsts.tinactory.content.logistics.MESignalController.SIGNAL_CONFIG_KEY;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;
import static org.shsts.tinactory.core.gui.Menu.PANEL_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.PANEL_WIDTH;
import static org.shsts.tinactory.core.gui.Menu.PORT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.PORT_PADDING_ICON;
import static org.shsts.tinactory.core.gui.Menu.PORT_PADDING_TEXT;
import static org.shsts.tinactory.core.gui.Menu.PORT_TEXT_COLOR;
import static org.shsts.tinactory.core.gui.Texture.INPUT_OUTPUT_OVERLAY;
import static org.shsts.tinactory.core.gui.Texture.SWITCH_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MESignalControllerScreen extends MenuScreen<MESignalControllerMenu> {
    private static final int SIGNAL_WIDTH = 64;
    private static final int WIDTH = PANEL_WIDTH + SIGNAL_WIDTH + MARGIN_X;
    private static final int HEIGHT = PANEL_HEIGHT;

    private final IMachineConfig machineConfig;
    private final ListMultimap<UUID, MESignalControllerSyncPacket.SignalInfo> machineSignals =
        ArrayListMultimap.create();

    private class SignalSelectPanel extends ButtonPanel {
        public SignalSelectPanel() {
            super(MESignalControllerScreen.this, SIGNAL_WIDTH, PORT_HEIGHT, 0);
        }

        @Override
        protected int getItemCount() {
            return machinePanel.getSelected()
                .map($ -> machineSignals.get($).size())
                .orElse(0);
        }

        private boolean isSelected(MESignalControllerSyncPacket.SignalInfo info) {
            return getConfig()
                .filter($ -> $.machine().equals(info.machineId()) &&
                    $.key().equals(info.key()))
                .isPresent();
        }

        private Component signalName(String key) {
            return I18n.tr("tinactory.gui.signal." + key);
        }

        private Optional<MESignalControllerSyncPacket.SignalInfo> getInfo(int index) {
            return machinePanel.getSelected()
                .flatMap(selected -> {
                    if (!machineSignals.containsKey(selected)) {
                        return Optional.empty();
                    }
                    var infos = machineSignals.get(selected);
                    return index >= infos.size() ? Optional.empty() : Optional.of(infos.get(index));
                });
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick,
            Rect rect, int index, boolean isHovering) {
            getInfo(index).ifPresent(info -> {
                var z = getBlitOffset();

                var bgW = SWITCH_BUTTON.width();
                var bgH = SWITCH_BUTTON.height() / 2;
                var bg = new Rect(0, isSelected(info) ? bgH : 0, bgW, bgH);
                StretchImage.render(poseStack, SWITCH_BUTTON, z, rect, bg, 3);

                var iconW = INPUT_OUTPUT_OVERLAY.width();
                var iconH = INPUT_OUTPUT_OVERLAY.height() / 2;
                var icon = new Rect(0, info.isWrite() ? 0 : iconH, iconW, iconH);
                RenderUtil.blit(poseStack, INPUT_OUTPUT_OVERLAY, z,
                    rect.offset(0, PORT_PADDING_ICON).resize(iconW, iconH), icon);

                RenderUtil.renderText(poseStack, signalName(info.key()),
                    rect.x() + iconW, rect.y() + PORT_PADDING_TEXT + 1,
                    PORT_TEXT_COLOR);
            });
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            getInfo(index).ifPresent(info -> {
                var config = new SignalConfig(info.machineId(), info.key());
                var packet = SetMachineConfigPacket.builder()
                    .set(SIGNAL_CONFIG_KEY, config.toTag());
                menu.triggerEvent(SET_MACHINE_CONFIG, packet);
            });
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return getInfo(index).map(info -> {
                var line1 = signalName(info.key());
                var line2 = tr(info.isWrite() ? "writeSignal" : "readSignal")
                    .withStyle(ChatFormatting.GRAY);
                return List.of(line1, line2);
            });
        }
    }

    private final MachineSelectPanel machinePanel;
    private final SignalSelectPanel signalPanel;

    private static MutableComponent tr(String key) {
        return I18n.tr("tinactory.gui.signalController." + key);
    }

    public MESignalControllerScreen(MESignalControllerMenu menu, Component title) {
        super(menu, title);
        this.contentWidth = WIDTH;
        this.contentHeight = HEIGHT;
        this.machineConfig = menu.machine.config();

        this.machinePanel = new MachineSelectPanel(this) {
            @Override
            public void select(UUID machine) {
                super.select(machine);
                signalPanel.refresh();
            }
        };
        this.signalPanel = new SignalSelectPanel();

        var offset1 = Rect.corners(1, 1, -SIGNAL_WIDTH - MARGIN_X - 1, -1);
        var offset2 = Rect.corners(-SIGNAL_WIDTH, 0, 0, 0);

        addPanel(offset1, machinePanel);
        addPanel(RectD.corners(1d, 0d, 1d, 1d), offset2, signalPanel);

        menu.onSyncPacket(SIGNAL_SYNC, this::refreshVisibleSignals);
    }

    private void refreshVisibleSignals(MESignalControllerSyncPacket p) {
        machineSignals.clear();
        machinePanel.clearList();

        for (var signal : p.signals()) {
            if (!machineSignals.containsKey(signal.machineId())) {
                machinePanel.add(signal.machineId(), signal.machineName(), signal.icon());
            }
            machineSignals.put(signal.machineId(), signal);
        }

        for (var machine : machineSignals.keySet()) {
            var l = machineSignals.get(machine);
            l.sort(Comparator.comparing(MESignalControllerSyncPacket.SignalInfo::key));
        }

        machinePanel.refresh();
        signalPanel.refresh();

        getConfig().ifPresent($ -> machinePanel.select($.machine()));
    }

    private Optional<SignalConfig> getConfig() {
        return machineConfig.getCompound(SIGNAL_CONFIG_KEY)
            .map(SignalConfig::fromTag);
    }
}
