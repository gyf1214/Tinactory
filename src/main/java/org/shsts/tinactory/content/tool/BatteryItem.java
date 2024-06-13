package org.shsts.tinactory.content.tool;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.util.MathUtil;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryItem extends Item {
    public final long capacity;

    public BatteryItem(Properties properties, long capacity) {
        super(properties.stacksTo(1));
        this.capacity = capacity;
    }

    public long getPowerLevel(ItemStack stack) {
        if (stack.getTag() == null) {
            return 0L;
        }
        return stack.getTag().getLong("power");
    }

    public void setPowerLevel(ItemStack stack, long value) {
        var tag = stack.getOrCreateTag();
        tag.putLong("power", value);
    }

    public void charge(ItemStack stack, long delta) {
        var value = MathUtil.clamp(getPowerLevel(stack) + delta, 0L, capacity);
        setPowerLevel(stack, value);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13f * (double) getPowerLevel(stack) / (double) capacity);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return 0xFF55FF55;
    }
}
