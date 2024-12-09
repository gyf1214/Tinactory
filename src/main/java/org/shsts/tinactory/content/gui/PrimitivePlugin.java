package org.shsts.tinactory.content.gui;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.crafting.RecipeType;
import org.shsts.tinactory.core.gui.ProcessingPlugin;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinycorelib.api.gui.IMenu;

import java.util.Optional;

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
    protected Optional<RecipeType<?>> recipeType() {
        return Optional.of(recipeType);
    }
}
