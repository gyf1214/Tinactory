package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record TargetRecipeConstraint(ResourceLocation recipeId) implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:target_recipe";
    public static final Codec<TargetRecipeConstraint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ResourceLocation.CODEC.fieldOf("recipeId").forGetter(TargetRecipeConstraint::recipeId)
    ).apply(instance, TargetRecipeConstraint::new));

    public TargetRecipeConstraint {
        if (recipeId.getPath().isBlank()) {
            throw new IllegalArgumentException("recipeId path must not be blank");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }
}
