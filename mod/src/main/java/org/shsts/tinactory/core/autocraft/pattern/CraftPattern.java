package org.shsts.tinactory.core.autocraft.pattern;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.UUIDUtil;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;

import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record CraftPattern(
    UUID patternUuid,
    List<CraftAmount> inputs,
    List<CraftAmount> outputs,
    List<IMachineConstraint> constraints) {
    public CraftPattern {
        inputs = List.copyOf(inputs);
        outputs = List.copyOf(outputs);
        constraints = List.copyOf(constraints);
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs must not be empty");
        }
    }

    public CraftPattern withUuid(UUID uuid) {
        return new CraftPattern(uuid, inputs, outputs, constraints);
    }

    public static Codec<CraftPattern> codec(Codec<CraftAmount> amountCodec,
        Codec<IMachineConstraint> constraintCodec) {
        return RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("patternUuid").forGetter(CraftPattern::patternUuid),
            amountCodec.listOf().fieldOf("inputs").forGetter(CraftPattern::inputs),
            amountCodec.listOf().fieldOf("outputs").forGetter(CraftPattern::outputs),
            constraintCodec.listOf().fieldOf("constraints").forGetter(CraftPattern::constraints)
        ).apply(instance, CraftPattern::new));
    }
}
