package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.content.gui.sync.AutocraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.AutocraftEventPacket;
import org.shsts.tinactory.core.autocraft.api.ExecutionError;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.VanillaButton;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.AllMenus.AUTOCRAFT_TERMINAL_ACTION;
import static org.shsts.tinactory.content.gui.client.AutocraftTerminalScreen.BUTTON_WIDTH;
import static org.shsts.tinactory.content.gui.client.AutocraftTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftCpuStatusPanel extends Panel {
    private class CpuPanel extends MachineSelectPanel {
        private final List<AutocraftCpuSyncPacket.CpuInfo> cpus = new ArrayList<>();

        public CpuPanel() {
            super(AutocraftCpuStatusPanel.this.screen);
        }

        @Override
        public void clearList() {
            super.clearList();
            cpus.clear();
        }

        public void add(AutocraftCpuSyncPacket.CpuInfo info) {
            add(info.status().cpuId(), info.name(), info.icon());
            cpus.add(info);
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            var ret = new ArrayList<Component>();
            var entry = cpus.get(index);
            var status = entry.status();
            ret.add(entry.name());
            ret.add(tr("cpu.state." + status.state().id).withStyle(ChatFormatting.GRAY));
            if (!status.targets().isEmpty()) {
                var target = status.targets().get(0);
                ret.add(tr("cpu.target", target.key().name(), ClientUtil.getNumberString(target.amount())));
            }
            if (status.state().busy()) {
                ret.add(tr("cpu.steps",
                    NUMBER_FORMAT.format(status.completedSteps()),
                    NUMBER_FORMAT.format(status.totalSteps())).withStyle(ChatFormatting.GRAY));
            }
            if (status.error() != ExecutionError.NONE) {
                ret.add(tr("cpu.error." + status.error().id).withStyle(ChatFormatting.GRAY));
            }

            return Optional.of(ret);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (onSelectCpu != null) {
                onSelectCpu.accept(cpus.get(index));
            } else {
                super.onSelect(index, mouseX, mouseY, button);
            }
        }
    }

    private final CpuPanel cpuPanel;
    private final Component cancelLabel;
    private final Component cancelJobLabel;
    private final VanillaButton cancelButton;

    @Nullable
    private Consumer<AutocraftCpuSyncPacket.CpuInfo> onSelectCpu = null;

    public AutocraftCpuStatusPanel(AutocraftTerminalScreen screen) {
        super(screen);
        this.cpuPanel = new CpuPanel();
        this.cancelLabel = tr("cancel");
        this.cancelJobLabel = tr("cancelJob");
        this.cancelButton = new VanillaButton(menu, cancelLabel, null, this::cancel);

        addGroup(Rect.corners(2, 2, -2, -BUTTON_HEIGHT - SPACING - 2), cpuPanel);
        addChild(RectD.corners(1d, 1d, 1d, 1d), Rect.corners(-BUTTON_WIDTH, -BUTTON_HEIGHT, 0, 0), cancelButton);
    }

    public void updateStatus(AutocraftCpuSyncPacket packet) {
        cpuPanel.clearList();
        packet.entries().forEach(cpuPanel::add);
    }

    public void onSelectCpu(@Nullable Consumer<AutocraftCpuSyncPacket.CpuInfo> val) {
        onSelectCpu = val;
        cpuPanel.clearSelect();
        cancelButton.setLabel(val == null ? cancelLabel : cancelJobLabel);
    }

    private void cancel() {
        if (onSelectCpu != null) {
            onSelectCpu.accept(null);
        } else if (cpuPanel.getSelected().isPresent()) {
            var packet = AutocraftEventPacket.cancel(cpuPanel.getSelected().get());
            menu.triggerEvent(AUTOCRAFT_TERMINAL_ACTION, () -> packet);
        }
    }
}
