package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllRecipes;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeFactories1 {
    public static final ProcessingRecipeFactory1 VACUUM_FREEZER;

    static {
        VACUUM_FREEZER = new ProcessingRecipeFactory1(AllRecipes.VACUUM_FREEZER)
            .defaults(RecipeFactories1::fullDefaults)
            .defaults($ -> $.amperage(1.5d));
    }

    private static <S extends ProcessingRecipeBuilder1<?, ?, S>> S fullDefaults(S builder) {
        return builder.defaults(0, 1, 2, 3);
    }
}
