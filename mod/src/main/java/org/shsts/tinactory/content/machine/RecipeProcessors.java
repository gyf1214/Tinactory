package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.shsts.tinactory.content.electric.Generator;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.content.multiblock.CoilMachine;
import org.shsts.tinactory.content.multiblock.FusionReactor;
import org.shsts.tinactory.content.multiblock.MultiblockProcessor;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.machine.IRecipeProcessor;
import org.shsts.tinactory.core.machine.ProcessingMachine;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.integration.machine.MachineProcessor;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.recipe.IRecipeManager;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.shsts.tinactory.AllRecipes.MARKER;
import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeProcessors {
    private static final String ID = "machine/recipe_processor";

    private static Supplier<IRecipeManager> recipeManager(BlockEntity be) {
        return () -> CORE.recipeManager(Objects.requireNonNull(be.getLevel()));
    }

    public static <R extends ProcessingRecipe> Function<BlockEntity, IRecipeProcessor<R>> processing(
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType) {
        return be -> new ProcessingMachine<>(recipeType, recipeManager(be), MARKER);
    }

    public static Function<BlockEntity, IRecipeProcessor<ProcessingRecipe>> fusion(
        IRecipeType<? extends IRecipeBuilderBase<ProcessingRecipe>> recipeType,
        FusionReactor.Properties properties) {
        return be -> new ProcessingMachine<>(recipeType, recipeManager(be), MARKER);
    }

    public static Function<BlockEntity, IRecipeProcessor<SmeltingRecipe>> electricFurnace(
        int inputPort, int outputPort, double amperage) {
        return electricFurnace(inputPort, outputPort, amperage, 0);
    }

    public static Function<BlockEntity, IRecipeProcessor<SmeltingRecipe>> electricFurnace(
        int inputPort, int outputPort, double amperage, int baseTemperature) {
        return be -> new ElectricFurnace(be, inputPort, outputPort, amperage, baseTemperature);
    }

    public static Function<BlockEntity, IRecipeProcessor<ProcessingRecipe>> generator(
        IRecipeType<ProcessingRecipe.Builder> recipeType) {
        return be -> new Generator(recipeType, recipeManager(be), MARKER);
    }

    public static Function<BlockEntity, IRecipeProcessor<OreAnalyzerRecipe>> oreAnalyzer(
        IRecipeType<OreAnalyzerRecipe.Builder> recipeType) {
        return be -> new OreAnalyzer(recipeType, recipeManager(be), MARKER);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> machine(
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> new MachineProcessor(be, processorFactories, autoRecipe));
    }

    public static <R extends ProcessingRecipe> Function<BlockEntity, IRecipeProcessor<R>> coil(
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType, int baseTemperature) {
        return be -> new CoilMachine<>(recipeType, recipeManager(be), MARKER) {
            @Override
            protected int getRecipeTemperature(R recipe) {
                return baseTemperature;
            }
        };
    }

    public static Function<BlockEntity, IRecipeProcessor<BlastFurnaceRecipe>> blastFurnace(
        IRecipeType<BlastFurnaceRecipe.Builder> recipeType) {
        return be -> new BlastFurnace(recipeType, recipeManager(be), MARKER);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> multiblock(
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> new MultiblockProcessor(be, processorFactories, autoRecipe));
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> fusionMultiblock(
        Collection<Function<BlockEntity, ? extends IRecipeProcessor<?>>> processorFactories,
        boolean autoRecipe,
        FusionReactor.Properties properties) {
        return $ -> $.capability(ID, be -> new MultiblockProcessor(be,
            new FusionReactor(be, processorFactories, autoRecipe, properties)));
    }
}
