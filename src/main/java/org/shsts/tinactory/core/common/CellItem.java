package org.shsts.tinactory.core.common;

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
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.core.util.ClientUtil;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CellItem extends CapabilityItem {
    public final MaterialSet material;
    private final int capacity;

    public CellItem(Properties properties, MaterialSet material, int capacity) {
        super(properties);
        this.material = material;
        this.capacity = capacity;
    }

    public static Function<Properties, CellItem> factory(MaterialSet material, int capacityFactor) {
        return prop -> new CellItem(prop, material,
                capacityFactor * TinactoryConfig.INSTANCE.baseFluidCellSize.get());
    }

    private static FluidStack getFluid(ItemStack item) {
        return item.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY)
                .orElseThrow(NoSuchElementException::new)
                .getFluidInTank(0);
    }

    public static int getTint(ItemStack item, int index) {
        if (index != 1) {
            return 0xFFFFFFFF;
        }
        var fluid = getFluid(item);
        if (fluid.isEmpty()) {
            return 0;
        }
        return fluid.getFluid().getAttributes().getColor();
    }

    @Override
    public void appendHoverText(ItemStack item, @Nullable Level world,
                                List<Component> components, TooltipFlag flag) {
        components.addAll(ClientUtil.fluidTooltip(getFluid(item), true));
    }

    @Override
    public void attachCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        event.addCapability(modLoc("fluid_cell"),
                new FluidHandlerItemStack(event.getObject(), capacity));
    }
}
