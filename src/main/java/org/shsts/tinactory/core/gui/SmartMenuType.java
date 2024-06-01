package org.shsts.tinactory.core.gui;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmartMenuType<T extends BlockEntity, M extends Menu<? super T, M>> extends MenuType<M> {
    private final Menu.Factory<T, M> factory;
    private final Function<T, Component> title;

    @SuppressWarnings("ConstantConditions")
    public SmartMenuType(Menu.Factory<T, M> factory, Function<T, Component> title) {
        super(null);
        this.factory = factory;
        this.title = title;
    }

    @Override
    public M create(int containerId, Inventory inventory) {
        return create(containerId, inventory, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public M create(int containerId, Inventory inventory, @Nullable FriendlyByteBuf data) {
        assert data != null;
        var pos = data.readBlockPos();
        var level = Minecraft.getInstance().level;
        assert level != null;
        var be = level.getBlockEntity(pos);
        assert be != null;
        return factory.create(this, containerId, inventory, (T) be);
    }

    public M createFromBlockEntity(int containerId, Inventory inventory, T blockEntity) {
        return factory.create(this, containerId, inventory, blockEntity);
    }

    private class Provider implements MenuProvider {
        private final T blockEntity;

        public Provider(T blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public Component getDisplayName() {
            return title.apply(blockEntity);
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return createFromBlockEntity(containerId, inventory, blockEntity);
        }
    }

    public MenuProvider getProvider(T blockEntity) {
        return new Provider(blockEntity);
    }
}
