package org.shsts.tinactory.registrate.handler;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class MenuScreenHandler {
    private record Entry<M extends AbstractContainerMenu, U extends AbstractContainerScreen<M>>
            (MenuType<M> menuType, MenuScreens.ScreenConstructor<M, U> factory) {
        public void register() {
            MenuScreens.register(this.menuType, this.factory);
        }
    }

    private final List<Entry<?, ?>> entries = new ArrayList<>();

    public <M extends AbstractContainerMenu, U extends AbstractContainerScreen<M>>
    void setMenuScreen(MenuType<M> menuType, MenuScreens.ScreenConstructor<M, U> factory) {
        this.entries.add(new Entry<>(menuType, factory));
    }

    public void onClientSetup() {
        for (var entry : this.entries) {
            entry.register();
        }
        this.entries.clear();
    }
}
