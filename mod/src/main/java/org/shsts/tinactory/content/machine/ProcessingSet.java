package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingSet extends MachineSet {
    public final IRecipeType<?> recipeType;

    public ProcessingSet(IRecipeType<?> type, Map<Voltage, Layout> layoutSet,
        Map<Voltage, IEntry<? extends Block>> machines) {
        super(layoutSet, machines);
        this.recipeType = type;
    }
}
