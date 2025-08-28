package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.core.material.MaterialSet;

import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllMaterials {
    public static final Map<String, MaterialSet> SET = new HashMap<>();
    public static final Map<String, String> ALIASES = new HashMap<>();

    public static MaterialSet.Builder<?> newMaterial(String id) {
        return MaterialSet.builder(Unit.INSTANCE, id)
            .onCreateObject(mat -> SET.put(mat.name, mat));
    }

    public static void alias(String name, String target) {
        ALIASES.put(name, target);
    }

    public static MaterialSet getMaterial(String name) {
        return SET.get(name);
    }

    public static void init() {
        for (var entry : ALIASES.entrySet()) {
            SET.put(entry.getKey(), getMaterial(entry.getValue()));
        }
    }
}
