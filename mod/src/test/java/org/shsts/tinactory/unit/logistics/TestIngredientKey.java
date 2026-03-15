package org.shsts.tinactory.unit.logistics;

import org.shsts.tinactory.api.logistics.PortType;
import org.shsts.tinactory.core.logistics.IIngredientKey;

record TestIngredientKey(PortType type, String id, String nbt) implements IIngredientKey {
    static IIngredientKey item(String id, String nbt) {
        return new TestIngredientKey(PortType.ITEM, id, nbt);
    }

    static IIngredientKey fluid(String id, String nbt) {
        return new TestIngredientKey(PortType.FLUID, id, nbt);
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
