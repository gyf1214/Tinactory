package org.shsts.tinactory.content;

import net.minecraft.MethodsReturnNonnullByDefault;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.network.NetworkController;
import org.shsts.tinactory.content.primitive.PrimitiveMachine;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.SmartBlockEntity;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.core.Transformer;
import org.shsts.tinactory.gui.WorkbenchMenu;
import org.shsts.tinactory.gui.layout.AllLayouts;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlockEntities {
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
                .menu(WorkbenchMenu::new).layout(AllLayouts.WORKBENCH).build()
                .register();

        PRIMITIVE_STONE_GENERATOR = REGISTRATE.blockEntity("primitive/stone_generator", PrimitiveMachine::new)
                .entityClass(PrimitiveMachine.class)
                .validBlock(AllBlocks.PRIMITIVE_STONE_GENERATOR)
                .ticking()
                .transform(processingStack(AllRecipes.STONE_GENERATOR, AllLayouts.STONE_GENERATOR))
                .register();
    }

    private static <S extends BlockEntityBuilder<?, ?, S>> Transformer<S>
    processingStack(RecipeTypeEntry<? extends ProcessingRecipe<?>, ?> recipeType, Layout layout) {
        return builder -> builder
                .capability(AllCapabilities.PROCESSING_STACK_CONTAINER, $ -> $
                        .recipeType(recipeType)
                        .layout(layout))
                .menu().layout(layout).build();
    }

    public static void init() {}
}
