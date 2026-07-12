package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.content.multiblock.Lithography;
import org.shsts.tinactory.core.recipe.StackIngredient;
import org.shsts.tinactory.integration.multiblock.MultiblockInterface;
import org.shsts.tinactory.integration.recipe.ItemsIngredient;

import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EngravingRecipe extends CleanRecipe {
    public static final MapCodec<EngravingRecipe> CODEC = codec(EngravingRecipe::new);

    public EngravingRecipe(List<Input> inputs, List<Output> outputs, long workTicks, long voltage, long power,
        double minCleanness, double maxCleanness) {
        super(inputs, outputs, workTicks, voltage, power, minCleanness, maxCleanness);
    }

    private Optional<Lithography> getLithography(IMachine machine) {
        if (!(machine instanceof MultiblockInterface multiblockInterface)) {
            return Optional.empty();
        }
        return multiblockInterface.getMultiblock()
            .flatMap($ -> $ instanceof Lithography lithography ?
                Optional.of(lithography) : Optional.empty());
    }

    private boolean matchLens(Lithography lithography, IProcessingIngredient ingredient) {
        var lens = lithography.getLens();
        if (ingredient instanceof ItemsIngredient items) {
            return lens.stream().anyMatch($ -> items.ingredient.test(new ItemStack($)));
        } else if (ingredient instanceof StackIngredient<?> item && item.type() == PortType.ITEM) {
            return lens.stream().anyMatch($ -> ((ItemStack) item.stack()).is($));
        } else {
            return false;
        }
    }

    @Override
    public boolean canCraft(IMachine machine) {
        return super.canCraft(machine) && getLithography(machine)
            .map($ -> inputs.stream().allMatch(input ->
                input.port() != 1 || matchLens($, input.ingredient())))
            .orElse(true);
    }

    @Override
    protected boolean matchInputs(IMachine machine, IContainer container, int parallel) {
        return getLithography(machine)
            .map($ -> inputs.stream().allMatch(input ->
                input.port() == 1 || canConsumeInput(container, input, parallel)))
            .orElseGet(() -> super.matchInputs(machine, container, parallel));
    }

    @Override
    protected double getCleanness(IMachine machine, Level world, BlockPos pos) {
        var factor = getLithography(machine).map(Lithography::getCleannessFactor).orElse(1d);
        return factor * super.getCleanness(machine, world, pos);
    }

}
