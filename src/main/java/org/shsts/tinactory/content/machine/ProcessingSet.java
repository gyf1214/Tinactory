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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.shsts.tinactory.Tinactory.REGISTRATE;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ProcessingSet<T extends ProcessingRecipe> {
    public final RecipeTypeEntry<T, ?> recipeType;
    public final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>> machines;
    @Nullable
    protected final BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>> primitive;

    public ProcessingSet(RecipeTypeEntry<T, ?> recipeType, Map<Voltage, Layout> layoutSet,
                         ResourceLocation overlay, Collection<Voltage> voltages) {
        this.recipeType = recipeType;
        this.layoutSet = layoutSet;
        this.machines = voltages.stream()
                .filter(v -> v != Voltage.PRIMITIVE)
                .collect(Collectors.toMap($ -> $, voltage -> createMachine(voltage, overlay)));
        if (voltages.contains(Voltage.PRIMITIVE)) {
            this.primitive = createPrimitive(overlay);
        } else {
            this.primitive = null;
        }
    }

    protected BlockEntitySet<SmartBlockEntity, MachineBlock<SmartBlockEntity>>
    createMachine(Voltage voltage, ResourceLocation overlay) {
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

        if (voltage == Voltage.PRIMITIVE) {
            builder.blockEntity().ticking();
            builder.block().tag(BlockTags.MINEABLE_WITH_AXE);
        }

        return builder.register();
    }

    protected BlockEntitySet<PrimitiveMachine, PrimitiveBlock<PrimitiveMachine>>
    createPrimitive(ResourceLocation overlay) {
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

    public static class Builder<T extends ProcessingRecipe, P> extends
            SimpleBuilder<ProcessingSet<T>, P, Builder<T, P>> {
        private final RecipeTypeEntry<T, ?> recipeType;
        private final Set<Voltage> voltages = new HashSet<>();
        @Nullable
        private ResourceLocation overlay = null;
        @Nullable
        private Map<Voltage, Layout> layoutSet = null;

        public Builder(RecipeTypeEntry<T, ?> value, P parent) {
            super(parent);
            recipeType = value;
        }

        public Builder<T, P> voltage(Voltage from) {
            Arrays.stream(Voltage.values())
                    .filter(v -> v.rank >= from.rank && v.rank <= Voltage.IV.rank)
                    .forEach(voltages::add);
            return this;
        }

        public Builder<T, P> overlay(ResourceLocation loc) {
            overlay = loc;
            return this;
        }

        public LayoutSetBuilder<Builder<T, P>> layoutSet() {
            return Layout.builder(this).onCreateObject(value -> layoutSet = value);
        }

        @Override
        public ProcessingSet<T> createObject() {
            assert overlay != null;
            assert layoutSet != null;
            if (voltages.isEmpty()) {
                voltage(Voltage.LV);
            }
            return new ProcessingSet<>(recipeType, layoutSet, overlay, voltages);
        }
    }

    public static <T extends ProcessingRecipe> Builder<T, Unit>
    builder(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType, Unit.INSTANCE);
    }
}
