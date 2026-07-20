package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import org.shsts.tinactory.content.gui.sync.MECraftCpuSyncPacket;
import org.shsts.tinactory.content.gui.sync.MECraftEventPacket;
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

import static org.shsts.tinactory.AllMenus.ME_CRAFT_ACTION;
import static org.shsts.tinactory.content.gui.client.MECraftTerminalScreen.BUTTON_WIDTH;
import static org.shsts.tinactory.content.gui.client.MECraftTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_HEIGHT;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MECraftCpuStatusPanel extends Panel {
    private class CpuPanel extends MachineSelectPanel {
        private final List<MECraftCpuSyncPacket.CpuInfo> cpus = new ArrayList<>();

        public CpuPanel() {
            super(MECraftCpuStatusPanel.this.screen);
        }

        @Override
        public void clearList() {
            super.clearList();
            cpus.clear();
        }

        public void add(MECraftCpuSyncPacket.CpuInfo info) {
            add(info.status().cpuId(), info.name(), info.icon());
            cpus.add(info);
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            var ret = new ArrayList<Component>();
            var entry = cpus.get(index);
            var status = entry.status();
            ret.add(entry.name());
            ret.add(tr("cpu.state." + status.state().id).copy().withStyle(ChatFormatting.GRAY));
            if (!status.targets().isEmpty()) {
                var target = status.targets().getFirst();
                ret.add(tr("cpu.target", target.key().name(), ClientUtil.getNumberString(target.amount())));
            }
            if (status.state().busy()) {
                ret.add(tr("cpu.steps",
                    NUMBER_FORMAT.format(status.completedSteps()),
                    NUMBER_FORMAT.format(status.totalSteps())).copy().withStyle(ChatFormatting.GRAY));
            }
            ret.add(tr("memory",
                ClientUtil.getBytesString(status.memoryUsage()),
                ClientUtil.getBytesString(status.memoryLimit())).copy().withStyle(ChatFormatting.GRAY));
            if (status.error() != ExecutionError.NONE) {
                ret.add(tr("cpu.error." + status.error().id).copy().withStyle(ChatFormatting.GRAY));
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
    private Consumer<MECraftCpuSyncPacket.CpuInfo> onSelectCpu = null;

    public MECraftCpuStatusPanel(MECraftTerminalScreen screen) {
        super(screen);
        this.cpuPanel = new CpuPanel();
        this.cancelLabel = tr("cancel");
        this.cancelJobLabel = tr("cancelJob");
        this.cancelButton = new VanillaButton(menu, cancelLabel, null, this::cancel);

        addGroup(Rect.corners(2, 2, -2, -BUTTON_HEIGHT - SPACING - 2), cpuPanel);
        addChild(RectD.corners(0d, 1d, 0d, 1d), Rect.corners(0, -BUTTON_HEIGHT, BUTTON_WIDTH, 0), cancelButton);
    }

    public void updateStatus(MECraftCpuSyncPacket packet) {
        cpuPanel.clearList();
        packet.entries().forEach(cpuPanel::add);
        cpuPanel.refreshDisplayMachines();
    }

    public void onSelectCpu(@Nullable Consumer<MECraftCpuSyncPacket.CpuInfo> val) {
        onSelectCpu = val;
        cpuPanel.clearSelect();
        cpuPanel.setSearchQuery("");
        cancelButton.setLabel(val == null ? cancelJobLabel : cancelLabel);
    }

    private void cancel() {
        if (onSelectCpu != null) {
            onSelectCpu.accept(null);
        } else if (cpuPanel.getSelected().isPresent()) {
            var packet = MECraftEventPacket.cancel(cpuPanel.getSelected().get());
            menu.triggerEvent(ME_CRAFT_ACTION, () -> packet);
        }
    }
}
