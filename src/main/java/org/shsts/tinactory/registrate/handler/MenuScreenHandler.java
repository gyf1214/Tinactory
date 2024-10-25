package org.shsts.tinactory.registrate.handler;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MenuScreenHandler {
    private record Entry<M extends AbstractContainerMenu, U extends AbstractContainerScreen<M>>
            (MenuType<M> menuType, MenuScreens.ScreenConstructor<M, U> factory) {
        public void register() {
            MenuScreens.register(menuType, factory);
        }
    }

    private final List<Entry<?, ?>> entries = new ArrayList<>();

    @OnlyIn(Dist.CLIENT)
    public <M extends AbstractContainerMenu, U extends AbstractContainerScreen<M>>
    void setMenuScreen(MenuType<M> menuType, MenuScreens.ScreenConstructor<M, U> factory) {
        entries.add(new Entry<>(menuType, factory));
    }

    public void onClientSetup() {
        for (var entry : entries) {
            entry.register();
        }
        entries.clear();
    }
}
