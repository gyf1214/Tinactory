package org.shsts.tinactory.core.logistics;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.logistics.PortType;

import java.util.Objects;

/**
 * Used to represent unique item type used in IItemCollection.
 * Two item stacks are equal if they have same item, NBT and caps.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ItemTypeWrapper(@Nonnull ItemStack stack) implements ILogisticsTypeWrapper {
    @Override
    public String toString() {
        return "ItemTypeWrapper{%s}".formatted(stack.getItem());
    }

    @Override
    public int hashCode() {
        // unfortunately capNBT is protected
        return Objects.hash(stack.getItem(), stack.getTag());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || (obj instanceof ItemTypeWrapper o && ItemHelper.canItemsStack(stack, o.stack));
    }

    @Override
    public PortType getPortType() {
        return PortType.ITEM;
    }
}
