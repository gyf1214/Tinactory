package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllMaterials;
import org.shsts.tinactory.content.material.OreVariant;
import org.shsts.tinactory.integration.jei.category.RecipeCategory;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.ArrayList;

import static org.shsts.tinactory.content.AllBlockEntities.PROCESSING_SETS;
import static org.shsts.tinactory.content.AllMaterials.WATER;
import static org.shsts.tinactory.content.AllRecipes.BLAST_FURNACE;
import static org.shsts.tinactory.content.AllRecipes.CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.MACERATOR;
import static org.shsts.tinactory.content.AllRecipes.MARKER;
import static org.shsts.tinactory.content.AllRecipes.ORE_ANALYZER;
import static org.shsts.tinactory.content.AllRecipes.ORE_WASHER;
import static org.shsts.tinactory.content.AllRecipes.SIFTER;
import static org.shsts.tinactory.content.AllRecipes.THERMAL_CENTRIFUGE;
import static org.shsts.tinactory.content.AllRecipes.TOOL_CRAFTING;
import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class Markers {
    public static void init() {
        markerCrush("raw");
        markerCrush("crushed");
        markerCrush("crushed_purified");
        markerCrush("crushed_centrifuged");
        markerWash("crushed");
        markerWash("dust_impure");
        markerWash("dust_pure");

        MARKER.recipe(DATA_GEN, "centrifuge_dust_pure")
            .baseType(CENTRIFUGE)
            .inputItem(0, AllMaterials.tag("dust_pure"))
            .build();

        MARKER.recipe(DATA_GEN, "thermal_centrifuge_crushed_purified")
            .baseType(THERMAL_CENTRIFUGE)
            .inputItem(0, AllMaterials.tag("crushed_purified"))
            .build();

        for (var variant : OreVariant.values()) {
            MARKER.recipe(DATA_GEN, "analyze_" + variant.getName())
                .baseType(ORE_ANALYZER)
                .inputItem(0, variant.baseItem)
                .voltage(variant.voltage)
                .build();
        }

        trackJEICategory();
    }

    private static void markerCrush(String sub) {
        MARKER.recipe(DATA_GEN, "crush_" + sub)
            .baseType(MACERATOR)
            .inputItem(0, AllMaterials.tag(sub))
            .build();
    }

    private static void markerWash(String sub) {
        MARKER.recipe(DATA_GEN, "wash_" + sub)
            .baseType(ORE_WASHER)
            .inputItem(0, AllMaterials.tag(sub))
            .inputFluid(1, WATER.fluid())
            .build();
    }

    private static void trackJEICategory() {
        var allTypes = new ArrayList<IRecipeType<?>>();
        for (var set : PROCESSING_SETS) {
            allTypes.add(set.recipeType);
        }
        // TODO
        allTypes.add(TOOL_CRAFTING);
        allTypes.add(BLAST_FURNACE);
        allTypes.add(SIFTER);

        for (var type : allTypes) {
            DATA_GEN.trackLang(RecipeCategory.categoryTitleId(type.loc()));
        }
    }
}
