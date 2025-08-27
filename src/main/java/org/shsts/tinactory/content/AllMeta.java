package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.core.recipe.RecipeTypeMeta;
import org.shsts.tinycorelib.api.meta.IMetaConsumer;

import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AllMeta {
    static {
        registerAndExecute("recipe_type", RecipeTypeMeta::new);
    }

    private static void registerAndExecute(String folder, Supplier<? extends IMetaConsumer> supplier) {
        CORE.registerMeta(folder, supplier.get()).execute();
    }

    public static void init() {}
}
