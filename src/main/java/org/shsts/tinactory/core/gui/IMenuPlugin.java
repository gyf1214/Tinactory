package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.gui.client.MenuScreen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMenuPlugin<M extends Menu<?, M>> {
    @OnlyIn(Dist.CLIENT)
    void applyMenuScreen(MenuScreen<M> screen);

    default void onMenuRemoved(Player player) {}

    @OnlyIn(Dist.CLIENT)
    default void onScreenRemoved() {}

    @FunctionalInterface
    interface Factory<M extends Menu<?, M>> {
        IMenuPlugin<M> create(M menu);

        @SuppressWarnings("unchecked")
        default <M1 extends Menu<?, M1>> IMenuPlugin<M1> castCreate(M1 menu) {
            return (IMenuPlugin<M1>) create((M) menu);
        }
    }
}
