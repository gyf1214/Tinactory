package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.content.gui.ResearchBenchPlugin;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen1;
import org.shsts.tinactory.core.tech.TechManager;
import org.shsts.tinactory.integration.jei.JEI;
import org.shsts.tinactory.integration.jei.ingredient.TechWrapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreenHandler1 implements IGuiContainerHandler<MenuScreen1<?>> {
    private final JEI plugin;
    private final Map<Class<? extends Menu<?, ?>>, IMenuScreenClickableProvider<?>> clickableHandlers;

    public MenuScreenHandler1(JEI plugin) {
        this.plugin = plugin;
        this.clickableHandlers = new HashMap<>();
        clickableHandler(ProcessingMenu.class, this::processingClickable);
    }

    private <M extends Menu<?, M>> void clickableHandler(Class<M> clazz,
        IMenuScreenClickableProvider<M> handler) {
        clickableHandlers.put(clazz, handler);
    }

    @Override
    public @Nullable Object getIngredientUnderMouse(MenuScreen1<?> screen, double mouseX, double mouseY) {
        var hovered = screen.getHovered((int) mouseX, (int) mouseY);
        if (hovered.isEmpty()) {
            return null;
        }
        if (hovered.get() instanceof FluidSlot slot) {
            var stack = slot.getFluidStack();
            return stack.isEmpty() ? null : stack;
        } else if (ResearchBenchPlugin.isHoveringTech(hovered.get())) {
            return TechManager.localTeam()
                .flatMap(ITeamProfile::getTargetTech)
                .map(tech -> new TechWrapper(tech.getLoc()))
                .orElse(null);
        }
        return null;
    }

    private Optional<IGuiClickableArea> processingClickable(MenuScreen1<ProcessingMenu> screen) {
        var menu = screen.getMenu();

        var category = menu.getRecipeType().flatMap(plugin::processingCategory);
        var layout = menu.layout;
        if (category.isEmpty() || layout == null || layout.progressBar == null) {
            return Optional.empty();
        }

        var rect = layout.progressBar.rect();
        var x = rect.x() + layout.getXOffset() + MARGIN_HORIZONTAL;
        var y = rect.y() + MARGIN_TOP;
        var w = rect.width();
        var h = rect.height();

        return Optional.of(IGuiClickableArea.createBasic(x, y, w, h, category.get().type));
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(MenuScreen1<?> screen,
        double mouseX, double mouseY) {
        var ret = new ArrayList<IGuiClickableArea>();
        var menu = screen.getMenu();
        for (var entry : clickableHandlers.entrySet()) {
            if (entry.getKey().isInstance(menu)) {
                entry.getValue().guiClickableAreas(screen).ifPresent(ret::add);
            }
        }
        return ret;
    }
}
