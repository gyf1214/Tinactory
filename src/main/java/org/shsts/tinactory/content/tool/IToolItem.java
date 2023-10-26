package org.shsts.tinactory.content.tool;

import net.minecraft.world.item.ItemStack;

public interface IToolItem {
    int getLevel();

    ItemStack doDamage(ItemStack stack, int damage);
}
