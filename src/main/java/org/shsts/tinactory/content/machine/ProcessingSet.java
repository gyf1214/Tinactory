package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.ElectricChestMenu;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.gui.ResearchBenchPlugin;
import org.shsts.tinactory.content.logistics.FlexibleStackContainer;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.multiblock.MultiBlockInterface;
import org.shsts.tinactory.core.multiblock.client.MultiBlockInterfaceRenderer;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.recipe.ResearchRecipe;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;

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

    public abstract static class Builder<T extends ProcessingRecipe, P> extends
            BuilderBase<ProcessingSet, P, Builder<T, P>> {
        protected final RecipeTypeEntry<T, ?> recipeType;

        public Builder(RecipeTypeEntry<T, ?> recipeType, P parent) {
            super(parent);
            this.recipeType = recipeType;
        }

        @Override
        protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
        getMachineBuilder(Voltage voltage) {
            var id = "machine/" + voltage.id + "/" + recipeType.id;
            var layout = getLayout(voltage);
            return REGISTRATE.blockEntity(id, MachineBlock.factory(voltage))
                    .blockEntity()
                    .eventManager()
                    .simpleCapability(Machine::builder)
                    .simpleCapability(StackProcessingContainer.builder(layout))
                    .menu(ProcessingMenu.machine(layout, recipeType))
                    .title(ProcessingMenu::getTitle)
                    .build()
                    .build()
                    .translucent();
        }

        @Override
        protected ProcessingSet createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                                          Map<Voltage, RegistryEntry<? extends Block>> machines) {
            return new ProcessingSet(recipeType, voltages, layoutSet, machines);
        }
    }

    public static <T extends ProcessingRecipe> Builder<T, ?>
    machine(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.machine(recipeType))
                        .menu()
                        .plugin(MachinePlugin.processing(recipeType))
                        .build()
                        .build();
            }
        };
    }

    public static <T extends ProcessingRecipe> Builder<T, ?>
    marker(RecipeTypeEntry<T, ?> recipeType, boolean includeNormal) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.machine(recipeType))
                        .menu()
                        .plugin(MachinePlugin.marker(recipeType, includeNormal))
                        .build()
                        .build();
            }
        };
    }

    public static Builder<ResearchRecipe, ?> research() {
        return new Builder<>(AllRecipes.RESEARCH_BENCH, Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.machine(recipeType))
                        .menu()
                        .plugin(MachinePlugin.processing(recipeType))
                        .plugin(ResearchBenchPlugin.builder())
                        .build()
                        .build();
            }
        };
    }

    public static Builder<OreAnalyzerRecipe, ?> oreAnalyzer() {
        return new Builder<>(AllRecipes.ORE_ANALYZER, Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor::oreProcessor)
                        .menu()
                        .plugin(MachinePlugin.marker(recipeType, false))
                        .build()
                        .build();
            }
        };
    }

    public static Builder<GeneratorRecipe, ?>
    generator(RecipeTypeEntry<GeneratorRecipe, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.generator(recipeType))
                        .menu()
                        .plugin(MachinePlugin.processing(recipeType))
                        .build()
                        .build();
            }
        };
    }

    public static MachineSet.Builder<?> electricFurnace() {
        return new MachineSet.Builder<>(Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                var id = "machine/" + voltage.id + "/electric_furnace";
                var layout = getLayout(voltage);
                return REGISTRATE.blockEntity(id, MachineBlock.factory(voltage))
                        .blockEntity()
                        .eventManager()
                        .simpleCapability(Machine::builder)
                        .simpleCapability(StackProcessingContainer.builder(layout))
                        .simpleCapability(RecipeProcessor::electricFurnace)
                        .menu(ProcessingMenu.machine(layout))
                        .title(ProcessingMenu::getTitle)
                        .plugin(MachinePlugin.electricFurnace(layout))
                        .build()
                        .build()
                        .translucent();
            }
        };
    }

    public static MachineSet.Builder<?> batteryBox() {
        return new MachineSet.Builder<>(Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, ?, ?>
            getMachineBuilder(Voltage voltage) {
                var id = "machine/" + voltage.id + "/battery_box";
                var layout = getLayout(voltage);
                return REGISTRATE.blockEntity(id, MachineBlock.sided(voltage))
                        .blockEntity()
                        .eventManager()
                        .simpleCapability(Machine::builder)
                        .simpleCapability(BatteryBox::builder)
                        .menu(ProcessingMenu.machine(layout))
                        .title(ProcessingMenu::getTitle)
                        .build()
                        .build().translucent();
            }
        };
    }

    public static MachineSet.Builder<?> electricChest() {
        return new MachineSet.Builder<>(Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                var id = "machine/" + voltage.id + "/electric_chest";
                var layout = getLayout(voltage);
                return REGISTRATE.blockEntity(id, MachineBlock.factory(voltage))
                        .blockEntity()
                        .eventManager()
                        .simpleCapability(Machine::builder)
                        .simpleCapability(ElectricChest.builder(layout))
                        .menu(ElectricChestMenu.factory(layout))
                        .title(ProcessingMenu::getTitle)
                        .build()
                        .build().translucent();
            }
        };
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
                .translucent()
                .buildObject();
    }
}
