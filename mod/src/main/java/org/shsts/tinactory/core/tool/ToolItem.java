package org.shsts.tinactory.core.tool;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolItem extends Item {
    public static final String HIDE_BAR = "tinactoryHideBar";

    protected final int durability;

    public ToolItem(Properties properties, int durability) {
        super(properties.defaultDurability(durability).setNoRepair());
        this.durability = durability;
    }

    public int durability() {
        return durability;
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
        return stack.getTag() == null || !stack.getTag().getBoolean(HIDE_BAR);
    }

    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip,
        TooltipFlag isAdvanced) {
        var remaining = stack.getMaxDamage() - stack.getDamageValue();
        addTooltip(tooltip, "tool", NUMBER_FORMAT.format(remaining));
    }
}
