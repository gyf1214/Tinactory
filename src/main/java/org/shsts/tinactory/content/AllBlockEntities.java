package org.shsts.tinactory.content;

import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.machine.IProcessingMachine;
import org.shsts.tinactory.content.machine.PrimitiveMachine;
import org.shsts.tinactory.content.network.NetworkController;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.Texture;
import org.shsts.tinactory.gui.WorkbenchMenu;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public class AllBlockEntities {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public static final RegistryEntry<SmartBlockEntityType<NetworkController>> NETWORK_CONTROLLER;

    public static final RegistryEntry<SmartBlockEntityType<SmartBlockEntity>> WORKBENCH;

    public static final RegistryEntry<SmartBlockEntityType<PrimitiveMachine>> PRIMITIVE_STONE_GENERATOR;

    static {
        NETWORK_CONTROLLER = REGISTRATE.blockEntity("network/controller", NetworkController::new)
                .entityClass(NetworkController.class)
                .ticking()
                .validBlock(AllBlocks.NETWORK_CONTROLLER)
                .register();

        WORKBENCH = REGISTRATE.blockEntity("primitive/workbench", SmartBlockEntity::new)
                .entityClass(SmartBlockEntity.class)
                .ticking()
                .validBlock(AllBlocks.WORKBENCH)
                .capability(AllCapabilities.WORKBENCH_CONTAINER)
                .menu(WorkbenchMenu::new).build()
                .register();

        PRIMITIVE_STONE_GENERATOR = REGISTRATE.blockEntity("primitive/stone_generator", PrimitiveMachine::new)
                .entityClass(PrimitiveMachine.class)
                .validBlock(AllBlocks.PRIMITIVE_STONE_GENERATOR)
                .ticking()
                .capability(AllCapabilities.PROCESSING_STACK_CONTAINER, $ -> $
                        .recipeType(AllRecipes.STONE_GENERATOR)
                        .port(1, true))
                .menu()
                .title($ -> new TextComponent("Stone Generator"))
                .slot(0, ContainerMenu.SLOT_SIZE * 5, 1)
                .progressBar(Texture.PROGRESS_ARROW, ContainerMenu.SLOT_SIZE * 3 + 8, 0,
                        IProcessingMachine::getProgress)
                .build()
                .register();
    }

    public static void init() {}
}
