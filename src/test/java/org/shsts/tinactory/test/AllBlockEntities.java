package org.shsts.tinactory.test;

import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public class AllBlockEntities {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RegistryEntry<SmartBlockEntityType<PrimitiveStoneGenerator>> PRIMITIVE_STONE_GENERATOR;

    static {
        PRIMITIVE_STONE_GENERATOR = REGISTRATE.blockEntity("primitive/stone_generator", PrimitiveStoneGenerator::new)
                .entityClass(PrimitiveStoneGenerator.class)
                .validBlock(AllBlocks.PRIMITIVE_STONE_GENERATOR)
                .menu(ContainerMenu::new)
                .screen(() -> () -> TestMenuScreen::new)
                .build()
                .register();
    }
}
