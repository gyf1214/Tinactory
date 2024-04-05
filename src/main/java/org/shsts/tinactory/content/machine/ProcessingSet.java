package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.MenuGen;
import org.shsts.tinactory.content.logistics.StackContainer;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.gui.LayoutSetBuilder;
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
public class ProcessingSet<T extends ProcessingRecipe<T>> {
    public final RecipeTypeEntry<T, ?> recipeType;
    public final Map<Voltage, Layout> layoutSet;
    protected final Map<Voltage, BlockEntitySet<Machine, MachineBlock<Machine>>> machines;

    public ProcessingSet(RecipeTypeEntry<T, ?> recipeType, Map<Voltage, Layout> layoutSet,
                         ResourceLocation frontOverlay, Collection<Voltage> voltages) {
        this.recipeType = recipeType;
        this.layoutSet = layoutSet;
        this.machines = voltages.stream()
                .collect(Collectors.toMap($ -> $, voltage -> createMachine(voltage, frontOverlay)));
    }

    protected BlockEntitySet<Machine, MachineBlock<Machine>>
    createMachine(Voltage voltage, ResourceLocation frontOverlay) {
        var id = "machine/" + voltage.id + "/" + recipeType.id;
        var layout = layoutSet.get(voltage);
        var builder = REGISTRATE.blockEntitySet(id, Machine.factory(voltage), MachineBlock.factory(voltage))
                .entityClass(Machine.class)
                .blockEntity()
                .hasEvent()
                .capability(RecipeProcessor::builder)
                .recipeType(recipeType).voltage(voltage)
                .build()
                .capability(StackContainer::builder)
                .layout(layout)
                .build()
                .menu()
                .transform(MenuGen.machineMenu(layout))
                .transform(MenuGen.machineRecipeBook(recipeType, layout))
                .build() // menu
                .build() // blockEntity
                .block()
                .transform(ModelGen.machine(voltage, frontOverlay))
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

    public Block getBlock(Voltage voltage) {
        return machines.get(voltage).getBlock();
    }


    public static class Builder<T extends ProcessingRecipe<T>> {
        private final RecipeTypeEntry<T, ?> recipeType;
        private final Set<Voltage> voltages = new HashSet<>();
        @Nullable
        private ResourceLocation frontOverlay = null;
        @Nullable
        private Map<Voltage, Layout> layoutSet = null;

        private Builder(RecipeTypeEntry<T, ?> value) {
            recipeType = value;
        }

        public Builder<T> voltage(Voltage... values) {
            voltages.addAll(Arrays.asList(values));
            return this;
        }

        public Builder<T> frontOverlay(ResourceLocation loc) {
            frontOverlay = loc;
            return this;
        }

        public LayoutSetBuilder<Builder<T>> layoutSet() {
            return Layout.builder(this).onCreateObject(value -> layoutSet = value);
        }

        public ProcessingSet<T> build() {
            assert frontOverlay != null;
            assert layoutSet != null;
            return new ProcessingSet<>(recipeType, layoutSet, frontOverlay, voltages);
        }
    }

    public static <T extends ProcessingRecipe<T>> Builder<T>
    builder(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType);
    }
}
