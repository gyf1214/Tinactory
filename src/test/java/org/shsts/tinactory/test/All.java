package org.shsts.tinactory.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.shsts.tinactory.api.logistics.SlotType;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Menu;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import static org.shsts.tinactory.content.AllRecipes.RESEARCH;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;
import static org.shsts.tinactory.test.TinactoryTest.REGISTRATE;

public final class All {
    public static final Layout TEST_FLUID_LAYOUT;
    public static final RegistryEntry<SimpleFluid> TEST_STEAM;
    public static final RegistryEntry<FluidCell> TEST_FLUID_CELL;
    public static final BlockEntitySet<TestGenerator, MachineBlock<TestGenerator>> TEST_GENERATOR;

    public static final ResourceLocation TEST_BASE_TECH;
    public static final ResourceLocation TEST_TECH;

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

        TEST_GENERATOR = REGISTRATE.blockEntitySet("machine/test_generator",
                        TestGenerator.factory(Voltage.ULV, 1),
                        MachineBlock.factory(Voltage.ULV))
                .entityClass(TestGenerator.class)
                .blockEntity()
                .simpleCapability(Machine::builder)
                .build()
                .block()
                .transform(ModelGen.machine(Voltage.ULV, ModelGen.gregtech("blocks/overlay/machine/overlay_screen")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .build()
                .register();

        TEST_BASE_TECH = REGISTRATE.tech("test_base")
                .maxProgress(10)
                .displayItem(Items.WOODEN_SWORD)
                .buildObject();

        TEST_TECH = REGISTRATE.tech("test")
                .maxProgress(20)
                .depends(TEST_BASE_TECH)
                .displayItem(Items.IRON_SWORD)
                .buildObject();
    }

    public static void init() {}

    public static void initRecipes() {
        RESEARCH.recipe(REGISTRATE, "test_research")
                .target(TEST_BASE_TECH)
                .inputItem(() -> Items.COBBLESTONE)
                .workTicks(200)
                .voltage(Voltage.ULV)
                .build();
    }
}
