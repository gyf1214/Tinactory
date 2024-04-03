package org.shsts.tinactory.test;

import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.NumberFormat;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidCell extends Item {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private final int capacity;

    public FluidCell(Properties properties, int capacity) {
        super(properties);
        this.capacity = capacity;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidHandlerItemStack(stack, this.capacity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world,
                                List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        FluidUtil.getFluidHandler(stack).ifPresent(fluidHandler -> {
            var tanks = fluidHandler.getTanks();
            for (var i = 0; i < tanks; i++) {
                var fluid = fluidHandler.getFluidInTank(i);
                var line = new TranslatableComponent("tinactory_test.tooltip.fluid_cell",
                        fluid.getDisplayName(), NUMBER_FORMAT.format(fluid.getAmount()));
                tooltipComponents.add(line.withStyle(ChatFormatting.GRAY));
            }
        });
    }
}
