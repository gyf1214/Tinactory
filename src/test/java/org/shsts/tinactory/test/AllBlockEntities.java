package org.shsts.tinactory.test;

import net.minecraft.network.chat.TextComponent;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.Texture;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

public class AllBlockEntities {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RegistryEntry<SmartBlockEntityType<PrimitiveStoneGenerator>> PRIMITIVE_STONE_GENERATOR;

    static {
        PRIMITIVE_STONE_GENERATOR = REGISTRATE.blockEntity("primitive/stone_generator", PrimitiveStoneGenerator::new)
                .entityClass(PrimitiveStoneGenerator.class)
                .validBlock(AllBlocks.PRIMITIVE_STONE_GENERATOR)
                .menu()
                .title($ -> new TextComponent("Stone Generator"))
                .slot(0, ContainerMenu.SLOT_SIZE * 5, 1)
                .progressBar(Texture.PROGRESS_ARROW, ContainerMenu.SLOT_SIZE * 3 + 8, 0,
                        PrimitiveStoneGenerator::getProgress)
                .build()
                .register();
    }
}
