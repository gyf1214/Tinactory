package org.shsts.tinactory.content.gui.client;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.core.gui.client.MenuWidget;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.core.util.I18n;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ResearchBenchScreen extends ProcessingScreen {
    private static class TechButton extends MenuWidget {
        @Nullable
        private ITechnology tech = null;

        public TechButton(IMenu menu) {
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

    public ResearchBenchScreen(IMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        var rect = layout.images.get(0).rect().offset(layout.getXOffset(), 0);
        addWidget(rect, new TechButton(menu));
    }

    public static boolean isHoveringTech(Widget component) {
        return component instanceof TechButton;
    }
}
