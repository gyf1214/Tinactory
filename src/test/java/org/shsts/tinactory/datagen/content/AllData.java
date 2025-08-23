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
        Technologies.init();
        AllDataKt.INSTANCE.init();
        Components.init();
        Machines.init();
        Veins.init();
        Markers.init();

        MaterialExporter.init();
    }
}
