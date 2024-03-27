package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.content.AllCapabilityProviders;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.gui.MenuGen;
import org.shsts.tinactory.content.gui.client.MachineRecipeBook;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.gui.Layout;
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
    public final Layout layout;
    protected final Map<Voltage, BlockEntitySet<Machine, MachineBlock<Machine>>> machines;

    public ProcessingSet(RecipeTypeEntry<T, ?> recipeType, Layout layout,
                         ResourceLocation frontOverlay, Collection<Voltage> voltages) {
        this.recipeType = recipeType;
        this.layout = layout;
        this.machines = voltages.stream()
                .collect(Collectors.toMap($ -> $, voltage -> this.createMachine(voltage, frontOverlay)));
    }

    protected BlockEntitySet<Machine, MachineBlock<Machine>>
    createMachine(Voltage voltage, ResourceLocation frontOverlay) {
        var id = "machine/" + voltage.id + "/" + this.recipeType.id;
        var builder = REGISTRATE.blockEntitySet(id, Machine.factory(voltage), MachineBlock.factory(voltage))
                .entityClass(Machine.class)
                .blockEntity()
                .hasEvent()
                .capability(AllCapabilityProviders.RECIPE_PROCESSOR, $ -> $
                        .recipeType(this.recipeType.get()).voltage(voltage))
                .capability(AllCapabilityProviders.STACK_CONTAINER, $ -> $
                        .layout(this.layout, voltage))
                .menu()
                .transform(MenuGen.machineMenu(this.layout, voltage))
                .widget(() -> menu -> new MachineRecipeBook(menu, this.recipeType.get(), 0, 0))
                .build() // menu
                .build() // blockEntity
                .block()
                .transform(ModelGen.machine(voltage, frontOverlay))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .dropSelf()
                .blockItem().tag(AllTags.processingMachine(this.recipeType)).build()
                .build();

        if (voltage == Voltage.PRIMITIVE) {
            builder.blockEntity().ticking();
            builder.block().tag(BlockTags.MINEABLE_WITH_AXE);
        }

        return builder.register();
    }

    public Block getBlock(Voltage voltage) {
        return this.machines.get(voltage).getBlock();
    }

    public static class Builder<T extends ProcessingRecipe<T>> {
        private final RecipeTypeEntry<T, ?> recipeType;
        private final Set<Voltage> voltages = new HashSet<>();
        @Nullable
        private ResourceLocation frontOverlay = null;
        @Nullable
        private Layout layout = null;

        private Builder(RecipeTypeEntry<T, ?> recipeType) {
            this.recipeType = recipeType;
        }

        public Builder<T> voltage(Voltage... voltages) {
            this.voltages.addAll(Arrays.asList(voltages));
            return this;
        }

        public Builder<T> frontOverlay(ResourceLocation loc) {
            this.frontOverlay = loc;
            return this;
        }

        public Builder<T> layout(Layout layout) {
            this.layout = layout;
            return this;
        }

        public Layout.Builder<Builder<T>> layout() {
            return Layout.builder(this)
                    .onCreate(layout -> this.layout = layout);
        }

        public ProcessingSet<T> build() {
            assert this.frontOverlay != null;
            assert this.layout != null;
            return new ProcessingSet<>(this.recipeType, this.layout,
                    this.frontOverlay, this.voltages);
        }
    }

    public static <T extends ProcessingRecipe<T>> Builder<T>
    builder(RecipeTypeEntry<T, ?> recipeType) {
        return new Builder<>(recipeType);
    }
}
