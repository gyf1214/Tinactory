package org.shsts.tinactory.unit.fixture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;

public record TestIngredientKey(PortType type, String id, String nbt) implements IIngredientKey {
    private static final Codec<TestIngredientKey> RAW_CODEC = Codec.STRING.comapFlatMap(
        TestIngredientKey::decode,
        TestIngredientKey::encode
    );

    public static final Codec<IIngredientKey> CODEC = RAW_CODEC.xmap(
        key -> key,
        key -> (TestIngredientKey) key
    );

    public static TestIngredientKey item(String id, String nbt) {
        return new TestIngredientKey(PortType.ITEM, id, nbt);
    }

    public static TestIngredientKey fluid(String id, String nbt) {
        return new TestIngredientKey(PortType.FLUID, id, nbt);
    }

    private static DataResult<TestIngredientKey> decode(String encoded) {
        var parts = encoded.split("\\|", 3);
        if (parts.length != 3) {
            return DataResult.error("Invalid test key encoding: " + encoded);
        }
        var type = switch (parts[0]) {
            case "item" -> PortType.ITEM;
            case "fluid" -> PortType.FLUID;
            default -> null;
        };
        if (type == null) {
            return DataResult.error("Invalid test key type: " + parts[0]);
        }
        return DataResult.success(new TestIngredientKey(type, parts[1], parts[2]));
    }

    private static String encode(TestIngredientKey key) {
        var typeName = switch (key.type()) {
            case ITEM -> "item";
            case FLUID -> "fluid";
            case NONE -> throw new IllegalArgumentException("Unsupported test key type: NONE");
        };
        return typeName + "|" + key.id() + "|" + key.nbt();
    }

    @Override
    public int compareTo(IIngredientKey other) {
        if (!(other instanceof TestIngredientKey typed)) {
            throw new IllegalArgumentException("Expected TestIngredientKey");
        }
        var byType = Integer.compare(type.ordinal(), typed.type.ordinal());
        if (byType != 0) {
            return byType;
        }
        var byId = id.compareTo(typed.id);
        if (byId != 0) {
            return byId;
        }
        return nbt.compareTo(typed.nbt);
    }

    @Override
    public String toString() {
        return nbt.isEmpty() ? id : id + nbt;
    }
}
