package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.core.util.MathUtil;

import java.util.function.Consumer;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

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

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        ItemProperties.register(this, ITEM_PROPERTY, (stack, $1, $2, $3) ->
            (float) getPowerLevel(stack) / capacity);
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
