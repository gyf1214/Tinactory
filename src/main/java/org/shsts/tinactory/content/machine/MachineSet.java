package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.ProcessingPlugin;
import org.shsts.tinactory.content.gui.RecipeBookPlugin;
import org.shsts.tinactory.content.logistics.StackProcessingContainer;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SimpleBuilder;
import org.shsts.tinactory.core.common.SmartBlockEntity;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
import org.shsts.tinactory.core.gui.ProcessingMenu;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.common.BlockEntitySet;
import org.shsts.tinactory.registrate.common.RecipeTypeEntry;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MachineSet<T extends ProcessingRecipe> {
    public final RecipeTypeEntry<T, ?> recipeType;
    public final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines;
    @Nullable
    protected final BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive;

    private MachineSet(RecipeTypeEntry<T, ?> recipeType, Map<Voltage, Layout> layoutSet,
                       Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines,
                       @Nullable BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive) {
        this.recipeType = recipeType;
        this.layoutSet = layoutSet;
        this.machines = machines;
        this.primitive = primitive;
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

    public static abstract class Builder<T extends ProcessingRecipe, P, S extends Builder<T, P, S>> extends
            SimpleBuilder<MachineSet<T>, P, S> {
        protected final RecipeTypeEntry<T, ?> recipeType;
        protected final Set<Voltage> voltages = new HashSet<>();
        @Nullable
        protected Map<Voltage, Layout> layoutSet = null;

        public Builder(RecipeTypeEntry<T, ?> recipeType, P parent) {
            super(parent);
            this.recipeType = recipeType;
        }

        public S voltage(Voltage from) {
            Arrays.stream(Voltage.values())
                    .filter(v -> v.rank >= from.rank && v.rank <= Voltage.IV.rank)
                    .forEach(voltages::add);
            return self();
        }

        public LayoutSetBuilder<S> layoutSet() {
            return Layout.builder(self()).onCreateObject(value -> layoutSet = value);
        }

        protected abstract BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
        createMachine(Voltage voltage);

        protected abstract BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>>
        createPrimitive();

        @Override
        protected MachineSet<T> createObject() {
            assert layoutSet != null;
            if (voltages.isEmpty()) {
                voltage(Voltage.LV);
            }
            var machines = voltages.stream()
                    .filter(v -> v != Voltage.PRIMITIVE)
                    .collect(Collectors.toMap($ -> $, this::createMachine));
            var primitive = voltages.contains(Voltage.PRIMITIVE) ? createPrimitive() : null;
            return new MachineSet<>(recipeType, layoutSet, machines, primitive);
        }
    }

    public static class ProcessingBuilder<T extends ProcessingRecipe, P> extends
            Builder<T, P, ProcessingBuilder<T, P>> {
        @Nullable
        private ResourceLocation overlay = null;

        public ProcessingBuilder(RecipeTypeEntry<T, ?> recipeType, P parent) {
            super(recipeType, parent);
        }

        public ProcessingBuilder<T, P> overlay(ResourceLocation loc) {
            overlay = loc;
            return this;
        }

        @Override
        protected BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
        createMachine(Voltage voltage) {
            assert layoutSet != null;
            assert overlay != null;
            var id = "machine/" + voltage.id + "/" + recipeType.id;
            var layout = layoutSet.get(voltage);
            var builder = REGISTRATE.blockEntitySet(id, SmartBlockEntity::new, MachineBlock.factory(voltage))
                    .entityClass(SmartBlockEntity.class)
                    .blockEntity()
                    .eventManager()
                    .simpleCapability(Machine::builder)
                    .capability(RecipeProcessor::builder)
                    .recipeType(recipeType).voltage(voltage)
                    .build()
                    .capability(StackProcessingContainer::builder)
                    .layout(layout)
                    .build()
                    .menu(ProcessingMenu.factory(layout))
                    .plugin(ProcessingPlugin::new)
                    .plugin(RecipeBookPlugin.builder(recipeType, layout))
                    .build() // menu
                    .build() // blockEntity
                    .block()
                    .transform(ModelGen.machine(voltage, overlay))
                    .tag(AllTags.MINEABLE_WITH_WRENCH)
                    .dropSelf()
                    .blockItem().tag(AllTags.processingMachine(recipeType)).build()
                    .build();

            return builder.register();
        }

        @Override
        protected BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>>
        createPrimitive() {
            assert layoutSet != null;
            assert overlay != null;
            var id = "primitive/" + recipeType.id;
            var layout = layoutSet.get(Voltage.PRIMITIVE);
            var builder = REGISTRATE.blockEntitySet(id, PrimitiveMachine::new, PrimitiveBlock<PrimitiveMachine>::new)
                    .entityClass(PrimitiveMachine.class)
                    .blockEntity()
                    .eventManager().ticking()
                    .capability(RecipeProcessor::builder)
                    .recipeType(recipeType).voltage(Voltage.PRIMITIVE)
                    .build()
                    .capability(StackProcessingContainer::builder)
                    .layout(layout)
                    .build()
                    .menu(ProcessingMenu.factory(layout))
                    .build() // menu
                    .build() // blockEntity
                    .block()
                    .transform(ModelGen.primitiveMachine(overlay))
                    .tag(AllTags.MINEABLE_WITH_WRENCH)
                    .tag(BlockTags.MINEABLE_WITH_AXE)
                    .dropSelf()
                    .blockItem().tag(AllTags.processingMachine(recipeType)).build()
                    .build();

            return builder.register();
        }
    }

    public static <T extends ProcessingRecipe> ProcessingBuilder<T, Unit>
    processing(RecipeTypeEntry<T, ?> recipeType) {
        return new ProcessingBuilder<>(recipeType, Unit.INSTANCE);
    }
}
