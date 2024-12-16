package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.content.gui.client.ProcessingScreen;
import org.shsts.tinactory.core.gui.ProcessingPlugin;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinycorelib.api.gui.IMenu;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import static org.shsts.tinactory.content.AllCapabilities.PROCESSOR;
import static org.shsts.tinactory.core.gui.Menu.SLOT_SIZE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class PrimitivePlugin extends ProcessingPlugin {
    private final IRecipeType<?> recipeType;

    public PrimitivePlugin(IMenu menu) {
        super(menu, SLOT_SIZE / 2);
        this.recipeType = ((MachineProcessor<?>) PROCESSOR.get(menu.blockEntity())).recipeType;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void applyMenuScreen(ProcessingScreen screen) {
        super.applyMenuScreen(screen);
        screen.setRecipeType(recipeType);
    }
}
