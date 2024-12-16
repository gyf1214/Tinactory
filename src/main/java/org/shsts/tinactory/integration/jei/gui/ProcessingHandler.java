package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.Widget;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.integration.jei.JEI;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.shsts.tinactory.core.gui.Menu.MARGIN_TOP;
import static org.shsts.tinactory.core.gui.Menu.MARGIN_X;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingHandler extends MenuScreenHandler<ProcessingScreen> {
    private final JEI jei;

    public ProcessingHandler(JEI jei) {
        this.jei = jei;
    }

    @Override
    protected @Nullable Object getIngredientHovered(Widget hovered, double mouseX, double mouseY) {
        return null;
    }

    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(ProcessingScreen screen,
        double mouseX, double mouseY) {
        var layout = screen.layout;
        var category = screen.getRecipeType()
            .flatMap(jei::processingCategory)
            .orElse(null);
        if (layout.progressBar == null || category == null) {
            return Collections.emptyList();
        }
        var rect = layout.progressBar.rect()
            .offset(layout.getXOffset() + MARGIN_X, MARGIN_TOP);
        return List.of(IGuiClickableArea.createBasic(rect.x(), rect.y(),
            rect.width(), rect.height(), category.type));
    }
}
