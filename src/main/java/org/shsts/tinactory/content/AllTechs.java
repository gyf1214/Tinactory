package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.content.material.OreVariant;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllTechs {
    public static final Map<OreVariant, ResourceLocation> BASE_ORE;
    public static final List<ResourceLocation> LOGISTICS;

    static {
        BASE_ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            BASE_ORE.put(variant, modLoc("ore/" + variant.name().toLowerCase()));
        }

        LOGISTICS = new ArrayList<>();
        for (var i = 0; i < 3; i++) {
            LOGISTICS.add(modLoc("logistics/" + i));
        }
    }

    public static void init() {}

    public static void initRecipes() {}
}
