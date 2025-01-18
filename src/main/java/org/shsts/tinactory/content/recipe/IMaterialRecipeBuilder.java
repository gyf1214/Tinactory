package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.material.MaterialSet;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.core.ISelf;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMaterialRecipeBuilder<S extends ProcessingRecipe.BuilderBase<?, S>>
    extends ISelf<S> {
    default S input(MaterialSet material, String sub, float amount) {
        if (material.hasItem(sub)) {
            return self().inputItem(material.tag(sub), (int) amount);
        } else {
            return self().inputFluid(material.fluid(sub), material.fluidAmount(sub, amount));
        }
    }

    default S input(MaterialSet material, float amount) {
        return material.hasItem("dust") ? input(material, "dust", amount) : input(material, "fluid", amount);
    }

    default S input(MaterialSet material) {
        return input(material, 1f);
    }

    default S output(MaterialSet material, String sub, float amount) {
        if (material.hasItem(sub)) {
            return self().outputItem(material.entry(sub), (int) amount);
        } else {
            return self().outputFluid(material.fluid(sub), material.fluidAmount(sub, amount));
        }
    }

    default S output(MaterialSet material, float amount) {
        return material.hasItem("dust") ? output(material, "dust", amount) : output(material, "fluid", amount);
    }

    default S output(MaterialSet material) {
        return output(material, 1f);
    }
}
