package org.shsts.tinactory.content.material;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.shsts.tinactory.AllMaterials;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BottleFluidHandler implements IFluidHandlerItem {
    private static final int BOTTLE_VOLUME = 250;

    private ItemStack container;

    public BottleFluidHandler(ItemStack container) {
        this.container = container;
    }

    private Fluid honey() {
        return AllMaterials.getMaterial("honey").fluid("fluid").get();
    }

    private FluidStack getFluid() {
        if (container.is(Items.HONEY_BOTTLE)) {
            return new FluidStack(honey(), BOTTLE_VOLUME);
        } else if (container.is(Items.POTION) &&
            container.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER)) {
            return new FluidStack(Fluids.WATER, BOTTLE_VOLUME);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return BOTTLE_VOLUME;
    }

    private boolean isFluidValid(FluidStack stack) {
        return stack.is(honey()) || stack.is(Fluids.WATER);
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack stack, FluidAction action) {
        if (container.getCount() != 1 || !container.is(Items.GLASS_BOTTLE) ||
            stack.getAmount() < BOTTLE_VOLUME || !isFluidValid(stack)) {
            return 0;
        }

        if (action.execute()) {
            if (stack.is(honey())) {
                container = new ItemStack(Items.HONEY_BOTTLE);
            } else if (stack.is(Fluids.WATER)) {
                container = PotionContents.createItemStack(Items.POTION, Potions.WATER);
            }
        }

        return BOTTLE_VOLUME;
    }

    @Override
    public FluidStack drain(FluidStack stack, FluidAction action) {
        if (container.getCount() != 1 || stack.getAmount() < BOTTLE_VOLUME) {
            return FluidStack.EMPTY;
        }

        var stack1 = getFluid();
        if (stack1.isEmpty() || !FluidStack.isSameFluidSameComponents(stack, stack1)) {
            return FluidStack.EMPTY;
        }
        if (action.execute()) {
            container = new ItemStack(Items.GLASS_BOTTLE);
        }
        return stack1;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (container.getCount() != 1 || maxDrain < BOTTLE_VOLUME) {
            return FluidStack.EMPTY;
        }

        var stack1 = getFluid();
        if (stack1.isEmpty()) {
            return FluidStack.EMPTY;
        }
        if (action.execute()) {
            container = new ItemStack(Items.GLASS_BOTTLE);
        }
        return stack1;
    }

    @Override
    public ItemStack getContainer() {
        return container;
    }
}
