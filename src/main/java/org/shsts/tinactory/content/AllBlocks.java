package org.shsts.tinactory.content;

import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.CableSetting;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllBlocks {
    public static final RegistryEntry<CableBlock> NORMAL_CABLE;
    public static final RegistryEntry<CableBlock> DENSE_CABLE;

    static {
        NORMAL_CABLE = REGISTRATE.block("network/cable/normal", CableBlock.factory(CableSetting.NORMAL))
                .transform(ModelGen.cable())
                .tint(0xFF363636, 0xFFFFFFFF)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem().dropSelf()
                .register();

        DENSE_CABLE = REGISTRATE.block("network/cable/dense", CableBlock.factory(CableSetting.DENSE))
                .transform(ModelGen.cable())
                .tint(0xFF363636, 0xFFFFFFFF)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem().dropSelf()
                .register();
    }

    public static void init() {}
}
