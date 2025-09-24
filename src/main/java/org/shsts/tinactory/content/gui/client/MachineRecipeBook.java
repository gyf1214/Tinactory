package org.shsts.tinactory.content.gui.client;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.api.tech.ITeamProfile;
import org.shsts.tinactory.core.machine.MachineProcessor;
import org.shsts.tinactory.core.tech.TechManager;

import java.util.function.Consumer;

import static org.shsts.tinactory.content.AllCapabilities.MACHINE;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineRecipeBook extends AbstractRecipeBook {
    private final Consumer<ITeamProfile> onTechChange = $ -> onTechChange();

    public MachineRecipeBook(ProcessingScreen screen) {
        super(screen);
    }

    public void remove() {
        TechManager.client().removeProgressChangeListener(onTechChange);
    }

    @Override
    protected void doRefreshRecipes() {
        var processor = MACHINE.tryGet(blockEntity).flatMap(IMachine::processor);
        if (processor.isEmpty() || !(processor.get() instanceof MachineProcessor machineProcessor)) {
            return;
        }
        var items = machineProcessor.targetRecipes().getValue();
        recipes.addAll(items);
    }

    private void onTechChange() {
        refreshRecipes();
        buttonPanel.refresh();
    }
}
