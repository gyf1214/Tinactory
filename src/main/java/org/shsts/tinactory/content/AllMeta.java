package org.shsts.tinactory.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.content.electric.CircuitMeta;
import org.shsts.tinactory.content.material.ComponentMeta;
import org.shsts.tinactory.content.material.MaterialMeta;
import org.shsts.tinactory.core.recipe.RecipeTypeMeta;
import org.shsts.tinycorelib.api.meta.IMetaConsumer;

import java.util.function.Supplier;

import static org.shsts.tinactory.Tinactory.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AllMeta {
    static {
        execute("recipe_type", RecipeTypeMeta::new);
        execute("material", MaterialMeta::new);
        execute("circuit", CircuitMeta::new);
        execute("component", ComponentMeta::new);
    }

    private static void execute(String folder, Supplier<? extends IMetaConsumer> supplier) {
        CORE.registerMeta(folder, supplier.get()).execute();
    }

    public static void init() {}
}
