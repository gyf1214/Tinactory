package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.multiblock.MultiblockProcessor;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;

import java.util.Collection;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeProcessors {
    private static final String ID = "machine/recipe_processor";
    public static final long PROGRESS_PER_TICK = 256;

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> machine(
        Collection<IRecipeProcessor<?>> processors, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> new MachineProcessor(be, processors, autoRecipe));
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> multiblock(
        Collection<IRecipeProcessor<?>> processors, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> new MultiblockProcessor(be, processors, autoRecipe));
    }
}
