package org.shsts.tinactory.test;

import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.MenuGen;
import org.shsts.tinactory.content.logistics.StackContainer;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.RecipeProcessor;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.registrate.common.BlockEntitySet;

import static org.shsts.tinactory.test.TinactoryTest.REGISTRATE;

public final class AllBlockEntities {
    public static final BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>> TEST_MACHINE;
    public static final BlockEntitySet<TestGenerator, MachineBlock<TestGenerator>> TEST_GENERATOR;

    static {
        TEST_MACHINE = REGISTRATE.blockEntitySet("machine/test", SmartBlockEntity::new,
                        MachineBlock.factory(Voltage.ULV))
                .entityClass(SmartBlockEntity.class)
                .blockEntity()
                .ticking().eventManager()
                .capability(StackContainer::builder)
                .layout(AllBlocks.TEST_FLUID_LAYOUT)
                .build()
                .simpleCapability(Machine.builder(Voltage.ULV))
                .capability(RecipeProcessor::builder)
                .recipeType(AllRecipes.TEST_RECIPE_TYPE)
                .voltage(Voltage.ULV)
                .build()
                .menu(ProcessingMenu::new)
                .transform(MenuGen.machineMenu(AllBlocks.TEST_FLUID_LAYOUT))
                .transform(MenuGen.machineRecipeBook(AllRecipes.TEST_RECIPE_TYPE, AllBlocks.TEST_FLUID_LAYOUT))
                .build() // menu
                .build() // blockEntity
                .block()
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/ulv"),
                        ModelGen.gregtech("blocks/machines/alloy_smelter/overlay_front")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        TEST_GENERATOR = REGISTRATE.blockEntitySet("machine/test_generator",
                        TestGenerator.factory(Voltage.ULV, 1),
                        MachineBlock.factory(Voltage.ULV))
                .entityClass(TestGenerator.class)
                .blockEntity()
                .simpleCapability(Machine.builder(Voltage.ULV))
                .build()
                .block()
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/ulv"),
                        ModelGen.gregtech("blocks/overlay/machine/overlay_screen")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();
    }

    public static void init() {}
}
