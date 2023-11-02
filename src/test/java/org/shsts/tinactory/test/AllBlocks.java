package org.shsts.tinactory.test;

import net.minecraft.world.item.CreativeModeTab;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllBlocks {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RegistryEntry<CableBlock> NORMAL_CABLE;

    static {
        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);
        NORMAL_CABLE = REGISTRATE.blockHandler.getEntry("tinactory:network/cable/normal");
    }

    public static void init() {}
}
