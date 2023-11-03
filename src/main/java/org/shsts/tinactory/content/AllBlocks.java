package org.shsts.tinactory.content;

import net.minecraft.world.item.CreativeModeTab;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.CableSetting;
import org.shsts.tinactory.content.network.NetworkController;
import org.shsts.tinactory.content.primitive.PrimitiveBlock;
import org.shsts.tinactory.content.primitive.PrimitiveMachine;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllBlocks {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<CableBlock> NORMAL_CABLE;
    public static final RegistryEntry<CableBlock> DENSE_CABLE;
    public static final RegistryEntry<MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> WORKBENCH;

    public static final RegistryEntry<PrimitiveBlock<PrimitiveMachine>> PRIMITIVE_STONE_GENERATOR;

    static {
        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);
        NORMAL_CABLE = REGISTRATE.block("network/cable/normal", CableBlock.factory(CableSetting.NORMAL))
                .transform(ModelGen.cable())
                .tint(0x363636, 0xFFFFFF)
                .defaultBlockItem()
                .register();
        DENSE_CABLE = REGISTRATE.block("network/cable/dense", CableBlock.factory(CableSetting.DENSE))
                .transform(ModelGen.cable())
                .tint(0x363636, 0xFFFFFF)
                .defaultBlockItem()
                .register();

        NETWORK_CONTROLLER = REGISTRATE.entityBlock("network/controller", MachineBlock<NetworkController>::new)
                .type(() -> AllBlockEntities.NETWORK_CONTROLLER)
                .transform(ModelGen.machine(
                        ModelGen.vendorLoc("gregtech", "blocks/casings/voltage/mv"),
                        ModelGen.vendorLoc("gregtech", "blocks/overlay/machine/overlay_screen")))
                .defaultBlockItem()
                .register();

        WORKBENCH = REGISTRATE.entityBlock("primitive/workbench", PrimitiveBlock<SmartBlockEntity>::new)
                .type(() -> AllBlockEntities.WORKBENCH)
                .blockState(ModelGen.primitive(ModelGen.vendorLoc("gregtech", "blocks/casings/crafting_table")))
                .defaultBlockItem()
                .register();

        PRIMITIVE_STONE_GENERATOR = REGISTRATE.entityBlock(
                        "primitive/stone_generator", PrimitiveBlock<PrimitiveMachine>::new)
                .type(() -> AllBlockEntities.PRIMITIVE_STONE_GENERATOR)
                .transform(ModelGen.primitiveMachine(
                        ModelGen.vendorLoc("gregtech", "blocks/casings/voltage/mv"),
                        ModelGen.vendorLoc("gregtech", "blocks/machines/rock_crusher/overlay_front")))
                .defaultBlockItem()
                .register();
    }

    public static void init() {}
}
