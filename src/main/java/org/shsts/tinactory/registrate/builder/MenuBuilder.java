package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.ContainerMenuScreen;
import org.shsts.tinactory.gui.ContainerMenuType;
import org.shsts.tinactory.gui.ContainerWidget;
import org.shsts.tinactory.gui.Rect;
import org.shsts.tinactory.registrate.DistLazy;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuBuilder<T extends SmartBlockEntity, M extends ContainerMenu<T>,
        P extends BlockEntityBuilder<T, ?, ?>, S extends MenuBuilder<T, M, P, S>>
        extends RegistryEntryBuilder<MenuType<?>, ContainerMenuType<T, M>, P, S> {
    protected final Supplier<SmartBlockEntityType<T>> blockEntityType;
    protected final ContainerMenu.Factory<T, M> factory;
    protected Function<T, Component> title = be -> new TextComponent(be.toString());
    protected final List<Rect> widgetsRect = new ArrayList<>();
    protected boolean showInventory = true;
    protected final List<Consumer<M>> menuCallbacks = new ArrayList<>();

    protected DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>>
            screenFactory = () -> () -> ContainerMenuScreen::new;
    protected final List<ContainerWidget.Builder> widgets = new ArrayList<>();

    public MenuBuilder(Registrate registrate, String id, P parent, ContainerMenu.Factory<T, M> factory) {
        super(registrate, registrate.menuTypeHandler, id, parent);
        this.blockEntityType = () -> {
            assert parent.entry != null;
            return parent.entry.get();
        };
        this.factory = factory;
    }

    public S title(Function<T, Component> title) {
        this.title = title;
        return self();
    }

    public S menuCallback(Consumer<M> callback) {
        this.menuCallbacks.add(callback);
        return self();
    }

    public S screen(DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>> screen) {
        this.screenFactory = screen;
        return self();
    }

    public S widget(Rect rect, Supplier<Function<Rect, ContainerWidget>> factory) {
        this.widgetsRect.add(rect);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                this.widgets.add(new ContainerWidget.Builder(rect, factory.get())));
        return self();
    }

    public S slot(int slotIndex, int posX, int posY) {
        this.menuCallbacks.add(menu -> menu.addSlot(slotIndex, posX, posY));
        this.widgetsRect.add(new Rect(posX, posY, ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE));
        return self();
    }

    public S noInventory() {
        this.showInventory = false;
        return self();
    }

    protected ContainerMenu.Factory<T, M> getFactory() {
        var showInventory = this.showInventory;
        return (type, id, inventory, blockEntity) -> {
            var menu = this.factory.create(type, id, inventory, blockEntity);
            for (var callback : this.menuCallbacks) {
                callback.accept(menu);
            }
            menu.setLayout(this.widgetsRect, showInventory);
            return menu;
        };
    }

    protected MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>> getScreenFactory() {
        assert this.screenFactory != null;
        var screenFactory = this.screenFactory.getValue();
        return (menu, inventory, title) -> {
            var screen = screenFactory.create(menu, inventory, title);
            screen.addWidgetBuilders(this.widgets);
            return screen;
        };
    }

    @Override
    public ContainerMenuType<T, M> buildObject() {
        var menuType = new ContainerMenuType<>(this.getFactory(), this.blockEntityType, this.title);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                this.registrate.menuScreenHandler.setMenuScreen(menuType, this.getScreenFactory()));
        return menuType;
    }

    @Override
    public P build() {
        this.parent.onCreateEntry.add($ -> this.register());
        this.onCreateEntry.add(entry -> this.parent.setMenu(entry::get));
        return this.parent;
    }
}
