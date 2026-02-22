package org.shsts.tinactory.core.autocraft.api;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.model.IMachineConstraint;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class MachineConstraintRegistry implements IMachineConstraintRegistry {
    private final Map<String, CodecEntry<?>> byTypeId = new HashMap<>();
    private final Map<Class<?>, CodecEntry<?>> byClass = new HashMap<>();

    @Override
    public <T extends IMachineConstraint> void register(
        IMachineConstraintType<T> type,
        IMachineConstraintCodec<T> codec) {
        var entry = new CodecEntry<>(type, codec);
        byTypeId.put(type.id(), entry);
        byClass.put(type.constraintClass(), entry);
    }

    @Override
    public IMachineConstraint decode(String typeId, String payload) {
        var entry = byTypeId.get(typeId);
        if (entry == null) {
            throw new IllegalArgumentException("unknown machine constraint type id: " + typeId);
        }
        return entry.decode(payload);
    }

    @Override
    public SerializedConstraint encode(IMachineConstraint constraint) {
        var entry = byClass.get(constraint.getClass());
        if (entry == null) {
            throw new IllegalArgumentException(
                "no machine constraint codec for class: " + constraint.getClass().getName());
        }
        return entry.encode(constraint);
    }

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private record CodecEntry<T extends IMachineConstraint>(
        IMachineConstraintType<T> type,
        IMachineConstraintCodec<T> codec) {
        private IMachineConstraint decode(String payload) {
            return codec.decode(payload);
        }

        @SuppressWarnings("unchecked")
        private SerializedConstraint encode(IMachineConstraint constraint) {
            return new SerializedConstraint(type.id(), codec.encode((T) constraint));
        }
    }
}
