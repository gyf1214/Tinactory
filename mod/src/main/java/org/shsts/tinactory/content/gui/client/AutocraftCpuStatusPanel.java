package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.content.gui.sync.AutocraftTerminalCpuStatusSyncSlot;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.Label;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.Widgets;

import java.util.List;
import java.util.OptionalInt;

import static org.shsts.tinactory.core.gui.Menu.EDIT_HEIGHT;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AutocraftCpuStatusPanel extends Panel {
    private final Label title;
    private final Label summary;
    private final EditBox indexInput;

    public AutocraftCpuStatusPanel(AutocraftTerminalScreen screen) {
        super(screen);
        this.title = new Label(menu, new TextComponent("CPU Status"));
        this.summary = new Label(menu, new TextComponent("Select CPU index"));
        this.indexInput = Widgets.editBox();
        indexInput.setValue("");
        var cancelButton = Widgets.simpleButton(menu, new TextComponent("Cancel CPU"), null, screen::cancelCpuJob);

        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(4, 4, -4, 12), title);
        addWidget(RectD.corners(0d, 0d, 1d, 0d), new Rect(4, 20, -4, 26), summary);
        addWidget(RectD.corners(0d, 0d, 0d, 0d), new Rect(4, 50, 56, EDIT_HEIGHT), indexInput);
        addWidget(RectD.corners(1d, 0d, 1d, 0d), new Rect(-88, 48, 84, 20), cancelButton);
    }

    public OptionalInt selectedIndex(int size) {
        try {
            var index = Integer.parseInt(indexInput.getValue());
            return index >= 0 && index < size ? OptionalInt.of(index) : OptionalInt.empty();
        } catch (NumberFormatException ignored) {
            return OptionalInt.empty();
        }
    }

    public void refreshSummary(List<AutocraftTerminalCpuStatusSyncSlot.Row> rows) {
        var index = selectedIndex(rows.size());
        if (index.isEmpty()) {
            summary.setLine(0, new TextComponent("CPU index: select 0.." + Math.max(0, rows.size() - 1)));
            return;
        }
        var row = rows.get(index.getAsInt());
        var blocked = row.blockedReason().isEmpty() ? "-" : row.blockedReason();
        summary.setLine(0, new TextComponent("[" + index.getAsInt() + "] " +
            row.cpuId() + " | " + row.targetSummary() + " | step " + row.currentStep() + " | blocked " + blocked));
    }
}
