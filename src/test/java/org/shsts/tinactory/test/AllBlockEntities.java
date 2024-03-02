package org.shsts.tinactory.test;

import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.BlockEntitySet;
import org.shsts.tinactory.registrate.Registrate;

public final class AllBlockEntities {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final BlockEntitySet<Machine, MachineBlock<Machine>> TEST_MACHINE;
    public static final BlockEntitySet<TestGenerator, MachineBlock<TestGenerator>> TEST_GENERATOR;

    static {
        TEST_MACHINE = REGISTRATE.blockEntitySet("machine/test", Machine::new, MachineBlock<Machine>::new)
                .entityClass(Machine.class)
                .blockEntity()
                .ticking()
                .capability(AllCapabilities.STACK_CONTAINER, $ -> $
                        .layout(AllBlocks.TEST_FLUID_LAYOUT, Voltage.ULV))
                .capability(AllCapabilities.RECIPE_PROCESSOR, $ -> $
                        .voltage(Voltage.ULV)
                        .recipeType(AllBlocks.TEST_RECIPE_TYPE.get()))
                .menu().layout(AllBlocks.TEST_FLUID_LAYOUT, Voltage.ULV).build()
                .build()
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
                        MachineBlock<TestGenerator>::new)
                .entityClass(TestGenerator.class)
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
