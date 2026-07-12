package org.shsts.tinactory.integration.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import org.shsts.tinycorelib.api.item.ICapabilityItem;
import org.shsts.tinycorelib.api.registrate.entry.IItemCapability;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.FLUID_HANDLER_ITEM;
import static org.shsts.tinactory.AllDataComponents.FLUID_CELL_CONTENT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;
import static org.shsts.tinactory.integration.util.ClientUtil.fluidAmount;
import static org.shsts.tinactory.integration.util.ClientUtil.fluidName;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CellItem extends Item implements ICapabilityItem {
    public final int capacity;

    public CellItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public static Function<Properties, CellItem> factory(int capacity) {
        return prop -> new CellItem(prop, capacity);
    }

    private static FluidStack getFluid(ItemStack item) {
        return item.getOrDefault(FLUID_CELL_CONTENT.get(), SimpleFluidContent.EMPTY).copy();
    }

    public static int getTint(ItemStack item, int index) {
        if (index != 1) {
            return 0xFFFFFFFF;
        }
        var fluid = getFluid(item);
        if (fluid.isEmpty()) {
            return 0x00000000;
        } else if (fluid.getFluid() instanceof SimpleFluid simpleFluid) {
            return simpleFluid.displayColor;
        } else {
            return 0xFFFFFFFF;
        }
    }

    public ItemStack create(FluidStack fluid) {
        var ret = new ItemStack(this);
        ret.set(FLUID_CELL_CONTENT.get(), SimpleFluidContent.copyOf(fluid));
        return ret;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        var fluid = getFluid(stack);
        addTooltip(tooltip, fluidName(fluid));
        addTooltip(tooltip, "fluidCell", fluidAmount(fluid), fluidAmount(capacity));
    }

    @Override
    @Nullable
    public <T> T getCapability(ItemStack stack, IItemCapability<T> capability) {
        if (FLUID_HANDLER_ITEM.is(capability)) {
            return capability.cast(new FluidHandlerItemStack(FLUID_CELL_CONTENT, stack, capacity));
        }
        return null;
    }
}
