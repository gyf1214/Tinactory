package org.shsts.tinactory.core.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.LayoutMenu;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LayoutScreen<M extends LayoutMenu> extends MenuScreen<M> {
    protected LayoutScreen(M menu, Component title) {
        super(menu, title);
        this.contentHeight = menu.endY();
    }

    public static class Simple extends LayoutScreen<LayoutMenu> {
        public Simple(LayoutMenu menu, Component title) {
            super(menu, title);
        }
    }
}
