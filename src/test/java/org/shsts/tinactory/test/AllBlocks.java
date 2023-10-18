package org.shsts.tinactory.test;

import net.minecraft.world.item.CreativeModeTab;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllBlocks {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RegistryEntry<MachineBlock<PrimitiveStoneGenerator>> PRIMITIVE_STONE_GENERATOR;

    static {
        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);
        PRIMITIVE_STONE_GENERATOR = REGISTRATE.entityBlock(
                        "primitive/stone_generator", MachineBlock<PrimitiveStoneGenerator>::new)
                .type(() -> AllBlockEntities.PRIMITIVE_STONE_GENERATOR)
                .transform(ModelGen.machine(
                        ModelGen.vendorLoc("gregtech", "blocks/casings/voltage/mv"),
                        ModelGen.vendorLoc("gregtech", "blocks/machines/rock_crusher/overlay_front")))
                .defaultBlockItem()
                .register();
    }

    public static void init() {}
}
