package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.electric.Voltage;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PowerBlock extends Block {
    public final Voltage voltage;
    public final long capacity;

    public PowerBlock(Properties properties, Voltage voltage, long capacity) {
        super(properties);
        this.voltage = voltage;
        this.capacity = capacity;
    }

    public static Function<Properties, PowerBlock> factory(Voltage v, long capacity) {
        return properties -> new PowerBlock(properties, v, capacity);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world, List<Component> tooltip,
        TooltipFlag isAdvanced) {
        addTooltip(tooltip, "machineVoltage", NUMBER_FORMAT.format(voltage.value), voltage.displayName());
        addTooltip(tooltip, "powerBlock", NUMBER_FORMAT.format(capacity));
    }
}
