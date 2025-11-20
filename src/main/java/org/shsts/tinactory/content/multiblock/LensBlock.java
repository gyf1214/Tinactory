package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.core.util.ClientUtil.addTooltip;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LensBlock extends Block {
    private final List<Supplier<? extends Item>> lensSupp;
    @Nullable
    private List<Item> lens = null;

    public LensBlock(Properties properties, Collection<Supplier<? extends Item>> lens) {
        super(properties);
        this.lensSupp = new ArrayList<>(lens);
    }

    public static Function<Properties, LensBlock> factory(Collection<Supplier<? extends Item>> lens) {
        return properties -> new LensBlock(properties, lens);
    }

    public Collection<Item> getLens() {
        if (lens == null) {
            lens = lensSupp.stream().map($ -> (Item) $.get()).toList();
        }
        return lens;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter world,
        List<Component> tooltip, TooltipFlag isAdvanced) {
        addTooltip(tooltip, "lens");
        var lens = getLens();
        for (var len : lens) {
            addTooltip(tooltip, "list", (new ItemStack(len)).getHoverName());
        }
    }
}
