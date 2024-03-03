package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.tags.BlockTags;
import org.shsts.tinactory.content.gui.WorkbenchMenu;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.PrimitiveBlock;
import org.shsts.tinactory.content.machine.ProcessingSet;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.network.NetworkController;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.BlockEntitySet;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
    public static final BlockEntitySet<NetworkController, MachineBlock<NetworkController>> NETWORK_CONTROLLER;
    public static final BlockEntitySet<SmartBlockEntity, PrimitiveBlock<SmartBlockEntity>> WORKBENCH;
    public static final ProcessingSet<ProcessingRecipe.Simple> STONE_GENERATOR;
    public static final ProcessingSet<ProcessingRecipe.Simple> ORE_ANALYZER;

    static {
        NETWORK_CONTROLLER = REGISTRATE.blockEntitySet("network/controller",
                        NetworkController::new,
                        MachineBlock.factory(Voltage.PRIMITIVE))
                .entityClass(NetworkController.class)
                .blockEntity().ticking().build()
                .block()
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/mv"),
                        ModelGen.gregtech("blocks/overlay/machine/overlay_screen")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        WORKBENCH = REGISTRATE.blockEntitySet("primitive/workbench",
                        SmartBlockEntity::new,
                        PrimitiveBlock<SmartBlockEntity>::new)
                .entityClass(SmartBlockEntity.class)
                .blockEntity()
                .ticking()
                .capability(AllCapabilities.WORKBENCH_CONTAINER)
                .menu(WorkbenchMenu::new).layout(AllLayouts.WORKBENCH).build()
                .build()
                .block()
                .transform(ModelGen.primitive(ModelGen.gregtech("blocks/casings/crafting_table")))
                .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        STONE_GENERATOR = ProcessingSet.builder(AllRecipes.STONE_GENERATOR)
                .frontOverlay(ModelGen.gregtech("blocks/machines/rock_crusher/overlay_front"))
                .voltage(Voltage.PRIMITIVE, Voltage.LV)
                .layout()
                .port(Layout.SlotType.ITEM_OUTPUT)
                .slot(ContainerMenu.SLOT_SIZE * 2, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8, 0)
                .end()
                .build();

        ORE_ANALYZER = ProcessingSet.builder(AllRecipes.ORE_ANALYZER)
                .frontOverlay(ModelGen.gregtech("blocks/machines/electromagnetic_separator/overlay_front"))
                .voltage(Voltage.PRIMITIVE, Voltage.LV)
                .layout()
                .port(Layout.SlotType.ITEM_INPUT)
                .slot(0, 1)
                .port(Layout.SlotType.ITEM_OUTPUT)
                .slot(ContainerMenu.SLOT_SIZE * 3, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + ContainerMenu.SLOT_SIZE, 0)
                .end()
                .build();
    }

    public static void init() {}
}
