package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Machine;
import org.shsts.tinactory.content.machine.MachineBlock;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.BlockEntitySet;
import org.shsts.tinactory.core.common.SimpleFluid;
import org.shsts.tinactory.core.gui.ContainerMenu;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.Texture;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.core.gui.ContainerMenu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlocks {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> TEST_RECIPE_TYPE;

    public static final Layout TEST_FLUID_LAYOUT;
    public static final RegistryEntry<MachineBlock<Machine>> TEST_MACHINE_BLOCK;
    public static final BlockEntitySet<TestGenerator, MachineBlock<TestGenerator>> TEST_GENERATOR;
    public static final RegistryEntry<SimpleFluid> TEST_STEAM;
    public static final RegistryEntry<FluidCell> TEST_FLUID_CELL;

    static {
        TEST_RECIPE_TYPE = REGISTRATE.simpleProcessingRecipeType("test");

        TEST_FLUID_LAYOUT = Layout.builder()
                .port(Layout.SlotType.FLUID_INPUT)
                .slot(0, 1)
                .port(Layout.SlotType.FLUID_OUTPUT)
                .slot(ContainerMenu.SLOT_SIZE * 3, 1)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build();

        TEST_RECIPE_TYPE.modRecipe(TinactoryTest.modLoc("test"))
                .outputFluid(1, Fluids.WATER, 1000)
                .workTicks(50)
                .build();

        TEST_GENERATOR = BlockEntitySet.builder(REGISTRATE, "machine/test",
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
                .blockEntity()
                .ticking()
                .capability(AllCapabilities.STACK_CONTAINER, $ -> $.layout(AllBlocks.TEST_FLUID_LAYOUT, Voltage.ULV))
                .capability(AllCapabilities.RECIPE_PROCESSOR, $ -> $
                        .voltage(Voltage.ULV)
                        .recipeType(AllBlocks.TEST_RECIPE_TYPE.get()))
                .menu().layout(AllBlocks.TEST_FLUID_LAYOUT, Voltage.ULV).build()
                .build()
                .register();

        TEST_MACHINE_BLOCK = REGISTRATE.entityBlock("machine/test", MachineBlock<Machine>::new)
                .type(() -> AllBlockEntities.TEST_MACHINE)
                .transform(ModelGen.machine(
                        ModelGen.gregtech("blocks/casings/voltage/ulv"),
                        ModelGen.gregtech("blocks/machines/alloy_smelter/overlay_front")))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();

        TEST_STEAM = REGISTRATE.simpleFluid("steam", ModelGen.gregtech("blocks/fluids/fluid.steam"));

        TEST_FLUID_CELL = REGISTRATE.item("fluid_cell", properties -> new FluidCell(properties, 16000))
                .model(ModelGen.basicItem(ModelGen.gregtech("items/metaitems/fluid_cell/base")))
                .register();
    }

    public static void init() {}
}
