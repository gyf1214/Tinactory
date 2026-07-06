package org.shsts.tinactory.integration.autocraft;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.MachineConstraintHelper;
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.integration.logistics.StackHelper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class PatternHelper {
    private PatternHelper() {}

    public static final Codec<IMachineConstraint> CONSTRAINT_CODEC;
    public static final Codec<CraftAmount> AMOUNT_CODEC;
    public static final Codec<CraftPattern> PATTERN_CODEC;
    public static final PatternCodec PATTERN_CODECS;

    static {
        CONSTRAINT_CODEC = Codec.STRING.dispatch(IMachineConstraint::typeId, MachineConstraintHelper::codec);

        AMOUNT_CODEC = CraftAmount.codec(StackHelper.KEY_CODEC);
        PATTERN_CODEC = CraftPattern.codec(AMOUNT_CODEC, CONSTRAINT_CODEC);

        PATTERN_CODECS = new PatternCodec(AMOUNT_CODEC, PATTERN_CODEC,
            ByteBufCodecs.fromCodecWithRegistries(PATTERN_CODEC));
    }
}
