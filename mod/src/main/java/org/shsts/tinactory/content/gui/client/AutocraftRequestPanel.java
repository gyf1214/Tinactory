package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.autocraft.model.CraftKey;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.Widgets;

import java.util.List;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftRequestPanel extends Panel {
    private final Label title;
    private final Label targetSummary;
    private final Label cpuSummary;
    private final EditBox quantityInput;
    private final EditBox targetIndexInput;
    private final EditBox cpuIndexInput;

    public AutocraftRequestPanel(AutocraftTerminalScreen screen) {
        super(screen);
        this.title = new Label(menu, new TextComponent("Autocraft Request"));
        this.targetSummary = new Label(menu, new TextComponent("Target: not selected"));
        this.cpuSummary = new Label(menu, new TextComponent("CPU: not selected"));
        this.quantityInput = Widgets.editBox();
        this.targetIndexInput = Widgets.editBox();
        this.cpuIndexInput = Widgets.editBox();
        quantityInput.setValue("1");
        targetIndexInput.setValue("");
        cpuIndexInput.setValue("");
        var previewButton = Widgets.simpleButton(menu, new TextComponent("Preview"), null, screen::requestPreview);

        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(4, 4, -4, 12), title);
        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(4, 18, -4, 12), targetSummary);
        addWidget(RectD.corners(0d, 0d, 0d, 0d), new Rect(4, 32, 48, EDIT_HEIGHT), targetIndexInput);
        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(56, 36, -4, 12), cpuSummary);
        addWidget(RectD.corners(0d, 0d, 0d, 0d), new Rect(4, 46, 48, EDIT_HEIGHT), cpuIndexInput);
        addWidget(RectD.corners(1d, 0d, 1d, 0d), new Rect(-124, 44, 48, EDIT_HEIGHT), quantityInput);
        addWidget(RectD.corners(1d, 0d, 1d, 0d), new Rect(-72, 42, 68, 20), previewButton);
    }

    public OptionalLong quantity() {
        try {
            var value = Long.parseLong(quantityInput.getValue());
            return value > 0L ? OptionalLong.of(value) : OptionalLong.empty();
        } catch (NumberFormatException ignored) {
            return OptionalLong.empty();
        }
    }

    public OptionalInt targetIndex(int size) {
        return parseIndex(targetIndexInput.getValue(), size);
    }

    public OptionalInt cpuIndex(int size) {
        return parseIndex(cpuIndexInput.getValue(), size);
    }

    public void updateSelectionSummary(List<CraftKey> requestables, List<UUID> cpus) {
        targetSummary.setLine(0, new TextComponent(formatTargetSummary(requestables)));
        cpuSummary.setLine(0, new TextComponent(formatCpuSummary(cpus)));
    }

    public void setTitle(Component component) {
        title.setLine(0, component);
    }

    private static OptionalInt parseIndex(String raw, int size) {
        try {
            var index = Integer.parseInt(raw);
            return index >= 0 && index < size ? OptionalInt.of(index) : OptionalInt.empty();
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    private String formatTargetSummary(List<CraftKey> requestables) {
        var index = targetIndex(requestables.size());
        if (index.isEmpty()) {
            return "Target index: select 0.." + Math.max(0, requestables.size() - 1);
        }
        var key = requestables.get(index.getAsInt());
        return "Target[" + index.getAsInt() + "]: " + key.id();
    }

    private String formatCpuSummary(List<UUID> cpus) {
        var index = cpuIndex(cpus.size());
        if (index.isEmpty()) {
            return "CPU index: select 0.." + Math.max(0, cpus.size() - 1);
        }
        return "CPU[" + index.getAsInt() + "]: " + cpus.get(index.getAsInt());
    }
}
