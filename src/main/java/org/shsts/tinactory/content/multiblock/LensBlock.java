package org.shsts.tinactory.content.multiblock;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.GlassBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class LensBlock extends GlassBlock {
    private final List<Supplier<? extends Item>> lensSupp;
    @Nullable
    private List<Item> lens = null;

    public LensBlock(Properties properties, Collection<Supplier<? extends Item>> lens) {
        super(properties);
        this.lensSupp = new ArrayList<>(lens);
    }

    public Collection<Item> getLens() {
        if (lens == null) {
            lens = lensSupp.stream().map($ -> (Item) $.get()).toList();
        }
        return lens;
    }
}
