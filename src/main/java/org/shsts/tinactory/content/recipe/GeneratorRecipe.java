package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.electric.IElectricMachine;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.core.recipe.DisplayInputRecipe;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Optional;
import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneratorRecipe extends DisplayInputRecipe {
    private GeneratorRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    @Override
    protected boolean matchOutputs(IContainer container, Random random) {
        // no check output
        return true;
    }

    @Override
    protected boolean matchElectric(Optional<IElectricMachine> electric) {
        return electric.filter($ -> $.getVoltage() == voltage).isPresent();
    }

    public static Builder builder(IRecipeType<Builder> parent, ResourceLocation loc) {
        return new Builder(parent, loc) {
            @Override
            protected void validate() {
                assert power > 0 : loc;
                assert workTicks > 0 : loc;
            }

            @Override
            protected ProcessingRecipe createObject() {
                return new GeneratorRecipe(this);
            }
        };
    }
}
