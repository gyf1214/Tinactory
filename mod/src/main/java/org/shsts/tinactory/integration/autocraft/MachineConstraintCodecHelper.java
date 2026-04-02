package org.shsts.tinactory.integration.autocraft;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.PortConstraint;

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
            case PortConstraint.TYPE_ID -> PortConstraint.CODEC;
            default -> throw new IllegalArgumentException("unknown machine constraint type id: " + typeId);
        };
    }
}
