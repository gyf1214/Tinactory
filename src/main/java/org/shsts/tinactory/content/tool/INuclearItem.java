package org.shsts.tinactory.content.tool;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.shsts.tinactory.content.multiblock.INuclearCell;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface INuclearItem {
    ItemStack tickCell(ItemStack stack, INuclearCell cell);
}
