package org.shsts.tinactory.content.multiblock;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record MultiblockSet(IRecipeType<?> recipeType, Layout layout, IEntry<PrimitiveBlock> block) {
}
