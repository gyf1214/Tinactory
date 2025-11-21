package org.shsts.tinactory.core.common;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;
import static org.shsts.tinactory.core.util.ClientUtil.fluidAmount;
import static org.shsts.tinactory.core.util.ClientUtil.fluidName;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CellItem extends CapabilityItem {
    public final int capacity;

    public CellItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public static Function<Properties, CellItem> factory(int capacity) {
        return prop -> new CellItem(prop, capacity);
    }

    private static FluidStack getFluid(ItemStack item) {
        return item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
            .map(fluidHandler -> fluidHandler.getFluidInTank(0))
            .orElse(FluidStack.EMPTY);
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
            return fluid.getFluid().getAttributes().getColor();
        }
    }

    public ItemStack create(FluidStack fluid) {
        var tag = new CompoundTag();
        var tag1 = new CompoundTag();
        fluid.writeToNBT(tag1);
        tag.put(FluidHandlerItemStack.FLUID_NBT_KEY, tag1);
        var ret = new ItemStack(this);
        ret.setTag(tag);
        return ret;
    }

    @Override
    public void appendHoverText(ItemStack item, @Nullable Level world,
        List<Component> tooltip, TooltipFlag flag) {
        var fluid = getFluid(item);
        addTooltip(tooltip, fluidName(fluid));
        addTooltip(tooltip, "fluidCell", fluidAmount(fluid), fluidAmount(capacity));
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(modLoc("fluid_cell"),
            new FluidHandlerItemStack(event.getObject(), capacity));
    }
}
