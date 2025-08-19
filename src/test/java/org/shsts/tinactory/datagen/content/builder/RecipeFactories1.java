package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllRecipes;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeFactories1 {
    public static final ProcessingRecipeFactory1 STONE_GENERATOR;
    public static final ProcessingRecipeFactory1 VACUUM_FREEZER;

    static {
        VACUUM_FREEZER = new ProcessingRecipeFactory1(AllRecipes.VACUUM_FREEZER)
            .defaults(RecipeFactories1::fullDefaults)
            .defaults($ -> $.amperage(1.5d));

        STONE_GENERATOR = processing("stone_generator")
            .defaults($ -> $.defaults(-1, -1, 0, 1).amperage(0.125d).workTicks(20));
    }

    private static ProcessingRecipeFactory1 processing(String id) {
        return new ProcessingRecipeFactory1(REGISTRATE.getRecipeType(id));
    }

    private static <S extends ProcessingRecipeBuilder1<?, ?, S>> S fullDefaults(S builder) {
        return builder.defaults(0, 1, 2, 3);
    }
}
