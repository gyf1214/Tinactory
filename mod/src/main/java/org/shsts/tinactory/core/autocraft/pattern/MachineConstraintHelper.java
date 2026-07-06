package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineConstraintHelper {
    public static MapCodec<? extends IMachineConstraint> codec(String typeId) {
        return switch (typeId) {
            case PortConstraint.TYPE_ID -> PortConstraint.CODEC;
            case RecipeTypeConstraint.TYPE_ID -> RecipeTypeConstraint.CODEC;
            case TargetRecipeConstraint.TYPE_ID -> TargetRecipeConstraint.CODEC;
            case VoltageConstraint.TYPE_ID -> VoltageConstraint.CODEC;
            default -> throw new IllegalArgumentException("unknown machine constraint type id: " + typeId);
        };
    }
}
