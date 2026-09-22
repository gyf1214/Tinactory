package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.gui.client.SearchBox;
import org.shsts.tinactory.integration.logistics.StackHelper;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.content.gui.client.StoragePanel.BUTTON_OFFSET;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;
import static org.shsts.tinactory.integration.gui.client.SearchBox.SEARCH_ANCHOR;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternBrowserPanel extends ButtonPanel {
    private static final int MAX_DISPLAY_INGREDIENT = 3;

    private final List<CraftPattern> patterns = new ArrayList<>();
    private final List<CraftPattern> displayPatterns = new ArrayList<>();
    private final SearchBox searchBox;
    private final Consumer<CraftPattern> onSelectPattern;
    private final Runnable onCreatePattern;

    public MEPatternBrowserPanel(MEPatternTerminalScreen screen,
        Consumer<CraftPattern> onSelectPattern, Runnable onCreatePattern) {
        super(screen, SLOT_SIZE, SLOT_SIZE, 0, BUTTON_OFFSET, true);
        this.searchBox = SearchBox.light(screen, this::refreshDisplayEntries);
        this.onSelectPattern = onSelectPattern;
        this.onCreatePattern = onCreatePattern;

        addChild(SEARCH_ANCHOR, Rect.ZERO, searchBox);
    }

    @Override
    protected int getItemCount() {
        var slotCount = gridViewGroup.getSlotCount();
        return Math.max(1, (displayPatterns.size() + slotCount) / slotCount) * slotCount;
    }

    @Override
    protected void renderButton(GuiGraphics graphics, int mouseX, int mouseY,
        float partialTick, Rect rect, int index, boolean isHovering) {
        RenderUtil.blit(graphics, SLOT_BACKGROUND, rect, 0, 0);
        if (index >= displayPatterns.size()) {
            return;
        }

        var rect1 = rect.offset(1, 1).resize(16, 16);
        var pattern = displayPatterns.get(index);
        if (pattern.outputs().isEmpty()) {
            RenderUtil.renderItem(graphics, new ItemStack(Items.BARRIER), rect1);
            return;
        }
        var display = pattern.outputs().getFirst().key().display();
        RenderUtil.renderDescriptor(graphics, display, rect1);
    }

    @Override
    protected void onSelect(int index, double mouseX, double mouseY, int button) {
        if (index < displayPatterns.size()) {
            onSelectPattern.accept(displayPatterns.get(index));
        } else {
            onCreatePattern.run();
        }
    }

    @Override
    protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
        if (index >= displayPatterns.size()) {
            return Optional.empty();
        }

        var pattern = displayPatterns.get(index);
        var ret = new ArrayList<Component>();
        ret.add(tr("input", ingredientsTooltip(pattern.inputs())).withStyle(ChatFormatting.GRAY));
        ret.add(tr("output", ingredientsTooltip(pattern.outputs())).withStyle(ChatFormatting.GRAY));
        return Optional.of(ret);
    }

    private void refreshDisplayEntries(String query) {
        displayPatterns.clear();
        patterns.stream()
            .filter(pattern -> StackHelper.matchText(query, pattern.outputs().getFirst().key()))
            .forEach(displayPatterns::add);
        refresh();
    }

    public void updatePatterns(MEPatternSyncPacket packet) {
        patterns.clear();
        patterns.addAll(packet.patterns());
        refreshDisplayEntries(searchBox.getValue());
    }

    private static MutableComponent ingredientTooltip(CraftAmount ingredient) {
        return tr("ingredient", ingredient.key().name(), ClientUtil.getNumberString(ingredient.amount()));
    }

    private static MutableComponent ingredientsTooltip(List<CraftAmount> ingredients) {
        if (ingredients.isEmpty()) {
            return tr("empty");
        }
        var ret = ingredientTooltip(ingredients.getFirst());
        for (var i = 1; i < ingredients.size(); i++) {
            if (i >= MAX_DISPLAY_INGREDIENT) {
                ret = tr("ingredients", ret, tr("ellipsis"));
                break;
            }
            ret = tr("ingredients", ret, ingredientTooltip(ingredients.get(i)));
        }
        return ret;
    }
}
