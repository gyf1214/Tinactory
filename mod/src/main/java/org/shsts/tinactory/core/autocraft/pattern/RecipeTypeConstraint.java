package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
    public static final Codec<RecipeTypeConstraint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("recipeTypeId").forGetter(RecipeTypeConstraint::recipeTypeId)
    ).apply(instance, RecipeTypeConstraint::new));

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
