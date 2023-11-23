package org.shsts.tinactory.test;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.material.Fluids;
import org.shsts.tinactory.content.primitive.PrimitiveSet;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.gui.ContainerMenu;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.gui.layout.Texture;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.shsts.tinactory.gui.ContainerMenu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class AllBlocks {
    private static final Registrate REGISTRATE = TinactoryTest.REGISTRATE;

    public static final RecipeTypeEntry<ProcessingRecipe.Simple, ProcessingRecipe.SimpleBuilder> TEST_RECIPE_TYPE;

    public static final Layout TEST_FLUID_LAYOUT;
    public static final PrimitiveSet<ProcessingRecipe.Simple> TEST_MACHINE;

    static {
        REGISTRATE.creativeModeTab(CreativeModeTab.TAB_REDSTONE);

        TEST_RECIPE_TYPE = REGISTRATE.simpleProcessingRecipeType("test");

        TEST_FLUID_LAYOUT = Layout.builder()
                .slot(0, 0, 1, 0, Layout.SlotType.FLUID_INPUT)
                .slot(1, ContainerMenu.SLOT_SIZE * 3, 1, 1, Layout.SlotType.FLUID_OUTPUT)
                .progressBar(Texture.PROGRESS_ARROW, 8 + SLOT_SIZE, 0)
                .build();

        TEST_MACHINE = PrimitiveSet.create(REGISTRATE, "test",
                ModelGen.gregtech("machines/alloy_smelter/overlay"),
                TEST_RECIPE_TYPE, TEST_FLUID_LAYOUT);

        TEST_RECIPE_TYPE.modRecipe(TinactoryTest.modLoc("test"))
                .outputFluid(1, Fluids.WATER, 1000)
                .workTicks(50)
                .build();
    }

    public static void init() {}
}
