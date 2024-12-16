package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllMenus;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.core.builder.BlockEntityBuilder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.multiblock.MultiBlockInterfaceBlock;
import org.shsts.tinactory.core.multiblock.client.MultiBlockInterfaceRenderer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinycorelib.api.core.Transformer;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilder;
import org.shsts.tinycorelib.api.recipe.IRecipeBuilderBase;
import org.shsts.tinycorelib.api.registrate.IRegistrate;
import org.shsts.tinycorelib.api.registrate.builder.IBlockEntityTypeBuilder;
import org.shsts.tinycorelib.api.registrate.entry.IEntry;
import org.shsts.tinycorelib.api.registrate.entry.IMenuType;
import org.shsts.tinycorelib.api.registrate.entry.IRecipeType;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingSet extends MachineSet {
    public final IRecipeType<? extends IRecipeBuilderBase<? extends ProcessingRecipe>> recipeType;

    private ProcessingSet(IRecipeType<? extends IRecipeBuilderBase<? extends ProcessingRecipe>> recipeType,
        Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
        Map<Voltage, IEntry<? extends Block>> machines) {
        super(voltages, layoutSet, machines);
        this.recipeType = recipeType;
    }

    public static class Builder<R extends ProcessingRecipe, B extends IRecipeBuilder<R, B>, P> extends
        BuilderBase<ProcessingSet, P, Builder<R, B, P>> {
        public final IRecipeType<B> recipeType;
        private boolean hasProcessor = false;
        private boolean hasMenu = false;

        public Builder(IRegistrate registrate, P parent, IRecipeType<B> recipeType) {
            super(registrate, parent);
            this.recipeType = recipeType;

            machine(v -> "machine/" + v.id + "/" + recipeType.id(), MachineBlock::factory);
            layoutMachine(StackProcessingContainer::factory);
        }

        @Override
        public Builder<R, B, P> menu(IMenuType menu) {
            hasMenu = true;
            return super.menu(menu);
        }

        public <V> Builder<R, B, P> processor(
            Function<IRecipeType<B>, Transformer<IBlockEntityTypeBuilder<V>>> factory) {
            hasProcessor = true;
            return machine(factory.apply(recipeType));
        }

        @Override
        protected IEntry<? extends Block> createMachine(Voltage voltage) {
            if (!hasProcessor) {
                processor(RecipeProcessor::machine);
            }
            if (!hasMenu) {
                menu(AllMenus.PROCESSING_MACHINE);
            }
            return super.createMachine(voltage);
        }

        @Override
        protected ProcessingSet createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
            Map<Voltage, IEntry<? extends Block>> machines) {
            return new ProcessingSet(recipeType, voltages, layoutSet, machines);
        }
    }

    public static <P, R extends ProcessingRecipe,
        B extends IRecipeBuilder<R, B>> Builder<R, B, P> builder(
        P parent, IRecipeType<B> recipeType) {
        return new Builder<>(REGISTRATE, parent, recipeType);
    }

    public static IEntry<MachineBlock> multiblockInterface(Voltage voltage) {
        var id = "multi_block/" + voltage.id + "/interface";
        return BlockEntityBuilder.builder(id, MachineBlock.multiBlockInterface(voltage))
            .menu(AllMenus.MULTIBLOCK)
            .blockEntity()
            .transform(MultiBlockInterface::factory)
            .transform(FlexibleStackContainer::factory)
            .renderer(() -> () -> MultiBlockInterfaceRenderer::new)
            .end()
            .block()
            .tint(() -> () -> (state, $2, $3, i) -> MultiBlockInterfaceBlock
                .tint(voltage, state, i))
            .translucent()
            .end()
            .buildObject();
    }
}
