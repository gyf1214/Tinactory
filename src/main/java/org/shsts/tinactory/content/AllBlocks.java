package org.shsts.tinactory.content;

import net.minecraft.tags.BlockTags;
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
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem().dropSelf()
                .register();
        DENSE_CABLE = REGISTRATE.block("network/cable/dense", CableBlock.factory(CableSetting.DENSE))
                .transform(ModelGen.cable())
                .tint(0x363636, 0xFFFFFF)
                .tag(AllTags.MINEABLE_WITH_CUTTER)
                .defaultBlockItem().dropSelf()
                .register();

        NETWORK_CONTROLLER = REGISTRATE.entityBlock("network/controller", MachineBlock<NetworkController>::new)
                .type(() -> AllBlockEntities.NETWORK_CONTROLLER)
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/mv"),
                        ModelGen.gregtech("blocks/overlay/machine/overlay_screen")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();

        WORKBENCH = REGISTRATE.entityBlock("primitive/workbench", PrimitiveBlock<SmartBlockEntity>::new)
                .type(() -> AllBlockEntities.WORKBENCH)
                .blockState(ModelGen.primitiveAllFaces(
                        ModelGen.gregtech("blocks/casings/crafting_table")))
                .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();

        PRIMITIVE_STONE_GENERATOR = REGISTRATE.entityBlock(
                        "primitive/stone_generator", PrimitiveBlock<PrimitiveMachine>::new)
                .type(() -> AllBlockEntities.PRIMITIVE_STONE_GENERATOR)
                .transform(ModelGen.primitiveMachine(
                        ModelGen.gregtech("blocks/casings/wood_wall"),
                        ModelGen.gregtech("blocks/machines/rock_crusher/overlay_front")))
                .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();
    }

    public static void init() {}
}
