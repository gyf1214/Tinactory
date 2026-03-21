package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

import java.util.Locale;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record InputPortConstraint(int inputSlotIndex, @Nullable Integer portIndex, @Nullable Direction direction)
    implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:input_port";
    private static final Codec<Direction> DIRECTION_CODEC =
        Codec.STRING.xmap(Direction::decode, Direction::encode);
    public static final Codec<InputPortConstraint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("inputSlotIndex").forGetter(InputPortConstraint::inputSlotIndex),
        Codec.INT.optionalFieldOf("portIndex").forGetter($ -> Optional.ofNullable($.portIndex())),
        DIRECTION_CODEC.optionalFieldOf("direction").forGetter($ -> Optional.ofNullable($.direction()))
    ).apply(instance, (inputSlotIndex, portIndex, direction) ->
        new InputPortConstraint(inputSlotIndex, portIndex.orElse(null), direction.orElse(null))));

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
}
