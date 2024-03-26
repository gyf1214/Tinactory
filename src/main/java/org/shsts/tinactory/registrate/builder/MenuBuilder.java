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
import org.shsts.tinactory.content.machine.Voltage;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

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
            this.result = this.func.apply(menu);
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

    protected final List<Supplier<Function<M, ContainerWidget>>> widgets = new ArrayList<>();

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

    public S title(Function<T, Component> title) {
        this.title = title;
        return self();
    }

    public S screen(DistLazy<MenuScreens.ScreenConstructor<M, ? extends ContainerMenuScreen<M>>> screen) {
        this.screenFactory = screen;
        return self();
    }

    public S widget(Supplier<Function<M, ContainerWidget>> factory) {
        this.widgets.add(factory);
        return self();
    }

    public S widget(Rect rect, Supplier<Function<M, ContainerWidget>> factory) {
        this.widgetsRect.add(rect);
        this.widgets.add(factory);
        return self();
    }

    public S staticWidget(Texture tex, int x, int y) {
        return this.staticWidget(new Rect(x, y, tex.width(), tex.height()), tex);
    }

    public S staticWidget(Rect rect, Texture tex) {
        return this.widget(rect, () -> menu -> new StaticWidget(menu, rect, tex));
    }

    public S slot(int slotIndex, int posX, int posY) {
        return slot(SlotItemHandler::new, slotIndex, posX, posY);
    }

    public S slot(ContainerMenu.SlotFactory<?> factory, int slotIndex, int posX, int posY) {
        this.menuCallbacks.add(MenuCallback.dummy(menu -> menu.addSlot(factory, slotIndex, posX, posY)));
        this.widgetsRect.add(new Rect(posX, posY, ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE));
        return self();
    }

    protected <P1 extends ContainerSyncPacket>
    Supplier<Integer> addSyncSlot(Class<P1> clazz, ContainerMenu.SyncPacketFactory<T, P1> factory) {
        var callback = new MenuCallback<M, Integer>(menu -> menu.addSyncSlot(clazz, factory));
        this.menuCallbacks.add(callback);
        return callback;
    }

    public S progressBar(Texture tex, Rect rect, ToDoubleFunction<T> progressReader) {
        var syncSlot = this.addSyncSlot(ContainerSyncPacket.Double.class,
                (containerId, index, $, be) -> new ContainerSyncPacket.Double(containerId, index,
                        progressReader.applyAsDouble(be)));
        return this.widget(rect, () -> menu -> new ProgressBar(menu, rect, tex, syncSlot.get()));
    }

    public S switchButton(Texture tex, int x, int y, Component tooltip,
                          Predicate<T> valueReader, BiConsumer<M, Boolean> onSwitch) {
        var rect = new Rect(x, y, tex.width(), tex.height() / 2);
        return this.switchButton(tex, rect, tooltip, valueReader, onSwitch);
    }

    public S switchButton(Texture tex, Rect rect, Component tooltip,
                          Predicate<T> valueReader, BiConsumer<M, Boolean> onSwitch) {
        var syncSlot = this.addSyncSlot(ContainerSyncPacket.Boolean.class,
                (containerId, index, $, be) -> new ContainerSyncPacket.Boolean(containerId, index,
                        valueReader.test(be)));
        return this.widget(rect, () -> menu ->
                new SwitchButton(menu, rect, tex, tooltip, syncSlot.get(), onSwitch));
    }

    public <P1 extends ContainerEventPacket>
    S registerEvent(ContainerEventHandler.Event<P1> event, BiConsumer<M, P1> handler) {
        this.menuCallbacks.add(MenuCallback.dummy(menu ->
                menu.registerEvent(event, p -> handler.accept(menu, p))));
        return self();
    }

    public S fluidSlot(int tank, int x, int y) {
        var syncSlot = new MenuCallback<M, Integer>(menu -> menu.addFluidSlot(tank));
        this.menuCallbacks.add(syncSlot);
        var rect = new Rect(x, y, ContainerMenu.SLOT_SIZE, ContainerMenu.SLOT_SIZE);
        var rect1 = rect.offset(1, 1).enlarge(-2, -2);
        return this.staticWidget(rect, Texture.SLOT_BACKGROUND)
                .widget(rect1, () -> menu -> new FluidSlot(menu, rect1, tank, syncSlot.get()));
    }

    public S layout(Layout layout, int yOffset, Voltage voltage) {
        return this.transform(layout.applyMenu(yOffset, voltage));
    }

    public S layout(Layout layout, Voltage voltage) {
        return this.layout(layout, 0, voltage);
    }

    public S layout(Layout layout) {
        return this.layout(layout, Voltage.MAXIMUM);
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
                screen.addWidget(widget.get());
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
}
