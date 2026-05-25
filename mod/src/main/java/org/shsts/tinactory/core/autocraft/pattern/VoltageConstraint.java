package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.electric.Voltage;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record VoltageConstraint(int voltageTier) implements IMachineConstraint {
    public static final String TYPE_ID = "tinactory:voltage";
    public static final Codec<VoltageConstraint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("voltageTier").forGetter(VoltageConstraint::voltageTier)
    ).apply(instance, VoltageConstraint::new));

    public VoltageConstraint {
        if (voltageTier < 0) {
            throw new IllegalArgumentException("voltageTier must be non-negative");
        }
    }

    @Override
    public String typeId() {
        return TYPE_ID;
    }

    @Override
    public boolean matches(IMachine machine, Voltage voltage) {
        return voltage.rank >= voltageTier;
    }
}
