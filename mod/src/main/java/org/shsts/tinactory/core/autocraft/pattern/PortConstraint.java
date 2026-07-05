package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.IStackKey;
import org.shsts.tinactory.api.logistics.PortDirection;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.util.CodecHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PortConstraint(PortDirection direction, int index, int port)
    implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:port";

    public static final MapCodec<PortConstraint> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        CodecHelper.PORT_DIRECTION_CODEC.fieldOf("direction").forGetter(PortConstraint::direction),
        Codec.INT.fieldOf("index").forGetter(PortConstraint::index),
        Codec.INT.fieldOf("port").forGetter(PortConstraint::port)
    ).apply(instance, PortConstraint::new));

    public PortConstraint {
        if (direction == PortDirection.NONE) {
            throw new IllegalArgumentException("direction must not be NONE");
        }
        if (index < 0) {
            throw new IllegalArgumentException("index must be non-negative");
        }
        if (port < 0) {
            throw new IllegalArgumentException("port must be non-negative");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    @Override
    public boolean matchesRoute(PortDirection dir, int index, IStackKey key, long amount, int port,
        PortType portType) {
        return this.direction != dir || this.index != index || this.port == port;
    }
}
