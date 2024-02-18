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
import org.shsts.tinactory.core.common.SmartBlockEntityType;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ContainerMenuType<T extends BlockEntity, M extends ContainerMenu<T>>
        extends MenuType<M> {

    private final ContainerMenu.Factory<T, M> factory;
    private final Function<T, Component> title;
    public final Supplier<SmartBlockEntityType<T>> blockEntityType;

    @SuppressWarnings("ConstantConditions")
    public ContainerMenuType(ContainerMenu.Factory<T, M> factory,
                             Supplier<SmartBlockEntityType<T>> blockEntityType,
                             Function<T, Component> title) {
        super(null);
        this.factory = factory;
        this.title = title;
        this.blockEntityType = blockEntityType;
    }

    @Override
    public M create(int containerId, Inventory inventory) {
        return this.create(containerId, inventory, null);
    }

    @Override
    public M create(int containerId, Inventory inventory, @Nullable FriendlyByteBuf data) {
        assert data != null;
        var pos = data.readBlockPos();
        var level = Minecraft.getInstance().level;
        assert level != null;
        var be = level.getBlockEntity(pos);
        var type = this.blockEntityType.get();
        assert be != null && be.getType() == type;
        return this.factory.create(this, containerId, inventory, type.cast(be));
    }

    public M createFromBlockEntity(int containerId, Inventory inventory, T blockEntity) {
        return this.factory.create(this, containerId, inventory, blockEntity);
    }

    private class Provider implements MenuProvider {
        private final T blockEntity;

        public Provider(T blockEntity) {
            this.blockEntity = blockEntity;
        }

        @Override
        public Component getDisplayName() {
            return title.apply(this.blockEntity);
        }

        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            return createFromBlockEntity(containerId, inventory, this.blockEntity);
        }
    }

    public MenuProvider getProvider(T blockEntity) {
        return new Provider(blockEntity);
    }
}
