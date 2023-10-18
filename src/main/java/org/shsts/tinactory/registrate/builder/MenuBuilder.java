package org.shsts.tinactory.registrate.builder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.ContainerMenuType;
import org.shsts.tinactory.registrate.DistLazy;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
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
    @Nullable
    protected DistLazy<MenuScreens.ScreenConstructor<M, ? extends AbstractContainerScreen<M>>> screen = null;

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

    public S screen(DistLazy<MenuScreens.ScreenConstructor<M, ? extends AbstractContainerScreen<M>>> screen) {
        this.screen = screen;
        return self();
    }

    @Override
    public ContainerMenuType<T, M> buildObject() {
        var menuType = new ContainerMenuType<>(this.factory, this.blockEntityType, this.title);
        if (this.screen != null) {
            this.screen.runOnDist(Dist.CLIENT, () -> screen ->
                    this.registrate.menuScreenHandler.setMenuScreen(menuType, screen));
        }
        return menuType;
    }

    @Override
    public P build() {
        this.parent.onCreateEntry.add($ -> this.register());
        this.onCreateEntry.add(entry -> this.parent.setMenu(entry::get));
        return this.parent;
    }
}
