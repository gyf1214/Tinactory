package org.shsts.tinactory.content.logistics;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.shsts.tinycorelib.api.item.ICapabilityItem;
import org.shsts.tinycorelib.api.registrate.entry.IItemCapability;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.AllCapabilities.BYTES_PROVIDER_ITEM;
import static org.shsts.tinactory.AllCapabilities.FLUID_PORT_ITEM;
import static org.shsts.tinactory.AllCapabilities.ITEM_PORT_ITEM;
import static org.shsts.tinactory.AllDataComponents.ME_FLUID_CELL_CONTENT;
import static org.shsts.tinactory.AllDataComponents.ME_ITEM_CELL_CONTENT;
import static org.shsts.tinactory.TinactoryConfig.CONFIG;
import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MEStorageCell extends Item implements ICapabilityItem {
    private final boolean isFluid;
    private final long bytesLimit;

    public MEStorageCell(Properties properties, boolean isFluid, long bytesLimit) {
        super(properties.stacksTo(1));
        this.isFluid = isFluid;
        this.bytesLimit = bytesLimit;
    }

    public static Function<Properties, MEStorageCell> itemCell(long bytesLimit) {
        return properties -> new MEStorageCell(properties, false, bytesLimit);
    }

    public static Function<Properties, MEStorageCell> fluidCell(long bytesLimit) {
        return properties -> new MEStorageCell(properties, true, bytesLimit);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        BYTES_PROVIDER_ITEM.tryGet(stack)
            .ifPresent(provider -> addTooltip(tooltip, "meStorageCell",
                NUMBER_FORMAT.format(provider.bytesUsed()), NUMBER_FORMAT.format(bytesLimit)));
    }

    @Override
    @Nullable
    public <T> T getCapability(ItemStack stack, IItemCapability<T> capability) {
        if (isFluid) {
            var port = fluidPort(stack);
            if (FLUID_PORT_ITEM.is(capability) || BYTES_PROVIDER_ITEM.is(capability)) {
                return capability.cast(port);
            }
        } else {
            var port = itemPort(stack);
            if (ITEM_PORT_ITEM.is(capability) || BYTES_PROVIDER_ITEM.is(capability)) {
                return capability.cast(port);
            }
        }
        return null;
    }

    private DigitalCellPort.Item itemPort(ItemStack stack) {
        return new DigitalCellPort.Item(
            stack,
            ME_ITEM_CELL_CONTENT,
            bytesLimit,
            CONFIG.bytesPerItemType.get(),
            CONFIG.bytesPerItem.get());
    }

    private DigitalCellPort.Fluid fluidPort(ItemStack stack) {
        return new DigitalCellPort.Fluid(
            stack,
            ME_FLUID_CELL_CONTENT,
            bytesLimit,
            CONFIG.bytesPerFluidType.get(),
            CONFIG.bytesPerFluid.get());
    }
}
