package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import org.shsts.tinactory.api.tech.ITechnology;
import org.shsts.tinactory.content.material.OreVariant;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.core.util.LocHelper.modLoc;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllTechs {
    public static final Map<OreVariant, ResourceLocation> BASE_ORE;
    public static final List<ResourceLocation> LOGISTICS;

    static {
        BASE_ORE = new HashMap<>();
        for (var variant : OreVariant.values()) {
            BASE_ORE.put(variant, tech("ore/" + variant.name().toLowerCase()));
        }

        LOGISTICS = new ArrayList<>();
        for (var i = 0; i < 3; i++) {
            LOGISTICS.add(tech("logistics/" + i));
        }
    }

    public static void init() {}

    private static ResourceLocation tech(String id) {
        var loc = modLoc(id);
        REGISTRATE.trackTranslation(ITechnology.getDescriptionId(loc));
        REGISTRATE.trackTranslation(ITechnology.getDetailsId(loc));
        return loc;
    }
}
