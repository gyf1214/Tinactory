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

    private int getHeight(IMachine machine) {
        if (!(machine instanceof MultiBlockInterface multiBlockInterface)) {
            return 0;
        }
        return multiBlockInterface.getMultiBlock()
            .filter($ -> $ instanceof DistillationTower)
            .map($ -> ((DistillationTower) $).getHeight())
            .orElse(0);
    }

    private boolean matchOutputs(IMachine machine, IContainer container, Random random) {
        var height = getHeight(machine);
        var fluids = 0;
        var items = 0;
        for (var output : outputs) {
            if (output.port() == 1) {
                if (fluids < height) {
                    if (!insertOutput(container, output, random, true)) {
                        return false;
                    }
                    fluids++;
                }
            } else {
                if (items < height) {
                    if (!insertOutput(container, output, random, true)) {
                        return false;
                    }
                    items++;
                }
            }
        }
        return true;
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
        var height = getHeight(machine);
        var fluids = 0;
        var items = 0;
        for (var output : outputs) {
            if (output.port() == 1) {
                if (fluids < height) {
                    insertOutput(container, output, random, false);
                    fluids++;
                }
            } else {
                if (items < height) {
                    insertOutput(container, output, random, false);
                    items++;
                }
            }
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
