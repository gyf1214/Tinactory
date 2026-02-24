package org.shsts.tinactory.core.autocraft.model;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintCodec;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraintType;

import java.util.Locale;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record InputPortConstraint(int inputSlotIndex, @Nullable Integer portIndex, @Nullable Direction direction)
    implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:input_port";

    public InputPortConstraint {
        if (inputSlotIndex < 0) {
            throw new IllegalArgumentException("inputSlotIndex must be non-negative");
        }
        if (portIndex != null && portIndex < 0) {
            throw new IllegalArgumentException("portIndex must be non-negative");
        }
        if (portIndex == null && direction == null) {
            throw new IllegalArgumentException("at least one selector must be set");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    public enum Direction {
        INPUT,
        OUTPUT;

        private static Direction decode(String raw) {
            return valueOf(raw.toUpperCase(Locale.ROOT));
        }

        private String encode() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final class Type implements IMachineConstraintType<InputPortConstraint> {
        @Override
        public String id() {
            return TYPE_ID;
        }

        @Override
        public Class<InputPortConstraint> constraintClass() {
            return InputPortConstraint.class;
        }
    }

    public static final class Codec implements IMachineConstraintCodec<InputPortConstraint> {
        @Override
        public String encode(InputPortConstraint constraint) {
            var portSelector = constraint.portIndex() == null ? "" : Integer.toString(constraint.portIndex());
            var directionSelector = constraint.direction() == null ? "" : constraint.direction().encode();
            return constraint.inputSlotIndex() + "," + portSelector + "," + directionSelector;
        }

        @Override
        public InputPortConstraint decode(String payload) {
            var fields = payload.split(",", -1);
            if (fields.length != 3) {
                throw new IllegalArgumentException("invalid input port constraint payload");
            }
            try {
                var slotIndex = Integer.parseInt(fields[0]);
                var portIndex = fields[1].isEmpty() ? null : Integer.valueOf(fields[1]);
                var direction = fields[2].isEmpty() ? null : Direction.decode(fields[2]);
                return new InputPortConstraint(slotIndex, portIndex, direction);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid input port constraint payload", e);
            }
        }
    }
}
