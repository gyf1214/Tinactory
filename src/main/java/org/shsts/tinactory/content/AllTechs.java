package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.registrate.builder.TechBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllTechs {
    public static final Map<OreVariant, ResourceLocation> ORE;

    static {
        ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            ore(variant);
        }
    }

    @Nullable
    private static ResourceLocation BASE_TECH = null;

    private static TechBuilder<?> techBuilder(String id) {
        var builder = REGISTRATE.tech(id);
        if (BASE_TECH != null) {
            builder.depends(BASE_TECH);
        }
        return builder;
    }

    private static TechBuilder<?> base(String id) {
        return techBuilder(id).onCreateObject(tech -> BASE_TECH = tech);
    }

    private static void ore(OreVariant variant) {
        ORE.put(variant, base("ore/" + variant.name().toLowerCase())
                .maxProgress(200L * (1L << (long) variant.rank))
                .buildObject());
    }

    public static void init() {}

    public static void initRecipes() {
        for (var entry : ORE.entrySet()) {
            var variant = entry.getKey();
            var tech = entry.getValue();
            AllRecipes.RESEARCH.recipe(entry.getValue())
                    .target(tech)
                    .inputItem(() -> variant.baseItem)
                    .voltage(variant.voltage)
                    .workTicks(200)
                    .build();
        }
    }
}
