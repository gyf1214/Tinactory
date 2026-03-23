package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

import java.util.Locale;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PortConstraint(PortDirection direction, int slotIndex, @Nullable Integer portIndex)
    implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:port";
    private static final Codec<PortDirection> DIRECTION_CODEC =
        Codec.STRING.xmap(PortConstraint::decodeDirection, PortConstraint::encodeDirection);
    public static final Codec<PortConstraint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        DIRECTION_CODEC.fieldOf("direction").forGetter(PortConstraint::direction),
        Codec.INT.fieldOf("slotIndex").forGetter(PortConstraint::slotIndex),
        Codec.INT.optionalFieldOf("portIndex").forGetter($ -> Optional.ofNullable($.portIndex()))
    ).apply(instance, (direction, slotIndex, portIndex) ->
        new PortConstraint(direction, slotIndex, portIndex.orElse(null))));

    public PortConstraint {
        if (direction == PortDirection.NONE) {
            throw new IllegalArgumentException("direction must not be NONE");
        }
        if (slotIndex < 0) {
            throw new IllegalArgumentException("slotIndex must be non-negative");
        }
        if (portIndex != null && portIndex < 0) {
            throw new IllegalArgumentException("portIndex must be non-negative");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    private static PortDirection decodeDirection(String raw) {
        return PortDirection.valueOf(raw.toUpperCase(Locale.ROOT));
    }

    private static String encodeDirection(PortDirection direction) {
        return direction.name().toLowerCase(Locale.ROOT);
    }
}
