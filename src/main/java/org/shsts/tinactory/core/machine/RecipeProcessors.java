package org.shsts.tinactory.core.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import org.shsts.tinactory.content.electric.Generator;
import org.shsts.tinactory.content.machine.ElectricFurnace;
import org.shsts.tinactory.content.machine.OreAnalyzer;
import org.shsts.tinactory.content.multiblock.BlastFurnace;
import org.shsts.tinactory.content.multiblock.CoilMachine;
import org.shsts.tinactory.content.multiblock.MultiblockProcessor;
import org.shsts.tinactory.content.recipe.BlastFurnaceRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Collection;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeProcessors {
    private static final String ID = "machine/recipe_processor";
    public static final long PROGRESS_PER_TICK = 256;

    public static <R extends ProcessingRecipe> Supplier<IRecipeProcessor<R>> processing(
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType) {
        return () -> new ProcessingMachine<>(recipeType);
    }

    public static Supplier<IRecipeProcessor<SmeltingRecipe>> electricFurnace(double amperage) {
        return () -> new ElectricFurnace(amperage);
    }

    public static Supplier<IRecipeProcessor<ProcessingRecipe>> generator(
        IRecipeType<ProcessingRecipe.Builder> recipeType) {
        return () -> new Generator(recipeType);
    }

    public static Supplier<IRecipeProcessor<OreAnalyzerRecipe>> oreAnalyzer(
        IRecipeType<OreAnalyzerRecipe.Builder> recipeType) {
        return () -> new OreAnalyzer(recipeType);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> machine(
        Collection<Supplier<? extends IRecipeProcessor<?>>> processorFactories, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> {
            var processors = processorFactories.stream()
                .map(Supplier::get)
                .toList();
            return new MachineProcessor(be, processors, autoRecipe);
        });
    }

    public static <R extends ProcessingRecipe> Supplier<IRecipeProcessor<R>> coil(
        IRecipeType<? extends IRecipeBuilderBase<R>> recipeType, int baseTemperature) {
        return () -> new CoilMachine<>(recipeType) {
            @Override
            protected int getRecipeTemperature(R recipe) {
                return baseTemperature;
            }
        };
    }

    public static Supplier<IRecipeProcessor<BlastFurnaceRecipe>> blastFurnace(
        IRecipeType<BlastFurnaceRecipe.Builder> recipeType) {
        return () -> new BlastFurnace(recipeType);
    }

    public static <P> Transformer<IBlockEntityTypeBuilder<P>> multiblock(
        Collection<Supplier<? extends IRecipeProcessor<?>>> processorFactories, boolean autoRecipe) {
        return $ -> $.capability(ID, be -> {
            var processors = processorFactories.stream()
                .map(Supplier::get)
                .toList();
            return new MultiblockProcessor(be, processors, autoRecipe);
        });
    }
}
