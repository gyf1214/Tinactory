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
import org.shsts.tinactory.core.common.ISelf;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.ContainerMenuType;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Rect;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.gui.client.ContainerMenuScreen;
import org.shsts.tinactory.core.gui.client.ContainerWidget;
import org.shsts.tinactory.core.gui.client.FluidSlot;
import org.shsts.tinactory.core.gui.client.ProgressBar;
import org.shsts.tinactory.core.gui.client.StaticWidget;
import org.shsts.tinactory.core.gui.client.SwitchButton;
import org.shsts.tinactory.core.gui.sync.ContainerEventHandler;
import org.shsts.tinactory.core.gui.sync.ContainerEventPacket;
import org.shsts.tinactory.core.gui.sync.ContainerSyncPacket;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.common.DistLazy;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MenuBuilder<T extends SmartBlockEntity, M extends ContainerMenu<T>,
        P extends BlockEntityBuilder<T, ?>>
        extends RegistryEntryBuilder<MenuType<?>, ContainerMenuType<T, M>, P, MenuBuilder<T, M, P>> {
    private final Supplier<SmartBlockEntityType<T>> blockEntityType;
    private final ContainerMenu.Factory<T, M> factory;
    private Function<T, Component> title = be -> new TextComponent(be.toString());
    private final List<Rect> widgetsRect = new ArrayList<>();
    private boolean showInventory = true;

    private static class MenuCallback<M1 extends ContainerMenu<?>, X> implements Supplier<X> {
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

        public static <M2 extends ContainerMenu<?>> MenuCallback<M2, Unit> dummy(Consumer<M2> cons) {
            return new MenuCallback<>(menu -> {
                cons.accept(menu);
                return Unit.INSTANCE;
            });
        }
    }

    private final List<MenuCallback<M, ?>> menuCallbacks = new ArrayList<>();
    private DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>>
            screenFactory = () -> () -> ContainerMenuScreen::new;
    private final List<Supplier<Function<M, ISelf<ContainerWidget>>>> widgets = new ArrayList<>();

    public MenuBuilder(Registrate registrate, String id, P parent, ContainerMenu.Factory<T, M> factory) {
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

    public MenuBuilder<T, M, P> title(Function<T, Component> value) {
        title = value;
        return self();
    }

    public MenuBuilder<T, M, P>
    screen(DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>> screen) {
        screenFactory = screen;
        return self();
    }

    public MenuBuilder<T, M, P> widget(Supplier<Function<M, ISelf<ContainerWidget>>> factory) {
        widgets.add(factory);
        return self();
    }

    public MenuBuilder<T, M, P> widget(Rect rect, Supplier<Function<M, ISelf<ContainerWidget>>> factory) {
        widgetsRect.add(rect);
        widgets.add(factory);
        return self();
    }

    public <P1 extends ContainerSyncPacket> MenuBuilder<T, M, P>
    syncWidget(Class<P1> packetClazz, ContainerMenu.SyncPacketFactory<T, P1> packetFactory,
               Supplier<BiFunction<M, Integer, ISelf<ContainerWidget>>> widgetFactory) {
        var syncSlot = addSyncSlot(packetClazz, packetFactory);
        return widget(() -> menu -> widgetFactory.get().apply(menu, syncSlot.getAsInt()));
    }

    public <P1 extends ContainerSyncPacket> MenuBuilder<T, M, P>
    syncWidget(Rect rect, Class<P1> packetClazz, ContainerMenu.SyncPacketFactory<T, P1> packetFactory,
               Supplier<BiFunction<M, Integer, ISelf<ContainerWidget>>> widgetFactory) {
        widgetsRect.add(rect);
        return syncWidget(packetClazz, packetFactory, widgetFactory);
    }

    public MenuBuilder<T, M, P> staticWidget(Texture tex, int x, int y) {
        return staticWidget(new Rect(x, y, tex.width(), tex.height()), tex);
    }

    public MenuBuilder<T, M, P> staticWidget(Rect rect, Texture tex) {
        return widget(rect, () -> menu -> new StaticWidget(menu, rect, tex));
    }

    public MenuBuilder<T, M, P> slot(int slotIndex, int posX, int posY) {
        return slot(SlotItemHandler::new, slotIndex, posX, posY);
    }

    public MenuBuilder<T, M, P>
    slot(ContainerMenu.SlotFactory<?> factory, int slotIndex, int posX, int posY) {
        menuCallbacks.add(MenuCallback.dummy(menu -> menu.addSlot(factory, slotIndex, posX, posY)));
        widgetsRect.add(new Rect(posX, posY, ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE));
        return self();
    }

    public <P1 extends ContainerSyncPacket>
    IntSupplier addSyncSlot(Class<P1> clazz, ContainerMenu.SyncPacketFactory<T, P1> factory) {
        var callback = new MenuCallback<M, Integer>(menu -> menu.addSyncSlot(clazz, factory));
        menuCallbacks.add(callback);
        return callback::get;
    }

    public MenuBuilder<T, M, P>
    progressBar(Texture tex, Rect rect, ToDoubleFunction<T> progressReader) {
        return syncWidget(rect, ContainerSyncPacket.Double.class, (containerId, index, $, be) ->
                        new ContainerSyncPacket.Double(containerId, index, progressReader.applyAsDouble(be)),
                () -> (menu, slot) -> new ProgressBar(menu, rect, tex, slot));
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
        return syncWidget(rect, ContainerSyncPacket.Boolean.class, (containerId, index, $, be) ->
                        new ContainerSyncPacket.Boolean(containerId, index, valueReader.test(be)),
                () -> (menu, slot) -> new SwitchButton(menu, rect, tex, tooltip, slot, onSwitch));
    }

    public <P1 extends ContainerEventPacket> MenuBuilder<T, M, P>
    event(ContainerEventHandler.Event<P1> event, BiConsumer<M, P1> handler) {
        menuCallbacks.add(MenuCallback.dummy(menu ->
                menu.registerEvent(event, p -> handler.accept(menu, p))));
        return self();
    }

    public MenuBuilder<T, M, P> fluidSlot(int tank, int x, int y) {
        var syncSlot = new MenuCallback<M, Integer>(menu -> menu.addFluidSlot(tank));
        menuCallbacks.add(syncSlot);
        var rect = new Rect(x, y, ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE);
        var rect1 = rect.offset(1, 1).enlarge(-2, -2);
        return staticWidget(rect, Texture.SLOT_BACKGROUND)
                .widget(rect1, () -> menu -> new FluidSlot(menu, rect1, tank, syncSlot.get()));
    }

    public MenuBuilder<T, M, P> layout(Layout layout) {
        return transform(layout.applyMenu());
    }

    public MenuBuilder<T, M, P> noInventory() {
        showInventory = false;
        return self();
    }

    private ContainerMenu.Factory<T, M> getFactory() {
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

    private MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>> getScreenFactory() {
        assert this.screenFactory != null;
        var screenFactory = this.screenFactory.getValue();
        var widgets = this.widgets;
        return (menu, inventory, title) -> {
            var screen = screenFactory.create(menu, inventory, title);
            for (var widget : widgets) {
                screen.addWidget(widget.get());
            }
            return screen;
        };
    }

    @Override
    public ContainerMenuType<T, M> createObject() {
        var menuType = new ContainerMenuType<>(getFactory(), blockEntityType, title);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                registrate.menuScreenHandler.setMenuScreen(menuType, getScreenFactory()));
        return menuType;
    }
}
