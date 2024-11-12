package org.shsts.tinactory.content.machine;

import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.gui.IMenuPlugin;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.multiblock.MultiBlockInterfaceBlock;
import org.shsts.tinactory.core.multiblock.client.MultiBlockInterfaceRenderer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.builder.CapabilityProviderBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingSet extends MachineSet {
    public final RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType;

    private ProcessingSet(RecipeTypeEntry<? extends ProcessingRecipe, ?> recipeType,
        Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
        Map<Voltage, RegistryEntry<? extends Block>> machines) {
        super(voltages, layoutSet, machines);
        this.recipeType = recipeType;
    }

    public static class Builder<T extends ProcessingRecipe, P> extends
        BuilderBase<ProcessingSet, P, Builder<T, P>> {
        public final RecipeTypeEntry<T, ?> recipeType;
        private boolean hasProcessor = false;
        private boolean hasPlugin = false;

        public Builder(Registrate registrate, RecipeTypeEntry<T, ?> recipeType, P parent) {
            super(registrate, parent);
            this.recipeType = recipeType;

            machine(v -> "machine/" + v.id + "/" + recipeType.id, MachineBlock::factory);
            machine(v -> $ -> $.blockEntity()
                .simpleCapability(StackProcessingContainer.builder(getLayout(v)))
                .menu(ProcessingMenu.machine(getLayout(v), recipeType))
                .title(ProcessingMenu::getTitle)
                .build()
                .build());
        }

        public <B> Builder<T, P> processor(Function<RecipeTypeEntry<? extends ProcessingRecipe, ?>,
            Function<B, ? extends CapabilityProviderBuilder<? super SmartBlockEntity, B>>> factory) {
            hasProcessor = true;
            return capability(factory.apply(recipeType));
        }

        public Builder<T, P> processingPlugin(Function<RecipeTypeEntry<? extends ProcessingRecipe, ?>,
            IMenuPlugin.Factory<?>> factory) {
            hasPlugin = true;
            return plugin(factory.apply(recipeType));
        }

        @Override
        protected RegistryEntry<? extends Block> createMachine(Voltage voltage) {
            if (!hasProcessor) {
                processor(RecipeProcessor::machine);
            }
            if (!hasPlugin) {
                processingPlugin(MachinePlugin::processing);
            }
            return super.createMachine(voltage);
        }

        @Override
        protected ProcessingSet createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
            Map<Voltage, RegistryEntry<? extends Block>> machines) {
            return new ProcessingSet(recipeType, voltages, layoutSet, machines);
        }
    }

    public static <T extends ProcessingRecipe, P> Transformer<Builder<T, P>> marker(boolean includeNormal) {
        return $ -> $.processingPlugin(r -> MachinePlugin.marker(r, includeNormal));
    }

    public static RegistryEntry<MachineBlock<SmartBlockEntity>> multiblockInterface(Voltage voltage) {
        var id = "multi_block/" + voltage.id + "/interface";
        return REGISTRATE.blockEntity(id, MachineBlock.multiBlockInterface(voltage))
            .blockEntity()
            .eventManager()
            .simpleCapability(MultiBlockInterface::basic)
            .simpleCapability(FlexibleStackContainer::builder)
            .menu(ProcessingMenu.multiBlock())
            .title(ProcessingMenu::getTitle)
            .plugin(MachinePlugin::multiBlock)
            .build()
            .renderer(() -> () -> MultiBlockInterfaceRenderer::new)
            .build()
            .block()
            .tint(() -> () -> (state, $2, $3, i) -> MultiBlockInterfaceBlock.tint(voltage, state, i))
            .build()
            .translucent()
            .buildObject();
    }
}
