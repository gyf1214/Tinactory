package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.machine.ElectricFurnace;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.content.multiblock.CoilMachine;
import org.shsts.tinactory.content.multiblock.MultiblockProcessor;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Collection;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeProcessors {
    private static final String ID = "machine/recipe_processor";
    public static final long PROGRESS_PER_TICK = 256;

    public static <P, R extends ProcessingRecipe,
        B extends IRecipeBuilderBase<R>> Transformer<IBlockEntityTypeBuilder<P>> machine(
        IRecipeType<B> type) {
        var processor = new ProcessingMachine<>(type);
        return $ -> $.capability(ID, be -> new MachineProcessor(be, List.of(processor), true));
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> machine(
        Collection<IRecipeProcessor<?>> processors, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> new MachineProcessor(be, processors, autoRecipe));
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> electricFurnace(double amperage) {
        var processor = new ElectricFurnace(amperage);
        return $ -> $.capability(ID, be -> new MachineProcessor(be, List.of(processor), true));
    }

    public static <P, R extends ProcessingRecipe,
        B extends IRecipeBuilderBase<R>> Transformer<IBlockEntityTypeBuilder<P>> multiblock(
        IRecipeType<B> type, boolean autoRecipe) {
        var processor = new ProcessingMachine<>(type);
        return $ -> $.capability(ID, be -> new MultiblockProcessor(be, List.of(processor), autoRecipe));
    }

    public static <P, R extends ProcessingRecipe,
        B extends IRecipeBuilderBase<R>> Transformer<IBlockEntityTypeBuilder<P>> coil(
        IRecipeType<B> type, boolean autoRecipe, int baseTemperature) {
        var processor = new CoilMachine<>(type) {
            @Override
            protected int getRecipeTemperature(R recipe) {
                return baseTemperature;
            }
        };
        return $ -> $.capability(ID, be -> new MultiblockProcessor(be, List.of(processor), autoRecipe));
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> blastFurnace(
        IRecipeType<BlastFurnaceRecipe.Builder> type) {
        var processor = new BlastFurnace(type);
        return $ -> $.capability(ID, be -> new MultiblockProcessor(be, List.of(processor), true));
    }
}
