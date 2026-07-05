package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record VoltageConstraint(int tier) implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:voltage";
    public static final MapCodec<VoltageConstraint> CODEC = Codec.INT.fieldOf("voltageTier")
        .xmap(VoltageConstraint::new, VoltageConstraint::tier);

    public VoltageConstraint {
        if (tier < 0) {
            throw new IllegalArgumentException("voltageTier must be non-negative");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    @Override
    public boolean matches(IMachine machine, Voltage voltage) {
        return voltage.rank >= tier;
    }
}
