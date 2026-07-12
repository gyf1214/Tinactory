package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.core.autocraft.api.IMachineConstraint;
import org.shsts.tinactory.core.autocraft.pattern.CraftAmount;
import org.shsts.tinactory.core.autocraft.pattern.CraftPattern;
import org.shsts.tinactory.core.autocraft.pattern.PatternCodec;
import org.shsts.tinactory.core.autocraft.pattern.RecipeTypeConstraint;
import org.shsts.tinactory.core.autocraft.pattern.VoltageConstraint;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public final class TestAutocraftHelper {
    private TestAutocraftHelper() {}

    public static final Codec<CraftAmount> AMOUNT_CODEC;
    public static final Codec<CraftPattern> PATTERN_CODEC;
    public static final StreamCodec<RegistryFriendlyByteBuf, CraftPattern> PATTERN_STREAM_CODEC;
    public static final PatternCodec PATTERN_CODECS;

    static {
        AMOUNT_CODEC = CraftAmount.codec(TestStackKey.CODEC);
        PATTERN_CODEC = CraftPattern.codec(AMOUNT_CODEC, TestMachineConstraint.MACHINE_CONSTRAINT_CODEC);
        PATTERN_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(PATTERN_CODEC);
        PATTERN_CODECS = new PatternCodec(AMOUNT_CODEC, PATTERN_CODEC, PATTERN_STREAM_CODEC);
    }

    public static List<IMachineConstraint> constraints(String recipeTypeId, int voltageTier) {
        return List.of(
            new RecipeTypeConstraint(ResourceLocation.parse(recipeTypeId)),
            new VoltageConstraint(voltageTier));
    }

    public static CraftPattern pattern(UUID id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return pattern(id, inputs, outputs, constraints("tinactory:machine", 1));
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs, List<CraftAmount> outputs) {
        return pattern(uuid(id), inputs, outputs);
    }

    public static CraftPattern pattern(String id, List<CraftAmount> inputs,
        List<CraftAmount> outputs, List<IMachineConstraint> constraints) {

        return pattern(uuid(id), inputs, outputs, constraints);
    }

    public static CraftPattern pattern(UUID id, List<CraftAmount> inputs,
        List<CraftAmount> outputs, List<IMachineConstraint> constraints) {

        return new CraftPattern(id, inputs, outputs, constraints);
    }

    public static UUID uuid(String id) {
        return UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8));
    }
}
