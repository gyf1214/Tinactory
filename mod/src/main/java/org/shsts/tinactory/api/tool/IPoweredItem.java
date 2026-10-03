package org.shsts.tinactory.api.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IPoweredItem {
    long voltage();

    long capacity();

    long getPower(ItemStack stack);

    void setPower(ItemStack stack, long value);

    void charge(ItemStack stack, long delta);
}
