package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternBrowserPanel extends Panel {
    private static final int MAX_DISPLAY_INGREDIENT = 3;

    private final List<CraftPattern> patterns = new ArrayList<>();
    private final Consumer<CraftPattern> onSelectPattern;
    private final Runnable onCreatePattern;

    private class PatternButtonPanel extends ButtonPanel {
        public PatternButtonPanel() {
            super(MEPatternBrowserPanel.this.screen, SLOT_SIZE, SLOT_SIZE, 0);
        }

        @Override
        protected int getItemCount() {
            var count = patterns.size();
            var slotCount = gridViewGroup.getSlotCount();
            return Math.max(1, count / slotCount + 1) * slotCount;
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            RenderUtil.blit(poseStack, SLOT_BACKGROUND, getBlitOffset(), rect, 0, 0);
            if (index >= patterns.size()) {
                return;
            }

            var rect1 = rect.offset(1, 1).resize(SLOT_SIZE - 2, SLOT_SIZE - 2);
            var pattern = patterns.get(index);
            if (pattern.outputs().isEmpty()) {
                RenderUtil.renderItem(new ItemStack(Items.BARRIER), rect1.x(), rect1.y());
                return;
            }
            var display = pattern.outputs().get(0).key().display();
            RenderUtil.renderDescriptor(poseStack, display, rect1, getBlitOffset());
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {
            if (index < patterns.size()) {
                onSelectPattern.accept(patterns.get(index));
            } else {
                onCreatePattern.run();
            }
        }

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return index < patterns.size() ? Optional.of(patternTooltip(patterns.get(index))) :
                Optional.empty();
        }
    }

    private final PatternButtonPanel buttonPanel;

    public MEPatternBrowserPanel(MEPatternTerminalScreen screen,
        Consumer<CraftPattern> onSelectPattern, Runnable onCreatePattern) {
        super(screen);
        this.onSelectPattern = onSelectPattern;
        this.onCreatePattern = onCreatePattern;
        this.buttonPanel = new PatternButtonPanel();
        addGroup(buttonPanel);
    }

    public void updatePatterns(MEPatternSyncPacket packet) {
        patterns.clear();
        patterns.addAll(packet.patterns());
        buttonPanel.refresh();
    }

    private static List<Component> patternTooltip(CraftPattern pattern) {
        var ret = new ArrayList<Component>();
        ret.add(new TextComponent(pattern.patternId()));
        ret.add(tr("input", ingredientsTooltip(pattern.inputs())).withStyle(ChatFormatting.GRAY));
        ret.add(tr("output", ingredientsTooltip(pattern.outputs())).withStyle(ChatFormatting.GRAY));
        return ret;
    }

    private static Component ingredientTooltip(CraftAmount ingredient) {
        return tr("ingredient", ingredient.key().name(), ClientUtil.getNumberString(ingredient.amount()));
    }

    private static Component ingredientsTooltip(List<CraftAmount> ingredients) {
        if (ingredients.isEmpty()) {
            return tr("empty");
        }
        var ret = ingredientTooltip(ingredients.get(0));
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
