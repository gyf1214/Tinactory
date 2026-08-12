package org.shsts.tinactory.datagen.content;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.data.DataProvider;
import net.minecraft.data.structures.SnbtToNbt;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.shsts.tinycorelib.datagen.api.IDataGen;

import java.util.ArrayList;
import java.util.List;

import static org.shsts.tinactory.datagen.TinactoryDatagen.DATA_GEN;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllData {
    private static final List<Runnable> DELAYED_CALLBACKS = new ArrayList<>();

    private static DataProvider snbtProvider(IDataGen dataGen, GatherDataEvent event) {
        var modFile = event.getModContainer().getModInfo().getOwningFile().getFile();
        var folder = modFile.findResource("snbt");
        return new SnbtToNbt(event.getGenerator().getPackOutput(), List.of(folder));
    }

    public static void initDelayed(Runnable cb) {
        DELAYED_CALLBACKS.add(cb);
    }

    public static void init() {
        Models.init();
        AllDataKt.INSTANCE.init();

        DATA_GEN.addProvider(AllData::snbtProvider);

        for (var cb : DELAYED_CALLBACKS) {
            cb.run();
        }
    }
}
