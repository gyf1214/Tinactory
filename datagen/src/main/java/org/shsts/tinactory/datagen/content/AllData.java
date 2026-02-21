package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.datagen.extra.SourceChecker;

import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllData {
    private static final List<Runnable> DELAYED_CALLBACKS = new ArrayList<>();

    public static void initDelayed(Runnable cb) {
        DELAYED_CALLBACKS.add(cb);
    }

    public static void init() {
        Models.init();
        AllDataKt.INSTANCE.init();

        for (var cb : DELAYED_CALLBACKS) {
            cb.run();
        }

        SourceChecker.check();
    }
}
