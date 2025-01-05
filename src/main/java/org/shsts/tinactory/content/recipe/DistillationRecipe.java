package org.shsts.tinactory.content.recipe;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.shsts.tinactory.api.logistics.IContainer;
import org.shsts.tinactory.api.machine.IMachine;
import org.shsts.tinactory.content.multiblock.DistillationTower;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Random;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DistillationRecipe extends ProcessingRecipe {
    private DistillationRecipe(BuilderBase<?, ?> builder) {
        super(builder);
    }

    private int getSlots(IMachine machine) {
        if (!(machine instanceof MultiBlockInterface multiBlockInterface)) {
            return 0;
        }
        return multiBlockInterface.getMultiBlock()
            .filter($ -> $ instanceof DistillationTower)
            .map($ -> ((DistillationTower) $).getSlots())
            .orElse(0);
    }

    private boolean matchOutputs(IMachine machine, IContainer container, Random random) {
        var slots = getSlots(machine);
        return outputs.stream().limit(slots)
            .allMatch(output -> insertOutput(container, output, random, true));
    }

    @Override
    public boolean matches(IMachine machine, Level world) {
        var container = machine.container();
        return canCraft(machine) && container
            .filter($ -> matchInputs($) && matchOutputs(machine, $, world.random))
            .isPresent();
    }

    @Override
    public void insertOutputs(IMachine machine, Random random) {
        var container = machine.container().orElseThrow();
        var slots = Math.min(outputs.size(), getSlots(machine));
        for (var i = 0; i < slots; i++) {
            insertOutput(container, outputs.get(i), random, false);
        }
    }

    public static Builder builder(IRecipeType<Builder> parent, ResourceLocation loc) {
        return new Builder(parent, loc) {
            @Override
            protected ProcessingRecipe createObject() {
                return new DistillationRecipe(this);
            }
        };
    }
}
