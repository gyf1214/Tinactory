package org.shsts.tinactory.core.logistics;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.api.logistics.IItemPort;

import java.util.Arrays;
import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CombinedItemPort extends CombinedPort<ItemStack, IItemPort> implements IItemPort {
    private static final IPortAccess<ItemStack, IItemPort> PORT_ACCESS = new IPortAccess<>() {
        @Override
        public boolean acceptInput(IItemPort port, ItemStack stack) {
            return port.acceptInput(stack);
        }

        @Override
        public boolean acceptOutput(IItemPort port) {
            return port.acceptOutput();
        }

        @Override
        public ItemStack insert(IItemPort port, ItemStack stack, boolean simulate) {
            return port.insert(stack, simulate);
        }

        @Override
        public ItemStack extract(IItemPort port, ItemStack stack, boolean simulate) {
            return port.extract(stack, simulate);
        }

        @Override
        public ItemStack extract(IItemPort port, int limit, boolean simulate) {
            return port.extract(limit, simulate);
        }

        @Override
        public int getStorageAmount(IItemPort port, ItemStack stack) {
            return port.getStorageAmount(stack);
        }

        @Override
        public Collection<ItemStack> getAllStorages(IItemPort port) {
            return port.getAllStorages();
        }
    };

    private static final IStackAdapter<ItemStack> STACK_ADAPTER = new IStackAdapter<>() {
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
            return new ItemStackWrapper(stack);
        }
    };

    public CombinedItemPort(IItemPort... composes) {
        super(PORT_ACCESS, STACK_ADAPTER, Arrays.asList(composes));
    }

    public CombinedItemPort() {
        super(PORT_ACCESS, STACK_ADAPTER);
    }

    @Override
    public boolean acceptInput(ItemStack stack) {
        return super.acceptInput(stack);
    }

    @Override
    public boolean acceptOutput() {
        return super.acceptOutput();
    }

    @Override
    public ItemStack insert(ItemStack stack, boolean simulate) {
        return super.insert(stack, simulate);
    }

    @Override
    public ItemStack extract(ItemStack item, boolean simulate) {
        return super.extract(item, simulate);
    }

    @Override
    public ItemStack extract(int limit, boolean simulate) {
        return super.extract(limit, simulate);
    }

    @Override
    public int getStorageAmount(ItemStack item) {
        return super.getStorageAmount(item);
    }

    @Override
    public Collection<ItemStack> getAllStorages() {
        return super.getAllStorages();
    }
}
