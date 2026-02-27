package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface IMachineConstraintRegistry {
    <T extends IMachineConstraint> void register(IMachineConstraintType<T> type, IMachineConstraintCodec<T> codec);

    IMachineConstraint decode(String typeId, String payload);

    SerializedConstraint encode(IMachineConstraint constraint);

    record SerializedConstraint(String typeId, String payload) {
    }
}
