package org.shsts.tinactory.core.common;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import org.shsts.tinactory.TinactoryConfig;
import org.shsts.tinactory.core.util.ClientUtil;
import org.shsts.tinactory.core.util.I18n;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CellItem extends CapabilityItem {
    private final int capacity;

    public CellItem(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    public static Function<Properties, CellItem> factory(int capacityFactor) {
        return prop -> new CellItem(prop,
                capacityFactor * TinactoryConfig.INSTANCE.baseFluidCellSize.get());
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
            return 0xFFFFFFFF;
        }
        return fluid.getFluid().getAttributes().getColor();
    }

    @Override
    public void appendHoverText(ItemStack item, @Nullable Level world,
                                List<Component> components, TooltipFlag flag) {
        var fluid = getFluid(item);
        var amount = I18n.tr("tinactory.tooltip.fluidCell",
                ClientUtil.fluidAmount(fluid), ClientUtil.fluidAmount(capacity));
        components.add(ClientUtil.fluidName(fluid).withStyle(ChatFormatting.GRAY));
        components.add(amount.withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(modLoc("fluid_cell"),
                new FluidHandlerItemStack(event.getObject(), capacity));
    }
}