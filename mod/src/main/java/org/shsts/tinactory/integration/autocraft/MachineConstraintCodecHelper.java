package org.shsts.tinactory.integration.autocraft;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.InputPortConstraint;
import org.shsts.tinactory.core.autocraft.pattern.OutputPortConstraint;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MachineConstraintCodecHelper {
    private MachineConstraintCodecHelper() {}

    public static final Codec<IMachineConstraint> CODEC = Codec.STRING.dispatch(
        IMachineConstraint::typeId,
        MachineConstraintCodecHelper::codec
    );

    private static Codec<? extends IMachineConstraint> codec(String typeId) {
        return switch (typeId) {
            case InputPortConstraint.TYPE_ID -> InputPortConstraint.CODEC;
            case OutputPortConstraint.TYPE_ID -> OutputPortConstraint.CODEC;
            default -> throw new IllegalArgumentException("unknown machine constraint type id: " + typeId);
        };
    }
}
