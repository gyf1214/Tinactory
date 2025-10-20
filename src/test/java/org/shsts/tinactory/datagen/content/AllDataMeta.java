package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;

import static org.shsts.tinactory.test.TinactoryTest.CORE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllDataMeta {
    static {
        CORE.registerMeta("language", new LanguageMeta()).execute();
    }

    public static void init() {}
}
