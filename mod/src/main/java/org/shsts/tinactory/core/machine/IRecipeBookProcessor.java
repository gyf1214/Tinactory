package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.gui.client.IRecipeBookItem;
import org.shsts.tinycorelib.api.core.DistLazy;

import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IRecipeBookProcessor {
    DistLazy<List<IRecipeBookItem>> recipeBookItems();
}
