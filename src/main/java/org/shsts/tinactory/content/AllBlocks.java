package org.shsts.tinactory.content;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.CreativeModeTab;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.network.CableBlock;
import org.shsts.tinactory.content.network.CableSetting;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public final class AllBlocks {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<CableBlock> NORMAL_CABLE;
    public static final RegistryEntry<CableBlock> DENSE_CABLE;
    public static final RegistryEntry<MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final RegistryEntry<PrimitiveBlock<SmartBlockEntity>> WORKBENCH;

    public static final ProcessingSet<ProcessingRecipe.Simple> PRIMITIVE_STONE_GENERATOR;
    public static final ProcessingSet<ProcessingRecipe.Simple> PRIMITIVE_ORE_ANALYZER;

    static {
        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);
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

        PRIMITIVE_STONE_GENERATOR = ProcessingSet.primitive("primitive/stone_generator",
                AllRecipes.STONE_GENERATOR,
                ModelGen.gregtech("blocks/machines/rock_crusher/overlay_front"),
                AllLayouts.STONE_GENERATOR);
        PRIMITIVE_ORE_ANALYZER = ProcessingSet.primitive("primitive/ore_analyzer",
                AllRecipes.ORE_ANALYZER,
                ModelGen.gregtech("blocks/machines/electromagnetic_separator/overlay_front"),
                AllLayouts.ORE_ANALYZER);
    }

    public static void init() {}
}
