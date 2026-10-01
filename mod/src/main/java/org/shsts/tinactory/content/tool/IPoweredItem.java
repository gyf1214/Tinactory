package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPoweredItem {
    Voltage voltage();

    long capacity();

    long getPower(ItemStack stack);

    void setPower(ItemStack stack, long value);

    void charge(ItemStack stack, long delta);
}
