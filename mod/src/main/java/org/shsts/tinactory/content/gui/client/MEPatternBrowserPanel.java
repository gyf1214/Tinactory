package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.content.gui.sync.MEPatternSyncPacket;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.integration.gui.client.ButtonPanel;
import org.shsts.tinactory.integration.gui.client.Panel;
import org.shsts.tinactory.integration.gui.client.RenderUtil;
import org.shsts.tinactory.integration.util.ClientUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.shsts.tinactory.content.gui.client.MEPatternTerminalScreen.tr;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.core.gui.Menu.SPACING;
import static org.shsts.tinactory.core.gui.Texture.RECIPE_BUTTON;
import static org.shsts.tinactory.core.gui.Texture.SLOT_BACKGROUND;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEPatternBrowserPanel extends Panel {
    private static final int BUTTON_WIDTH = 88;
    private static final int BUTTON_HEIGHT = 28;
    private static final int TEXT_X = SLOT_SIZE + SPACING * 2;
    private static final int TEXT_SCALE_Y = 15;
    private static final float TEXT_SCALE = 0.5f;

    private final List<CraftPattern> patterns = new ArrayList<>();

    private class PatternButtonPanel extends ButtonPanel {
        public PatternButtonPanel() {
            super(MEPatternBrowserPanel.this.screen, BUTTON_WIDTH, BUTTON_HEIGHT, 1);
        }

        @Override
        protected int getItemCount() {
            var count = patterns.size();
            var slotCount = gridViewGroup.getSlotCount();
            return Math.max(1, (count + slotCount - 1) / slotCount) * slotCount;
        }

        @Override
        protected void renderButton(PoseStack poseStack, int mouseX, int mouseY,
            float partialTick, Rect rect, int index, boolean isHovering) {
            RenderUtil.blit(poseStack, index < patterns.size() ? RECIPE_BUTTON : SLOT_BACKGROUND,
                getBlitOffset(), rect, 0, 0);
            if (index >= patterns.size()) {
                if (index == 0) {
                    RenderUtil.renderText(poseStack, tr("emptyPatternList"),
                        rect.x() + SPACING, rect.y() + TEXT_SCALE_Y, TEXT_SCALE);
                }
                return;
            }

            var pattern = patterns.get(index);
            var output = primaryOutput(pattern);
            var rect1 = rect.offset(1, 1).resize(SLOT_SIZE - 2, SLOT_SIZE - 2);
            RenderUtil.renderDescriptor(poseStack, output.key().display(), rect1, getBlitOffset());
            RenderUtil.renderText(poseStack, fitText(pattern.patternId()), rect.x() + TEXT_X, rect.y() + SPACING,
                TEXT_SCALE);
            RenderUtil.renderText(poseStack, primaryOutputText(output),
                rect.x() + TEXT_X, rect.y() + TEXT_SCALE_Y, TEXT_SCALE);
        }

        @Override
        protected void onSelect(int index, double mouseX, double mouseY, int button) {}

        @Override
        protected Optional<List<Component>> buttonTooltip(int index, double mouseX, double mouseY) {
            return index < patterns.size() ? Optional.of(patternTooltip(patterns.get(index))) : Optional.empty();
        }
    }

    private final PatternButtonPanel buttonPanel = new PatternButtonPanel();

    public MEPatternBrowserPanel(MEPatternTerminalScreen screen) {
        super(screen);
        addGroup(Rect.corners(0, 0, 0, 0), buttonPanel);
    }

    public void updatePatterns(MEPatternSyncPacket packet) {
        patterns.clear();
        patterns.addAll(packet.patterns());
        buttonPanel.refresh();
    }

    private static CraftAmount primaryOutput(CraftPattern pattern) {
        return pattern.outputs().get(0);
    }

    private static Component primaryOutputText(CraftAmount amount) {
        return tr("primaryOutput", amount.key().name(), ClientUtil.getNumberString(amount.amount()));
    }

    private static List<Component> patternTooltip(CraftPattern pattern) {
        var requirement = pattern.machineRequirement();
        var output = primaryOutput(pattern);
        var ret = new ArrayList<Component>();
        ret.add(tr("patternId", pattern.patternId()));
        ret.add(tr("primaryOutput", output.key().name(), ClientUtil.getNumberString(output.amount())));
        output.key().tooltip().ifPresent(ret::addAll);
        ret.add(tr("recipeType", requirement.recipeTypeId().toString()).withStyle(ChatFormatting.GRAY));
        ret.add(tr("voltageTier", voltageName(requirement.voltageTier())).withStyle(ChatFormatting.GRAY));
        ret.add(tr("inputCount", pattern.inputs().size()).withStyle(ChatFormatting.GRAY));
        ret.add(tr("outputCount", pattern.outputs().size()).withStyle(ChatFormatting.GRAY));
        return ret;
    }

    private static String voltageName(int rank) {
        try {
            return Voltage.fromRank(rank).displayName();
        } catch (NoSuchElementException ex) {
            return Integer.toString(rank);
        }
    }

    private static TextComponent fitText(String text) {
        var maxWidth = (int) ((BUTTON_WIDTH - TEXT_X - SPACING) / TEXT_SCALE);
        var font = ClientUtil.getFont();
        if (font.width(text) <= maxWidth) {
            return new TextComponent(text);
        }
        return new TextComponent(font.plainSubstrByWidth(text, maxWidth - font.width("...")) + "...");
    }
}
