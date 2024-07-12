package org.shsts.tinactory.content.gui;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.client.TechPanel;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.MenuWidget;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchBenchPlugin implements IMenuPlugin<ProcessingMenu> {
    @OnlyIn(Dist.CLIENT)
    private static class TechButton extends MenuWidget {
        @Nullable
        private ITechnology tech = null;

        public TechButton(Menu<?, ?> menu) {
            super(menu);
        }

        @Override
        public void doRender(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
            var team = TechManager.localTeam();
            tech = team.flatMap(ITeamProfile::getTargetTech).orElse(null);
            if (tech == null) {
                return;
            }
            TechPanel.renderTechButton(poseStack, getBlitOffset(), rect, team.get(), tech, false);
        }

        @Override
        protected boolean canHover() {
            return true;
        }

        @Override
        public Optional<List<Component>> getTooltip(double mouseX, double mouseY) {
            if (tech == null) {
                return Optional.empty();
            }
            return Optional.of(List.of(I18n.tr(tech.getDescriptionId())));
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void applyMenuScreen(MenuScreen<ProcessingMenu> screen) {
        var layout = AllBlockEntities.RESEARCH_BENCH.layout(Voltage.MAXIMUM);
        var rect = layout.images.get(0).rect().offset(layout.getXOffset(), 0);
        screen.addWidget(rect, new TechButton(screen.getMenu()));
    }

    private static final ResearchBenchPlugin INSTANCE = new ResearchBenchPlugin();

    public static IMenuPlugin.Factory<ProcessingMenu> builder() {
        return $ -> INSTANCE;
    }

    public static boolean isHoveringTech(GuiComponent component) {
        return component instanceof TechButton;
    }
}
