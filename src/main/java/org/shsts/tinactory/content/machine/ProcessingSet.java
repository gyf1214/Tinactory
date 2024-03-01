package org.shsts.tinactory.content.machine;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.model.ModelGen;
import org.shsts.tinactory.core.common.SmartBlockEntityType;
import org.shsts.tinactory.core.common.Transformer;
import org.shsts.tinactory.core.common.ValueHolder;
import org.shsts.tinactory.core.gui.Layout;
import org.shsts.tinactory.core.recipe.ProcessingRecipe;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;
import org.shsts.tinactory.registrate.builder.BlockEntityBuilder;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record ProcessingSet<T extends ProcessingRecipe<T>>(
        RegistryEntry<MachineBlock<Machine>> block,
        RegistryEntry<SmartBlockEntityType<Machine>> blockEntity,
        RecipeTypeEntry<T, ?> recipeType) {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public MachineBlock<Machine> getBlock() {
        return this.block.get();
    }

    public static <T extends ProcessingRecipe<T>> ProcessingSet<T>
    primitive(String id, RecipeTypeEntry<T, ?> recipeType, ResourceLocation overlay, Layout layout) {
        return create(id, recipeType, Voltage.PRIMITIVE, overlay, builder -> builder
                .capability(AllCapabilities.STACK_CONTAINER, $ -> $.layout(layout, Voltage.PRIMITIVE))
                .menu().layout(layout, Voltage.PRIMITIVE).build());
    }

    public static <T extends ProcessingRecipe<T>> ProcessingSet<T>
    create(String id, RecipeTypeEntry<T, ?> recipeType,
           Voltage voltage, ResourceLocation overlay,
           Transformer<BlockEntityBuilder<Machine, Registrate, ?>> trans) {

        var holder = ValueHolder.<Supplier<SmartBlockEntityType<Machine>>>create();

        var blockBuilder = REGISTRATE.entityBlock(id, MachineBlock<Machine>::new)
                .type(holder)
                .transform(ModelGen.machine(voltage, overlay))
                .tag(AllTags.MINEABLE_WITH_WRENCH)
                .dropSelf().blockItem()
                .tag(AllTags.processingMachine(recipeType))
                .build();
        if (voltage == Voltage.PRIMITIVE) {
            blockBuilder.tag(BlockTags.MINEABLE_WITH_AXE);
        }
        var block = blockBuilder.register();

        var entityBuilder = REGISTRATE.blockEntity(id,
                        voltage == Voltage.PRIMITIVE ? Machine::primitive : Machine::new)
                .entityClass(Machine.class)
                .validBlock(block)
                .capability(AllCapabilities.RECIPE_PROCESSOR, $ -> $
                        .recipeType(recipeType.get()).voltage(voltage))
                .transform(trans.cast());
        if (voltage == Voltage.PRIMITIVE) {
            entityBuilder.ticking();
        }
        var blockEntity = entityBuilder.register();

        holder.setValue(blockEntity);

        return new ProcessingSet<>(block, blockEntity, recipeType);
    }
}
