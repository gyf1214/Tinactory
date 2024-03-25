package org.shsts.tinactory.core.logistics;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;
import org.shsts.tinactory.api.logistics.IPort;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ItemContentWrapper(ItemStack itemStack) implements ILogisticsContentWrapper {
    @Override
    public int getCount() {
        return this.itemStack.getCount();
    }

    @Override
    public boolean isEmpty() {
        return this.itemStack.isEmpty();
    }

    @Override
    public void grow(int amount) {
        this.itemStack.grow(amount);
    }

    @Override
    public void shrink(int amount) {
        this.itemStack.shrink(amount);
    }

    @Override
    public ILogisticsContentWrapper extractFrom(IPort port, boolean simulate) {
        var stack = port.asItem().extractItem(this.itemStack, simulate);
        return new ItemContentWrapper(stack);
    }

    @Override
    public ILogisticsContentWrapper insertInto(IPort port, boolean simulate) {
        var stack = port.asItem().insertItem(this.itemStack, simulate);
        return new ItemContentWrapper(stack);
    }

    @Override
    public ILogisticsTypeWrapper getType() {
        return new ItemTypeWrapper(this.itemStack);
    }

    @Override
    public ILogisticsContentWrapper copyWithAmount(int amount) {
        var stack = ItemHandlerHelper.copyStackWithSize(this.itemStack, amount);
        return new ItemContentWrapper(stack);
    }
}
