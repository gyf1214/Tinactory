package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.core.gui.ProcessingPlugin;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinycorelib.api.gui.IMenu;

import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitivePlugin extends ProcessingPlugin {
    private final RecipeType<?> recipeType;

    public PrimitivePlugin(IMenu menu) {
        super(menu, SLOT_SIZE / 2);
        this.recipeType = ((RecipeProcessor<?>) PROCESSOR.get(menu.blockEntity())).recipeType;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(ProcessingScreen screen) {
        super.applyMenuScreen(screen);
        screen.setRecipeType(recipeType);
    }
}
