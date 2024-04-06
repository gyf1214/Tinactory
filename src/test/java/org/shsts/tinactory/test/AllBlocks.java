package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.Item;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.logistics.SlotType;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.test.TinactoryTest.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlocks {
    public static final Layout TEST_FLUID_LAYOUT;
    public static final RegistryEntry<SimpleFluid> TEST_STEAM;
    public static final RegistryEntry<FluidCell> TEST_FLUID_CELL;
    public static final RegistryEntry<Item> TEST_ORE;

    static {
        TEST_FLUID_LAYOUT = Layout.builder()
                .port(SlotType.FLUID_INPUT)
                .slot(0, 1)
                .port(SlotType.FLUID_OUTPUT)
                .slot(Menu.SLOT_SIZE * 3, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .buildLayout();

        TEST_STEAM = REGISTRATE.simpleFluid("steam", ModelGen.gregtech("blocks/fluids/fluid.steam"));

        TEST_FLUID_CELL = REGISTRATE.item("fluid_cell", properties -> new FluidCell(properties, 16000))
                .model(ModelGen.basicItem(ModelGen.gregtech("items/metaitems/fluid_cell/base")))
                .register();

        TEST_ORE = REGISTRATE.item("test_ore", Item::new)
                .model(ModelGen.basicItem(ModelGen.modLoc("items/material/raw")))
                .tint(0xFFFF6400)
                .register();
    }

    public static void init() {}
}
