package org.shsts.tinactory.integration.logistics;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IStackKey;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class IngredientKeyCodecHelper {
    private IngredientKeyCodecHelper() {}

    public static final Codec<IStackKey> CODEC = Codec.STRING.dispatch(
        IngredientKeyCodecHelper::codecName,
        IngredientKeyCodecHelper::codec
    );

    private static String codecName(IStackKey key) {
        return switch (key.type()) {
            case ITEM -> "item";
            case FLUID -> "fluid";
            case NONE -> throw new IllegalArgumentException("Unsupported ingredient key type: NONE");
        };
    }

    private static Codec<? extends IStackKey> codec(String name) {
        return switch (name) {
            case "item" -> ItemPortAdapter.keyCodec();
            case "fluid" -> FluidPortAdapter.keyCodec();
            default -> throw new IllegalArgumentException("Unknown ingredient key codec: " + name);
        };
    }

    public static Codec<? extends IStackKey> codec(PortType type) {
        return switch (type) {
            case ITEM -> ItemPortAdapter.keyCodec();
            case FLUID -> FluidPortAdapter.keyCodec();
            case NONE -> throw new IllegalArgumentException("Unsupported ingredient key type: NONE");
        };
    }
}
