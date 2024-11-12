package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolItem extends Item {
    public ToolItem(Properties properties, int durability) {
        super(properties.defaultDurability(durability).setNoRepair());
    }

    /**
     * Will override the itemStack.
     */
    public static ItemStack doDamage(ItemStack stack, int damage) {
        var newDamage = stack.getDamageValue() + damage;
        if (newDamage >= stack.getMaxDamage()) {
            return ItemStack.EMPTY;
        }
        stack.setDamageValue(newDamage);
        return stack;
    }

    /**
     * Will override the itemStack.
     */
    public static void doDamage(ItemStack stack, int damage, LivingEntity entity, InteractionHand hand) {
        stack.hurtAndBreak(damage, entity, entity1 -> entity1.broadcastBreakEvent(hand));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }
}
