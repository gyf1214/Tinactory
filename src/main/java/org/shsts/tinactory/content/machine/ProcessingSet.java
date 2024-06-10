package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.MachinePlugin;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.machine.RecipeProcessor;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.builder.BlockEntitySetBuilder;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

import static org.shsts.tinactory.Tinactory.REGISTRATE;
import static org.shsts.tinactory.content.model.ModelGen.gregtech;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingSet<T extends ProcessingRecipe> extends MachineSet {
    public final RecipeTypeEntry<T, ?> recipeType;

    private ProcessingSet(RecipeTypeEntry<T, ?> recipeType, Map<Voltage, Layout> layoutSet,
                          Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines,
                          @Nullable BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive) {
        super(layoutSet, machines, primitive);
        this.recipeType = recipeType;
    }

    public abstract static class Builder<T extends ProcessingRecipe, P> extends
            BuilderBase<ProcessingSet<T>, P, Builder<T, P>> {
        protected final RecipeTypeEntry<T, ?> recipeType;
        @Nullable
        protected ResourceLocation overlay = null;

        public Builder(RecipeTypeEntry<T, ?> recipeType, P parent) {
            super(parent);
            this.recipeType = recipeType;
        }

        public Builder<T, P> overlay(ResourceLocation loc) {
            overlay = loc;
            return this;
        }

        @Override
        protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
        getMachineBuilder(Voltage voltage) {
            assert overlay != null;
            var id = "machine/" + voltage.id + "/" + recipeType.id;
            var layout = getLayout(voltage);
            return REGISTRATE.blockEntitySet(id, SmartBlockEntity::new, MachineBlock.factory(voltage))
                    .entityClass(SmartBlockEntity.class)
                    .blockEntity()
                    .eventManager()
                    .simpleCapability(Machine::builder)
                    .simpleCapability(StackProcessingContainer.builder(layout))
                    .menu(ProcessingMenu.machine(layout)).build()
                    .build()
                    .block()
                    .transform(ModelGen.machine(voltage, overlay))
                    .tag(AllTags.MINEABLE_WITH_WRENCH)
                    .dropSelf()
                    .blockItem().tag(AllTags.processingMachine(recipeType)).build()
                    .build();
        }

        @Override
        protected BlockEntitySetBuilder<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>>
        getPrimitiveBuilder() {
            assert overlay != null;
            var id = "primitive/" + recipeType.id;
            var layout = getLayout(Voltage.PRIMITIVE);
            return REGISTRATE.blockEntitySet(id, PrimitiveMachine::new, PrimitiveBlock<PrimitiveMachine>::new)
                    .entityClass(PrimitiveMachine.class)
                    .blockEntity()
                    .eventManager().ticking()
                    .simpleCapability(RecipeProcessor.machine(recipeType))
                    .simpleCapability(StackProcessingContainer.builder(layout))
                    .menu(ProcessingMenu.machine(layout)).build()
                    .build()
                    .block()
                    .transform(ModelGen.primitiveMachine(gregtech("blocks/casings/wood_wall"),
                            overlay))
                    .tag(AllTags.MINEABLE_WITH_WRENCH)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .dropSelf()
                    .blockItem().tag(AllTags.processingMachine(recipeType)).build()
                    .build();
        }

        @Override
        protected ProcessingSet<T>
        createSet(Map<Voltage, Layout> layoutSet,
                  Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines,
                  @Nullable BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive) {
            return new ProcessingSet<>(recipeType, layoutSet, machines, primitive);
        }
    }

    public static <T extends ProcessingRecipe> Builder<T, ?>
    machine(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
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
            protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
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
        var builder = new Builder<>(AllRecipes.ORE_ANALYZER, Unit.INSTANCE) {
            @Override
            protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
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
        return builder.overlay(gregtech("blocks/machines/electromagnetic_separator"));
    }

    public static Builder<GeneratorRecipe, ?>
    generator(RecipeTypeEntry<GeneratorRecipe, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
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
        return new MachineSet.Builder<Object>(Unit.INSTANCE) {
            private final ResourceLocation overlay = gregtech("blocks/machines/electric_furnace");

            @Override
            protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
            getMachineBuilder(Voltage voltage) {
                var id = "machine/" + voltage.id + "/electric_furnace";
                var layout = getLayout(voltage);
                return REGISTRATE.blockEntitySet(id, SmartBlockEntity::new, MachineBlock.factory(voltage))
                        .entityClass(SmartBlockEntity.class)
                        .blockEntity()
                        .eventManager()
                        .simpleCapability(Machine::builder)
                        .simpleCapability(StackProcessingContainer.builder(layout))
                        .simpleCapability(RecipeProcessor::electricFurnace)
                        .menu(ProcessingMenu.machine(layout))
                        .plugin(MachinePlugin.electricFurnace(layout))
                        .build()
                        .build()
                        .block()
                        .transform(ModelGen.machine(voltage, overlay))
                        .tag(AllTags.MINEABLE_WITH_WRENCH)
                        .blockItem().tag(AllTags.ELECTRIC_FURNACE).build()
                        .dropSelf()
                        .build();
            }
        };
    }
}
