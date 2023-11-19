package org.shsts.tinactory.content.machine;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IWorkbench {
    ItemStack getResult();

    /**
     * Only called on client for the purpose of syncing
     */
    void setResult(ItemStack stack);

    void onTake(Player player, ItemStack stack);
}
