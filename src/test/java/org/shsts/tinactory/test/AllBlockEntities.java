package org.shsts.tinactory.test;

import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.registrate.RegistryEntry;

import static org.shsts.tinactory.test.TinactoryTest.REGISTRATE;

public final class AllBlockEntities {

    public static final RegistryEntry<SmartBlockEntityType<Machine>> TEST_MACHINE;

    public static final RegistryEntry<SmartBlockEntityType<TestGenerator>> TEST_GENERATOR;

    static {
        TEST_MACHINE = REGISTRATE.blockEntity("machine/test", Machine::new)
                .entityClass(Machine.class)
                .validBlock(AllBlocks.TEST_MACHINE_BLOCK)
                .ticking()
                .capability(AllCapabilities.STACK_CONTAINER, $ -> $.layout(AllBlocks.TEST_FLUID_LAYOUT, Voltage.ULV))
                .capability(AllCapabilities.RECIPE_PROCESSOR, $ -> $
                        .voltage(Voltage.ULV)
                        .recipeType(AllBlocks.TEST_RECIPE_TYPE.get()))
                .menu().layout(AllBlocks.TEST_FLUID_LAYOUT, Voltage.ULV).build()
                .register();

        TEST_GENERATOR = REGISTRATE.blockEntity("generator/test", TestGenerator.factory(Voltage.ULV, 1))
                .entityClass(TestGenerator.class)
                .validBlock(AllBlocks.TEST_GENERATOR_BLOCK)
                .ticking()
                .register();
    }


    public static void init() {}
}
