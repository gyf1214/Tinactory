package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Unit;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import org.shsts.tinactory.core.common.I;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.RectD;
import org.shsts.tinactory.core.gui.SmartMenuType;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.MenuScreen;
import org.shsts.tinactory.core.gui.client.Panel;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.client.SwitchButton;
import org.shsts.tinactory.core.gui.sync.MenuEventHandler;
import org.shsts.tinactory.core.gui.sync.MenuEventPacket;
import org.shsts.tinactory.core.gui.sync.MenuSyncPacket;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuBuilder<T extends SmartBlockEntity, M extends Menu<T>,
        P extends BlockEntityBuilder<T, ?>>
        extends RegistryEntryBuilder<MenuType<?>, SmartMenuType<T, M>, P, MenuBuilder<T, M, P>> {
    private final Supplier<SmartBlockEntityType<T>> blockEntityType;
    private final Menu.Factory<T, M> factory;
    private Function<T, Component> title = $ -> TextComponent.EMPTY;
    private final List<Rect> widgetsRect = new ArrayList<>();
    private boolean showInventory = true;

    private static class MenuCallback<M1 extends Menu<?>, X> implements Supplier<X> {
        @Nullable
        private X result;
        private final Function<M1, X> func;

        private MenuCallback(Function<M1, X> value) {
            func = value;
        }

        public void resolve(M1 menu) {
            result = func.apply(menu);
        }

        @Override
        public X get() {
            assert result != null;
            return result;
        }

        public static <M2 extends Menu<?>> MenuCallback<M2, Unit> dummy(Consumer<M2> cons) {
            return new MenuCallback<>(menu -> {
                cons.accept(menu);
                return Unit.INSTANCE;
            });
        }
    }

    private static class SyncSlotMenuCallback<M1 extends Menu<?>> extends MenuCallback<M1, Integer>
            implements IntSupplier {
        public SyncSlotMenuCallback(ToIntFunction<M1> value) {
            super(value::applyAsInt);
        }

        @Override
        public int getAsInt() {
            return get();
        }
    }

    public interface WidgetConsumer {
        void addGuiComponent(RectD anchor, Rect offset, GuiComponent widget);

        default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
        void addWidget(RectD anchor, Rect offset, I<T> widget) {
            addGuiComponent(anchor, offset, widget.self());
        }

        default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
        void addWidget(Rect offset, I<T> widget) {
            addGuiComponent(RectD.ZERO, offset, widget.self());
        }

        default <T extends GuiComponent & Widget & GuiEventListener & NarratableEntry>
        void addWidget(I<T> widget) {
            addGuiComponent(RectD.ZERO, Rect.ZERO, widget.self());
        }

        default void addPanel(RectD anchor, Rect offset, I<Panel> panel) {
            addGuiComponent(anchor, offset, panel.self());
        }

        default void addPanel(Rect offset, I<Panel> panel) {
            addGuiComponent(RectD.FULL, offset, panel.self());
        }

        default void addPanel(I<Panel> panel) {
            addGuiComponent(RectD.FULL, Rect.ZERO, panel.self());
        }
    }

    @FunctionalInterface
    public interface WidgetFactory<M extends Menu<?>> {
        void accept(MenuScreen<M> screen, WidgetConsumer cons);
    }

    @FunctionalInterface
    public interface MenuWidgetFactory<M extends Menu<?>> extends WidgetFactory<M> {
        void accept(M menu, WidgetConsumer cons);

        @Override
        default void accept(MenuScreen<M> screen, WidgetConsumer cons) {
            accept(screen.getMenu(), cons);
        }
    }

    private final List<MenuCallback<M, ?>> menuCallbacks = new ArrayList<>();
    private final List<Supplier<WidgetFactory<M>>> widgets = new ArrayList<>();

    public MenuBuilder(Registrate registrate, String id, P parent, Menu.Factory<T, M> factory) {
        super(registrate, registrate.menuTypeHandler, id, parent);
        this.blockEntityType = () -> {
            assert parent.entry != null;
            return parent.entry.get();
        };
        this.factory = factory;
        this.onBuild.add($ -> {
            $.parent.onCreateEntry.add($p -> $.register());
            $.onCreateEntry.add(entry -> $.parent.setMenu(entry::get));
        });
    }

    public MenuBuilder<T, M, P> title(String key) {
        title = $ -> new TranslatableComponent("tinactory.gui." + key + ".title");
        return self();
    }

    public MenuBuilder<T, M, P> screenWidget(Supplier<WidgetFactory<M>> factory) {
        widgets.add(factory);
        return self();
    }

    public MenuBuilder<T, M, P> menuWidget(Supplier<MenuWidgetFactory<M>> factory) {
        widgets.add(factory::get);
        return self();
    }

    public MenuBuilder<T, M, P> menuWidget(Rect rect, Supplier<MenuWidgetFactory<M>> factory) {
        widgetsRect.add(rect);
        return menuWidget(factory);
    }

    @FunctionalInterface
    public interface SyncWidgetFactory<M extends Menu<?>> {
        void accept(M menu, int syncSlot, WidgetConsumer widgetCons);
    }

    public <P1 extends MenuSyncPacket> MenuBuilder<T, M, P>
    syncWidget(Class<P1> packetClazz, Menu.SyncPacketFactory<T, P1> packetFactory,
               Supplier<SyncWidgetFactory<M>> widgetFactory) {
        var syncSlot = addSyncSlot(packetClazz, packetFactory);
        return menuWidget(() -> {
            var factory = widgetFactory.get();
            return (menu, widgetCons) -> factory.accept(menu, syncSlot.getAsInt(), widgetCons);
        });
    }

    public <P1 extends MenuSyncPacket> MenuBuilder<T, M, P>
    syncWidget(Rect rect, Class<P1> packetClazz, Menu.SyncPacketFactory<T, P1> packetFactory,
               Supplier<SyncWidgetFactory<M>> widgetFactory) {
        widgetsRect.add(rect);
        return syncWidget(packetClazz, packetFactory, widgetFactory);
    }

    public MenuBuilder<T, M, P> staticWidget(Texture tex, int x, int y) {
        return staticWidget(new Rect(x, y, tex.width(), tex.height()), tex);
    }

    public MenuBuilder<T, M, P> staticWidget(Rect rect, Texture tex) {
        return menuWidget(rect, () -> (menu, cons) -> cons.addWidget(rect, new StaticWidget(menu, tex)));
    }

    public <P1 extends MenuSyncPacket>
    IntSupplier addSyncSlot(Class<P1> clazz, Menu.SyncPacketFactory<T, P1> factory) {
        var callback = new SyncSlotMenuCallback<M>(menu -> menu.addSyncSlot(clazz, factory));
        menuCallbacks.add(callback);
        return callback;
    }

    public MenuBuilder<T, M, P>
    switchButton(Texture tex, int x, int y, Component tooltip,
                 Predicate<T> valueReader, BiConsumer<M, Boolean> onSwitch) {
        var rect = new Rect(x, y, tex.width(), tex.height() / 2);
        return switchButton(tex, rect, tooltip, valueReader, onSwitch);
    }

    public MenuBuilder<T, M, P>
    switchButton(Texture tex, Rect rect, Component tooltip,
                 Predicate<T> valueReader, BiConsumer<M, Boolean> onSwitch) {
        return syncWidget(rect, MenuSyncPacket.Boolean.class, (containerId, index, be) ->
                        new MenuSyncPacket.Boolean(containerId, index, valueReader.test(be)),
                () -> (menu, slot, cons) -> cons.addWidget(rect,
                        new SwitchButton(menu, tex, tooltip, slot, onSwitch)));
    }

    public <P1 extends MenuEventPacket> MenuBuilder<T, M, P>
    event(MenuEventHandler.Event<P1> event, BiConsumer<T, P1> handler) {
        menuCallbacks.add(MenuCallback.dummy(menu ->
                menu.onEventPacket(event, p -> handler.accept(menu.blockEntity, p))));
        return self();
    }

    public MenuBuilder<T, M, P> noInventory() {
        showInventory = false;
        return self();
    }

    private Menu.Factory<T, M> getFactory() {
        var showInventory = this.showInventory;
        var menuCallbacks = this.menuCallbacks;
        var widgetsRect = this.widgetsRect;
        var factory = this.factory;
        return (type, id, inventory, blockEntity) -> {
            var menu = factory.create(type, id, inventory, blockEntity);
            for (var callback : menuCallbacks) {
                callback.resolve(menu);
            }
            menu.setLayout(widgetsRect, showInventory);
            return menu;
        };
    }

    @OnlyIn(Dist.CLIENT)
    private MenuScreens.ScreenConstructor<M, ? extends MenuScreen<M>> getScreenFactory() {
        var widgets = this.widgets.stream().map(Supplier::get).toList();
        return (menu, inventory, title) -> {
            var screen = menu.<M>createScreen(inventory, title);
            for (var widget : widgets) {
                screen.initWidget(widget);
            }
            return screen;
        };
    }

    @Override
    public SmartMenuType<T, M> createObject() {
        var menuType = new SmartMenuType<>(getFactory(), blockEntityType, title);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                registrate.menuScreenHandler.setMenuScreen(menuType, getScreenFactory()));
        return menuType;
    }
}
