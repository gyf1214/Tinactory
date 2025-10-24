package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.client.ButtonPanel;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.RenderUtil;
import org.shsts.tinactory.core.gui.client.StretchImage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.core.gui.Menu.BUTTON_PANEL_BG;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.PANEL_BORDER;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSelectPanel extends ButtonPanel {
    private record MachineInfo(UUID id, Component name, ItemStack icon) {}

    private final List<MachineInfo> machineList = new ArrayList<>();
    private final HashSet<UUID> machines = new HashSet<>();

    @Nullable
    private UUID selected = null;

    public MachineSelectPanel(MenuScreen<?> screen) {
        super(screen, BUTTON_SIZE, BUTTON_SIZE, 1);
        var bg = new StretchImage(menu, RECIPE_BOOK_BG, BUTTON_PANEL_BG, PANEL_BORDER);
        addWidget(RectD.FULL, Rect.corners(-2, -2, 2, 2), bg);
    }

    public Optional<UUID> getSelected() {
        return Optional.ofNullable(selected);
    }

    public void select(UUID machine) {
        selected = machines.contains(machine) ? machine : null;
    }

    public void clearList() {
        machineList.clear();
        machines.clear();
    }

    public void add(UUID machine, Component name, ItemStack icon) {
        machineList.add(new MachineInfo(machine, name, icon));
        machines.add(machine);
    }

    @Override
    protected int getItemCount() {
        return machineList.size();
    }

    @Override
    protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
        float partialTick, Rect rect, int index, boolean isHovering) {
        var machine = machineList.get(index);
        RenderUtil.blit(poseStack, RECIPE_BUTTON, getBlitOffset(), rect,
            machine.id.equals(selected) ? 21 : 0, 0);
        RenderUtil.renderItem(machine.icon, rect.x() + 2, rect.y() + 2);
    }

    @Override
    protected void onSelect(int index, double mouseX, double mouseY, int button) {
        select(machineList.get(index).id);
    }

    @Override
    protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
        return Optional.of(List.of(machineList.get(index).name));
    }

    @Override
    protected void doRefresh() {
        super.doRefresh();
        if (selected != null) {
            select(selected);
        }
    }
}
