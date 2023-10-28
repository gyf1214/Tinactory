package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.SlotItemHandler;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.ContainerMenuType;
import org.shsts.tinactory.gui.ContainerSyncData;
import org.shsts.tinactory.gui.Rect;
import org.shsts.tinactory.gui.Texture;
import org.shsts.tinactory.gui.client.ContainerMenuScreen;
import org.shsts.tinactory.gui.client.ContainerWidget;
import org.shsts.tinactory.gui.client.ProgressBar;
import org.shsts.tinactory.registrate.DistLazy;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
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

    protected static class MenuCallback<M1 extends ContainerMenu<?>, X> implements Supplier<X> {
        @Nullable
        private X result;
        private final Function<M1, X> func;

        protected MenuCallback(Function<M1, X> func) {
            this.func = func;
        }

        public void resolve(M1 menu) {
            this.result = func.apply(menu);
        }

        @Override
        public X get() {
            assert this.result != null;
            return this.result;
        }

        public static <M2 extends ContainerMenu<?>> MenuCallback<M2, Unit> dummy(Consumer<M2> cons) {
            return new MenuCallback<>(menu -> {
                cons.accept(menu);
                return Unit.INSTANCE;
            });
        }
    }

    protected final List<MenuCallback<M, ?>> menuCallbacks = new ArrayList<>();

    protected DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>>
            screenFactory = () -> () -> ContainerMenuScreen::new;
    protected final List<Supplier<ContainerWidget.Builder<M>>> widgets = new ArrayList<>();

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

    public S screen(DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>> screen) {
        this.screenFactory = screen;
        return self();
    }

    public S widget(Rect rect, Supplier<BiFunction<M, Rect, ContainerWidget>> factory) {
        this.widgetsRect.add(rect);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                this.widgets.add(() -> new ContainerWidget.Builder<>(rect, factory.get())));
        return self();
    }

    public S slot(int slotIndex, int posX, int posY) {
        return slot(SlotItemHandler::new, slotIndex, posX, posY);
    }

    public S slot(ContainerMenu.SlotFactory<?> factory, int slotIndex, int posX, int posY) {
        this.menuCallbacks.add(MenuCallback.dummy(menu -> menu.addSlot(factory, slotIndex, posX, posY)));
        this.widgetsRect.add(new Rect(posX, posY, ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE));
        return self();
    }

    public S progressBar(Texture tex, int posX, int posY, Function<T, Double> progressReader) {
        int w = tex.width();
        int h = tex.height() / 2;
        var callback = new MenuCallback<M, Integer>(menu -> menu.addSyncData(
                ContainerSyncData.simpleReader(menu, menu1 ->
                        (short) (progressReader.apply(menu1.blockEntity) * Short.MAX_VALUE))));
        this.menuCallbacks.add(callback);
        return this.widget(new Rect(posX, posY, w, h), () -> (menu, rect) ->
                new ProgressBar(menu, rect, tex, callback.get()));
    }

    public S noInventory() {
        this.showInventory = false;
        return self();
    }

    protected ContainerMenu.Factory<T, M> getFactory() {
        var showInventory = this.showInventory;
        var menuCallbacks = this.menuCallbacks;
        var widgetsRect = this.widgetsRect;
        var factory = this.factory;
        return (type, id, inventory, blockEntity) -> {
            var menu = factory.create(type, id, inventory, blockEntity);
            menu.initLayout();
            for (var callback : menuCallbacks) {
                callback.resolve(menu);
            }
            menu.setLayout(widgetsRect, showInventory);
            return menu;
        };
    }

    protected MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>> getScreenFactory() {
        assert this.screenFactory != null;
        var screenFactory = this.screenFactory.getValue();
        var widgets = this.widgets;
        return (menu, inventory, title) -> {
            var screen = screenFactory.create(menu, inventory, title);
            for (var widget : widgets) {
                screen.addWidgetBuilder(widget.get());
            }
            return screen;
        };
    }

    @Override
    public ContainerMenuType<T, M> createObject() {
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
