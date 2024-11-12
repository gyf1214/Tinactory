package org.shsts.tinactory.integration.jei.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.client.MenuScreen;

import java.util.Optional;

@FunctionalInterface
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMenuScreenClickableProvider<M extends Menu<?, M>> {
    Optional<IGuiClickableArea> getGuiClickableAreas(MenuScreen<M> screen);

    @SuppressWarnings("unchecked")
    default Optional<IGuiClickableArea> guiClickableAreas(MenuScreen<?> screen) {
        return getGuiClickableAreas((MenuScreen<M>) screen);
    }
}
