package org.shsts.tinactory.content.gui.client;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.MenuScreen;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.StretchImage;
import org.shsts.tinactory.integration.gui.client.Widgets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.BUTTON_TOP_MARGIN;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.SEARCH_BOX_ANCHOR;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.SEARCH_BOX_MARGIN;
import static org.shsts.tinactory.content.gui.client.MachineRecipeBook.SEARCH_SIZE;
import static org.shsts.tinactory.core.gui.Menu.BUTTON_SIZE;
import static org.shsts.tinactory.core.gui.Menu.FONT_HEIGHT;
import static org.shsts.tinactory.core.gui.Menu.PANEL_BORDER;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BOOK_BG;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;
import static org.shsts.tinactory.integration.gui.client.Widgets.BUTTON_PANEL_BG;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSelectPanel<T> extends ButtonPanel {
    private static final Rect BUTTON_OFFSET = PAGE_PANEL_OFFSET
        .offset(0, BUTTON_TOP_MARGIN).enlarge(0, -BUTTON_TOP_MARGIN);
    private static final int SEARCH_BOX_Y = SEARCH_BOX_MARGIN + SPACING;
    private static final Rect SEARCH_BOX_OFFSET = Rect.corners(SEARCH_SIZE + SPACING * 2,
        SEARCH_BOX_Y, -4, SEARCH_BOX_Y + FONT_HEIGHT);

    private record MachineInfo<T>(UUID id, Component name, ItemStack icon, @Nullable T extra) {
        public boolean matchSearch(String query) {
            if (query.isEmpty()) {
                return true;
            }
            var query1 = query.toLowerCase(Locale.ROOT);
            return I18n.flattenComponent(name).contains(query1) ||
                (!icon.isEmpty() && I18n.flattenComponent(icon.getHoverName()).contains(query1));
        }
    }

    private final EditBox searchBox;
    private final List<MachineInfo<T>> machineList = new ArrayList<>();
    private final List<MachineInfo<T>> displayMachineList = new ArrayList<>();
    private final HashSet<UUID> machines = new HashSet<>();

    @Nullable
    private UUID selected = null;

    public MachineSelectPanel(MenuScreen<?> screen) {
        super(screen, BUTTON_SIZE, BUTTON_SIZE, 1, BUTTON_OFFSET);
        var bg = new StretchImage(menu, RECIPE_BOOK_BG, BUTTON_PANEL_BG, PANEL_BORDER);
        var icon = Widgets.searchIcon(menu);
        this.searchBox = Widgets.searchBox(this::refreshDisplayMachines);

        addChild(RectD.FULL, Rect.corners(-2, -2, 2, 2), -5, bg);
        addChild(new Rect(SPACING, SPACING, SEARCH_SIZE, SEARCH_SIZE), icon);
        addVanillaWidget(SEARCH_BOX_ANCHOR, SEARCH_BOX_OFFSET, 0, searchBox);
    }

    public Optional<UUID> getSelected() {
        return Optional.ofNullable(selected);
    }

    public void clearSelect() {
        selected = null;
    }

    public void select(UUID machine) {
        selected = machines.contains(machine) ? machine : null;
    }

    public void clearList() {
        machineList.clear();
        machines.clear();
    }

    public void add(UUID machine, Component name, ItemStack icon, @Nullable T extra) {
        machineList.add(new MachineInfo<>(machine, name, icon, extra));
        machines.add(machine);
    }

    public void add(UUID machine, Component name, ItemStack icon) {
        add(machine, name, icon, null);
    }

    private void refreshDisplayMachines(String query) {
        displayMachineList.clear();
        machineList.stream().filter($ -> $.matchSearch(query)).forEach(displayMachineList::add);
        refresh();
    }

    protected void refreshDisplayMachines() {
        refreshDisplayMachines(searchBox.getValue());
    }

    protected void setSearchQuery(String query) {
        searchBox.setValue(query);
    }

    @Override
    protected int getItemCount() {
        return displayMachineList.size();
    }

    @Nullable
    protected T getExtra(int index) {
        return displayMachineList.get(index).extra;
    }

    @Override
    protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
        float partialTick, Rect rect, int index, boolean isHovering) {
        var machine = displayMachineList.get(index);
        RenderUtil.blit(graphics, RECIPE_BUTTON, rect,
            machine.id.equals(selected) ? 21 : 0, 0);
        RenderUtil.renderItem(graphics, machine.icon, rect.x() + 2, rect.y() + 2);
    }

    @Override
    protected void onSelect(int index, double mouseX, double mouseY, int button) {
        select(displayMachineList.get(index).id);
    }

    @Override
    protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
        return Optional.of(List.of(displayMachineList.get(index).name));
    }

    @Override
    protected void doRefresh() {
        super.doRefresh();
        if (selected != null) {
            select(selected);
        }
    }
}
