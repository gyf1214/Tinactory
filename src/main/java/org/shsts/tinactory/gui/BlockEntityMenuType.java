package org.shsts.tinactory.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.network.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BlockEntityMenuType<T extends SmartBlockEntity, M extends BlockEntityMenu<T>>
        extends MenuType<M> {

    private final Function<T, Component> title;

    public BlockEntityMenuType(MenuSupplier<M> factory, Function<T, Component> title) {
        super(factory);
        this.title = title;
    }

    private class Provider implements MenuProvider {
        @Override
        public net.minecraft.network.chat.Component getDisplayName() {
            return null;
        }

        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
            return null;
        }
    }
}
