package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.client.MenuScreen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMenuPlugin<M extends Menu<?, M>> {
    @OnlyIn(Dist.CLIENT)
    void applyMenuScreen(MenuScreen<M> screen);
}
