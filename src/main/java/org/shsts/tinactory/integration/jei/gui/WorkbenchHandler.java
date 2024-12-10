package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllLayouts;
import org.shsts.tinactory.content.gui.client.WorkbenchScreen;
import org.shsts.tinactory.integration.jei.category.ToolCategory;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class WorkbenchHandler {
    private WorkbenchHandler() {}

    public static void addWorkbenchClickArea(IGuiHandlerRegistration registration,
        ToolCategory category) {
        var layout = AllLayouts.WORKBENCH;
        var rect = layout.images.get(0).rect();
        var x = rect.x() + layout.getXOffset() + MARGIN_X;
        var y = rect.y() + MARGIN_TOP;

        registration.addRecipeClickArea(WorkbenchScreen.class, x, y, rect.width(), rect.height(),
            category.type);
    }
}
