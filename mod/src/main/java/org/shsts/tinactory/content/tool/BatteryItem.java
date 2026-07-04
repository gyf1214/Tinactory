package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.List;

import static org.shsts.tinactory.AllDataComponents.BATTERY;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BatteryItem extends Item {
    public static final ResourceLocation ITEM_PROPERTY = modLoc("battery_level");

    public final Voltage voltage;
    public final long capacity;

    public BatteryItem(Properties properties, Voltage voltage, long capacity) {
        super(properties.stacksTo(1));
        this.voltage = voltage;
        this.capacity = capacity;
    }

    public long getPower(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(BATTERY, 0L), 0L, capacity);
    }

    public void setPowerLevel(ItemStack stack, long value) {
        var val1 = Math.clamp(value, 0, capacity);
        stack.set(BATTERY, val1);
    }

    public void charge(ItemStack stack, long delta) {
        var value = MathUtil.clamp(getPower(stack) + delta, 0L, capacity);
        setPowerLevel(stack, value);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int) Math.round(13f * (double) getPower(stack) / (double) capacity);
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return 0xFF55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        addTooltip(tooltip, "battery", NUMBER_FORMAT.format(getPower(stack)),
            NUMBER_FORMAT.format(capacity), voltage.displayName());
    }
}
