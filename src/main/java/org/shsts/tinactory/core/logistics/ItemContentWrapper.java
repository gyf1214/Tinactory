package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.IPort;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ItemContentWrapper(ItemStack itemStack) implements ILogisticsContentWrapper {
    @Override
    public int getCount() {
        return itemStack.getCount();
    }

    @Override
    public boolean isEmpty() {
        return itemStack.isEmpty();
    }

    @Override
    public void grow(int amount) {
        itemStack.grow(amount);
    }

    @Override
    public void shrink(int amount) {
        itemStack.shrink(amount);
    }

    @Override
    public ILogisticsContentWrapper extractFrom(IPort port, boolean simulate) {
        var stack = port.asItem().extractItem(itemStack, simulate);
        return new ItemContentWrapper(stack);
    }

    @Override
    public ILogisticsContentWrapper insertInto(IPort port, boolean simulate) {
        var stack = port.asItem().insertItem(itemStack, simulate);
        return new ItemContentWrapper(stack);
    }

    @Override
    public ILogisticsTypeWrapper getType() {
        return new ItemTypeWrapper(itemStack);
    }

    @Override
    public ILogisticsContentWrapper copyWithAmount(int amount) {
        var stack = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
        return new ItemContentWrapper(stack);
    }
}
