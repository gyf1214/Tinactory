package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.electric.BatteryBox;
import org.shsts.tinactory.content.electric.Voltage;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.network.MachineBlock;
import org.shsts.tinactory.content.network.PrimitiveBlock;
import org.shsts.tinactory.content.network.SidedMachineBlock;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;
import org.shsts.tinactory.registrate.common.RegistryEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingSet<T extends ProcessingRecipe> extends MachineSet<MachineBlock<SmartBlockEntity>> {
    public final RecipeTypeEntry<T, ?> recipeType;

    private ProcessingSet(RecipeTypeEntry<T, ?> recipeType, Collection<Voltage> voltages,
                          Map<Voltage, Layout> layoutSet,
                          Map<Voltage, RegistryEntry<MachineBlock<SmartBlockEntity>>> machines,
                          @Nullable RegistryEntry<PrimitiveBlock<PrimitiveMachine>> primitive) {
        super(voltages, layoutSet, machines, primitive);
        this.recipeType = recipeType;
    }

    public abstract static class Builder<T extends ProcessingRecipe, P> extends
            BuilderBase<MachineBlock<SmartBlockEntity>, ProcessingSet<T>, P, Builder<T, P>> {
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
                    .menu(ProcessingMenu.machine(layout)).build()
                    .build()
                    .translucent();
        }

        @Override
        protected BlockEntityBuilder<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>, ?>
        getPrimitiveBuilder() {
            var id = "primitive/" + recipeType.id;
            var layout = getLayout(Voltage.PRIMITIVE);
            return REGISTRATE.blockEntity(id, PrimitiveMachine::new, PrimitiveBlock<PrimitiveMachine>::new)
                    .entityClass(PrimitiveMachine.class)
                    .blockEntity()
                    .eventManager().ticking()
                    .simpleCapability(RecipeProcessor.machine(recipeType))
                    .simpleCapability(StackProcessingContainer.builder(layout))
                    .menu(ProcessingMenu.machine(layout)).build()
                    .build()
                    .translucent();
        }

        @Override
        protected ProcessingSet<T>
        createSet(Collection<Voltage> voltages, Map<Voltage, Layout> layoutSet,
                  Map<Voltage, RegistryEntry<MachineBlock<SmartBlockEntity>>> machines,
                  @Nullable RegistryEntry<PrimitiveBlock<PrimitiveMachine>> primitive) {
            return new ProcessingSet<>(recipeType, voltages, layoutSet, machines, primitive);
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
    marker(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.machine(recipeType))
                        .menu()
                        .plugin(MachinePlugin.marker(recipeType))
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
                        .plugin(MachinePlugin.marker(recipeType))
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

    public static MachineSet.Builder<MachineBlock<SmartBlockEntity>, ?> electricFurnace() {
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
                        .plugin(MachinePlugin.electricFurnace(layout))
                        .build()
                        .build()
                        .translucent();
            }
        };
    }

    public static MachineSet.Builder<SidedMachineBlock<SmartBlockEntity>, ?> batteryBox() {
        return new MachineSet.Builder<>(Unit.INSTANCE) {
            @Override
            protected BlockEntityBuilder<SmartBlockEntity, SidedMachineBlock<SmartBlockEntity>, ?>
            getMachineBuilder(Voltage voltage) {
                var id = "machine/" + voltage.id + "/battery_box";
                var layout = getLayout(voltage);
                return REGISTRATE.blockEntity(id, MachineBlock.sided(voltage))
                        .blockEntity()
                        .eventManager()
                        .simpleCapability(Machine::builder)
                        .simpleCapability(BatteryBox::builder)
                        .menu(ProcessingMenu.machine(layout)).build()
                        .build().translucent();
            }
        };
    }
}
