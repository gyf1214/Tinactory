package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.datagen.content.language.LanguageMeta;
import org.shsts.tinactory.datagen.extra.LitematicaMeta;
import org.shsts.tinycorelib.api.meta.IMetaConsumer;

import java.util.function.Supplier;

import static org.shsts.tinactory.datagen.TinactoryDatagen.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllDataMeta {
    static {
        execute("language", LanguageMeta::new);
        execute("multiblock", LitematicaMeta::new);
    }

    private static void execute(String folder, Supplier<? extends IMetaConsumer> supplier) {
        CORE.registerMeta(folder, supplier.get()).execute();
    }

    public static void init() {}
}
