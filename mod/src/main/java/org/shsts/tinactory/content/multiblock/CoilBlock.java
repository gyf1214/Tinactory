package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.integration.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.integration.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CoilBlock extends Block {
    public final int temperature;

    public CoilBlock(Properties properties, int temperature) {
        super(properties);
        this.temperature = temperature;
    }

    public static Function<Properties, CoilBlock> factory(int temperature) {
        return prop -> new CoilBlock(prop, temperature);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {
        addTooltip(tooltip, "coil", NUMBER_FORMAT.format(temperature));
    }
}
