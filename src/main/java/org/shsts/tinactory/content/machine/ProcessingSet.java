package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.electric.Voltage;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.multiblock.MultiblockInterface;
import org.shsts.tinactory.core.multiblock.MultiblockInterfaceBlock;
import org.shsts.tinactory.core.multiblock.client.MultiblockInterfaceRenderer;
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

    public static IEntry<MachineBlock> multiblockInterface(Voltage voltage) {
        var id = "multiblock/" + voltage.id + "/interface";
        return BlockEntityBuilder.builder(id, MachineBlock.multiblockInterface(voltage))
            .menu(AllMenus.MULTIBLOCK)
            .blockEntity()
            .transform(MultiblockInterface::factory)
            .transform(FlexibleStackContainer::factory)
            .renderer(() -> () -> MultiblockInterfaceRenderer::new)
            .end()
            .block()
            .tint(() -> () -> (state, $2, $3, i) -> MultiblockInterfaceBlock
                .tint(voltage, state, i))
            .translucent()
            .end()
            .buildObject();
    }
}
