package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllRecipes;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.ProcessingPlugin;
import org.shsts.tinactory.content.gui.RecipeBookPlugin;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.content.recipe.GeneratorRecipe;
import org.shsts.tinactory.content.recipe.OreAnalyzerRecipe;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.ProcessingMenu;
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

    public Block getPrimitive() {
        assert primitive != null;
        return primitive.getBlock();
    }

    public Block getBlock(Voltage voltage) {
        if (voltage == Voltage.PRIMITIVE) {
            return getPrimitive();
        }
        return machines.get(voltage).getBlock();
    }

    public abstract static class Builder<T extends ProcessingRecipe, P> extends
            MachineSet.Builder<ProcessingSet<T>, P, Builder<T, P>> {
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
                    .menu(ProcessingMenu.factory(layout)).build()
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
                    .simpleCapability(RecipeProcessor.basic(recipeType, Voltage.PRIMITIVE))
                    .simpleCapability(StackProcessingContainer.builder(layout))
                    .menu(ProcessingMenu.factory(layout)).build()
                    .build()
                    .block()
                    .transform(ModelGen.primitiveMachine(overlay))
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
    processing(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE) {
            @Override
            protected BlockEntitySetBuilder<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
            getMachineBuilder(Voltage voltage) {
                var layout = getLayout(voltage);
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.basic(recipeType, voltage))
                        .menu()
                        .plugin(ProcessingPlugin.builder(layout))
                        .plugin(RecipeBookPlugin.builder(recipeType, layout))
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
                        .simpleCapability(RecipeProcessor.oreProcessor(voltage))
                        .menu()
                        .plugin(ProcessingPlugin.builder(getLayout(voltage)))
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
                var layout = getLayout(voltage);
                return super.getMachineBuilder(voltage)
                        .blockEntity()
                        .simpleCapability(RecipeProcessor.generator(recipeType, voltage))
                        .menu()
                        .plugin(ProcessingPlugin.builder(layout))
                        .plugin(RecipeBookPlugin.builder(recipeType, layout))
                        .build()
                        .build();
            }
        };
    }
}
