package org.shsts.tinactory.integration.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.core.logistics.IIngredientKey;
import org.shsts.tinactory.core.logistics.IStackAdapter;
import org.shsts.tinactory.core.logistics.StackHelper;

import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class ItemPortAdapter implements IStackAdapter<ItemStack> {
    public static final ItemPortAdapter INSTANCE = new ItemPortAdapter();

    private ItemPortAdapter() {}

    @Override
    public ItemStack empty() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public ItemStack copy(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public int amount(ItemStack stack) {
        return stack.getCount();
    }

    @Override
    public ItemStack withAmount(ItemStack stack, int amount) {
        return StackHelper.copyWithCount(stack, amount);
    }

    @Override
    public boolean canStack(ItemStack left, ItemStack right) {
        return StackHelper.canItemsStack(left, right);
    }

    @Override
    public IIngredientKey keyOf(ItemStack stack) {
        return new ItemKey(stack);
    }

    private static final class ItemKey implements IIngredientKey {
        private final ItemStack stack;

        private ItemKey(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public boolean equals(Object other) {
            return this == other || (other instanceof ItemKey key && StackHelper.canItemsStack(stack, key.stack));
        }

        @Override
        public int hashCode() {
            return Objects.hash(stack.getItem(), stack.getTag());
        }
    }
}
