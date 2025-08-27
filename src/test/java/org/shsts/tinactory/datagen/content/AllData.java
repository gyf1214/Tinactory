package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.datagen.provider.LanguageDataProvider;

import static org.shsts.tinactory.test.TinactoryTest.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllData {
    private AllData() {}

    public static void init() {
        DATA_GEN.addProvider(LanguageDataProvider::new);
        Models.init();
        AllDataKt.INSTANCE.init();

        MaterialExporter.init();
    }
}
