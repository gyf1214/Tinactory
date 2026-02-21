package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.Function;

import static org.shsts.tinactory.core.util.ClientUtil.NUMBER_FORMAT;
import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

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
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world,
        List<Component> tooltip, TooltipFlag isAdvanced) {
        addTooltip(tooltip, "coil", NUMBER_FORMAT.format(temperature));
    }
}
