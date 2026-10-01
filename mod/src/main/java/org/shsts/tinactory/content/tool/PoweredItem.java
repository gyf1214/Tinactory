package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.List;

import static org.shsts.tinactory.AllDataComponents.BATTERY;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PoweredItem extends Item implements IPoweredItem {
    public static final String ITEM_PROPERTY = "battery_level";

    private final Voltage voltage;
    private final long capacity;

    public PoweredItem(Properties properties, Voltage voltage, long capacity) {
        super(properties.stacksTo(1));
        this.voltage = voltage;
        this.capacity = capacity;
    }

    @Override
    public Voltage voltage() {
        return voltage;
    }

    @Override
    public long capacity() {
        return capacity;
    }

    @Override
    public long getPower(ItemStack stack) {
        return Math.clamp(stack.getOrDefault(BATTERY, 0L), 0L, capacity);
    }

    public float getNormalizedPower(ItemStack stack) {
        return (float) getPower(stack) / (float) capacity;
    }

    public static float normalizedPower(ItemStack stack) {
        var poweredItem = (IPoweredItem) stack.getItem();
        return (float) poweredItem.getPower(stack) / (float) poweredItem.capacity();
    }

    public ItemStack fullItem() {
        var ret = new ItemStack(this);
        ret.set(BATTERY, capacity);
        return ret;
    }

    @Override
    public void setPower(ItemStack stack, long value) {
        stack.set(BATTERY, Math.clamp(value, 0L, capacity));
    }

    @Override
    public void charge(ItemStack stack, long delta) {
        setPower(stack, Math.clamp(getPower(stack) + delta, 0L, capacity));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13f * getNormalizedPower(stack));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        addTooltip(tooltip, "battery", NUMBER_FORMAT.format(getPower(stack)),
            NUMBER_FORMAT.format(capacity), voltage.displayName());
    }
}
