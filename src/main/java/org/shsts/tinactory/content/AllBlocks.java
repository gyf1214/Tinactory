package org.shsts.tinactory.content;

import net.minecraft.tags.BlockTags;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.CableSetting;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.RegistryEntry;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

public final class AllBlocks {
    public static final RegistryEntry<CableBlock> NORMAL_CABLE;
    public static final RegistryEntry<CableBlock> DENSE_CABLE;
    public static final RegistryEntry<MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> WORKBENCH;

    public static final ProcessingSet<ProcessingRecipe.Simple> STONE_GENERATOR;
    public static final ProcessingSet<ProcessingRecipe.Simple> ORE_ANALYZER;

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
                .transform(ModelGen.primitive(
                        ModelGen.gregtech("blocks/casings/crafting_table")))
                .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();

        STONE_GENERATOR = ProcessingSet.builder(AllRecipes.STONE_GENERATOR)
                .frontOverlay(ModelGen.gregtech("blocks/machines/rock_crusher/overlay_front"))
                .voltage(Voltage.PRIMITIVE, Voltage.LV)
                .layout(AllLayouts.STONE_GENERATOR)
                .build();

        ORE_ANALYZER = ProcessingSet.builder(AllRecipes.ORE_ANALYZER)
                .frontOverlay(ModelGen.gregtech("blocks/machines/electromagnetic_separator/overlay_front"))
                .voltage(Voltage.PRIMITIVE, Voltage.LV)
                .layout(AllLayouts.ORE_ANALYZER)
                .build();
    }

    public static void init() {}
}
