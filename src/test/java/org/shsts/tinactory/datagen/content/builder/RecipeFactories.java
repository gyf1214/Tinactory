package org.shsts.tinactory.datagen.content.builder;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.AllRecipes;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class RecipeFactories {
    public static final ProcessingRecipeFactory STONE_GENERATOR;
    public static final ProcessingRecipeFactory VACUUM_FREEZER;

    static {
        VACUUM_FREEZER = new ProcessingRecipeFactory(AllRecipes.VACUUM_FREEZER)
            .defaults(RecipeFactories::fullDefaults)
            .defaults($ -> $.amperage(1.5d));

        STONE_GENERATOR = processing("stone_generator")
            .defaults($ -> $.defaults(-1, -1, 0, 1).amperage(0.125d).workTicks(20));
    }

    private static ProcessingRecipeFactory processing(String id) {
        return new ProcessingRecipeFactory(REGISTRATE.getRecipeType(id));
    }

    private static <S extends ProcessingRecipeBuilder<?, ?, S>> S fullDefaults(S builder) {
        return builder.defaults(0, 1, 2, 3);
    }
}
