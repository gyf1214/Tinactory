package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuBuilder<T extends SmartBlockEntity, M extends Menu<? super T, M>, P>
        extends RegistryEntryBuilder<MenuType<?>, SmartMenuType<T, M>, P, MenuBuilder<T, M, P>> {
    private final Menu.Factory<T, M> factory;
    private Function<T, Component> title = be ->
            new TranslatableComponent(be.getBlockState().getBlock().getDescriptionId());
    private boolean showInventory = true;
    private final List<Function<M, IMenuPlugin<M>>> plugins = new ArrayList<>();

    public MenuBuilder(Registrate registrate, String id, P parent, Menu.Factory<T, M> factory) {
        super(registrate, registrate.menuTypeHandler, id, parent);
        this.factory = factory;
    }

    public MenuBuilder<T, M, P> title(String key) {
        title = $ -> new TranslatableComponent("tinactory.gui." + key + ".title");
        return self();
    }

    public MenuBuilder<T, M, P> plugin(Function<M, IMenuPlugin<M>> plugin) {
        plugins.add(plugin);
        return self();
    }

    public MenuBuilder<T, M, P> noInventory() {
        showInventory = false;
        return self();
    }

    private Menu.Factory<T, M> getFactory() {
        var showInventory = this.showInventory;
        var plugins = this.plugins;
        var factory = this.factory;
        return (type, id, inventory, blockEntity) -> {
            var menu = factory.create(type, id, inventory, blockEntity);
            for (var plugin : plugins) {
                menu.addPlugin(plugin.apply(menu));
            }
            menu.setLayout(showInventory);
            return menu;
        };
    }

    @OnlyIn(Dist.CLIENT)
    private MenuScreens.ScreenConstructor<M, MenuScreen<M>> getScreenFactory() {
        return (menu, inventory, title) -> {
            var screen = menu.createScreen(inventory, title);
            menu.applyPlugin(screen);
            return screen;
        };
    }

    @Override
    protected SmartMenuType<T, M> createObject() {
        var menuType = new SmartMenuType<>(getFactory(), title);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                registrate.menuScreenHandler.setMenuScreen(menuType, getScreenFactory()));
        return menuType;
    }
}
