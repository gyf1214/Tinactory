package org.shsts.tinactory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.core.SmartBlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BlockEntityMenu<T extends SmartBlockEntity> extends AbstractContainerMenu {
    protected final Player player;
    protected final Inventory inventory;
    @Nullable
    protected final T blockEntity;

    public BlockEntityMenu(MenuType<?> type, int id, Inventory inventory, T blockEntity) {
        super(type, id);
        this.player = inventory.player;
        this.inventory = inventory;
        this.blockEntity = blockEntity;
    }

    @OnlyIn(Dist.CLIENT)
    protected BlockEntityMenu(MenuType<?> type, int id, Class<T> blockEntityClass,
                              Inventory inventory, @Nullable FriendlyByteBuf data) {
        super(type, id);
        this.player = inventory.player;
        this.inventory = inventory;

        T blockEntity = null;
        if (data != null) {
            var pos = data.readBlockPos();
            var level = Minecraft.getInstance().level;
            assert level != null;
            var be = level.getBlockEntity(pos);
            if (be != null && be.getClass() == blockEntityClass) {
                blockEntity = blockEntityClass.cast(be);
            }
        }
        this.blockEntity = blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.blockEntity != null;
    }
}
