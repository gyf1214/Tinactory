package org.shsts.tinactory.test;

import net.minecraft.world.item.CreativeModeTab;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.registrate.Registrate;

public final class AllBlocks {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final Layout TEST_FLUID_LAYOUT;

    static {
        TEST_FLUID_LAYOUT = Layout.builder()
                .slot(0, 0, 0, 0, Layout.SlotType.FLUID_INPUT)
                .slot(1, ContainerMenu.SLOT_SIZE * 2, 0, 0, Layout.SlotType.FLUID_OUTPUT)
                .build();


        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);
    }

    public static void init() {}
}
