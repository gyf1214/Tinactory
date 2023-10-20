package org.shsts.tinactory.content.tool;

import net.minecraft.world.item.ItemStack;

public interface IToolItem {
    int getLevel();

    boolean canDamage(ItemStack stack, int damage);

    ItemStack doDamage(ItemStack stack, int damage);
}
