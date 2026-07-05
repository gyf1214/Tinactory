package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.machine.IMachineProcessor;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record RecipeTypeConstraint(ResourceLocation recipeTypeId) implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:recipe_type";
    public static final MapCodec<RecipeTypeConstraint> CODEC = ResourceLocation.CODEC.fieldOf("recipeTypeId")
        .xmap(RecipeTypeConstraint::new, RecipeTypeConstraint::recipeTypeId);

    public RecipeTypeConstraint {
        if (recipeTypeId.getPath().isBlank()) {
            throw new IllegalArgumentException("recipeTypeId path must not be blank");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    @Override
    public boolean matches(IMachine machine, Voltage voltage) {
        return machine.processor()
            .filter(IMachineProcessor.class::isInstance)
            .map(IMachineProcessor.class::cast)
            .filter(processor -> processor.supportsRecipeType(recipeTypeId))
            .isPresent();
    }
}
