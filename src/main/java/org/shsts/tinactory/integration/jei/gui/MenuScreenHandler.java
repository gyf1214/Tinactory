package org.shsts.tinactory.integration.jei.gui;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.AllBlockEntities;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.integration.jei.JEI;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_HORIZONTAL;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuScreenHandler implements IGuiContainerHandler<MenuScreen<?>> {
    private final JEI plugin;
    private final Map<Class<? extends Menu<?, ?>>, IMenuScreenClickableProvider<?>> clickableHandlers;

    public MenuScreenHandler(JEI plugin) {
        this.plugin = plugin;
        this.clickableHandlers = new HashMap<>();
        clickableHandler(ProcessingMenu.class, this::processingClickable);

        var x = AllLayouts.WORKBENCH.getXOffset() + 5 * SLOT_SIZE;
        var workbenchArea = IGuiClickableArea.createBasic(x, SLOT_SIZE + MARGIN_TOP,
                SLOT_SIZE, SLOT_SIZE, plugin.toolCategory.type);

        clickableHandler(WorkbenchMenu.class, screen -> Optional.of(workbenchArea));
    }

    private <M extends Menu<?, M>>
    void clickableHandler(Class<M> clazz, IMenuScreenClickableProvider<M> handler) {
        clickableHandlers.put(clazz, handler);
    }

    @Override
    public @Nullable Object getIngredientUnderMouse(MenuScreen<?> screen, double mouseX, double mouseY) {
        var hovered = screen.getHovered((int) mouseX, (int) mouseY);
        if (hovered.isPresent() && hovered.get() instanceof FluidSlot slot) {
            var stack = slot.getFluidStack();
            return stack.isEmpty() ? null : stack;
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    private Optional<IGuiClickableArea> processingClickable(MenuScreen<ProcessingMenu> screen) {
        var menu = screen.getMenu();
        var holder = menu.blockEntity.getBlockState().getBlock().asItem().builtInRegistryHolder();
        if (!holder.is(AllTags.MACHINE)) {
            return Optional.empty();
        }
        var layout = menu.layout;
        if (layout == null || layout.progressBar == null) {
            return Optional.empty();
        }
        var rect = layout.progressBar.rect();

        var x = rect.x() + layout.getXOffset() + MARGIN_HORIZONTAL;
        var y = rect.y() + MARGIN_TOP;
        var w = rect.width();
        var h = rect.height();
        for (var set : AllBlockEntities.PROCESSING_SETS) {
            if (holder.is(AllTags.machineTag(set.recipeType))) {
                var category = plugin.processingCategory(set.recipeType).type;
                return Optional.of(IGuiClickableArea.createBasic(x, y, w, h, category));
            }
        }
        return Optional.empty();
    }

    @Override
    public Collection<IGuiClickableArea>
    getGuiClickableAreas(MenuScreen<?> screen, double mouseX, double mouseY) {
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
