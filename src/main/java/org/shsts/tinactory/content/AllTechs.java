package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.registrate.builder.TechBuilder;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllTechs {
    public static final Map<OreVariant, ResourceLocation> ORE;
    public static final List<ResourceLocation> LOGISTICS;

    static {
        resetBase();
        ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            ore(variant);
        }

        resetBase();
        LOGISTICS = new ArrayList<>();
        for (var i = 0; i < 3; i++) {
            LOGISTICS.add(base("logistics/" + i)
                    .maxProgress(100L * (1L << (long) (2 * i)))
                    .modifier("logistics_level", 1)
                    .buildObject());
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

    private static void resetBase() {
        BASE_TECH = null;
    }

    private static TechBuilder<?> base(String id) {
        return techBuilder(id).onCreateObject(tech -> BASE_TECH = tech);
    }

    private static void ore(OreVariant variant) {
        ORE.put(variant, base("ore/" + variant.name().toLowerCase())
                .maxProgress(200L * (1L << (long) variant.rank))
                .displayItem(variant.baseItem)
                .buildObject());
    }

    public static void init() {}

    public static void initRecipes() {
        for (var entry : ORE.entrySet()) {
            var variant = entry.getKey();
            var tech = entry.getValue();
            AllRecipes.RESEARCH.recipe(tech)
                    .target(tech)
                    .inputItem(() -> variant.baseItem)
                    .voltage(variant.voltage)
                    .workTicks(200)
                    .build();
        }

        AllRecipes.RESEARCH.recipe(LOGISTICS.get(0))
                .target(LOGISTICS.get(0))
                .inputItem(AllItems.componentSet(Voltage.LV).conveyor)
                .voltage(Voltage.LV)
                .workTicks(200)
                .build();
    }
}
