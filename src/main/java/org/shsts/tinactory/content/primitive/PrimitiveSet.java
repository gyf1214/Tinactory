package org.shsts.tinactory.content.primitive;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import org.shsts.tinactory.Tinactory;
import org.shsts.tinactory.content.AllCapabilities;
import org.shsts.tinactory.content.AllTags;
import org.shsts.tinactory.content.machine.Voltage;
import org.shsts.tinactory.content.recipe.ProcessingRecipe;
import org.shsts.tinactory.core.SmartBlockEntityType;
import org.shsts.tinactory.core.ValueHolder;
import org.shsts.tinactory.gui.layout.Layout;
import org.shsts.tinactory.model.ModelGen;
import org.shsts.tinactory.registrate.RecipeTypeEntry;
import org.shsts.tinactory.registrate.Registrate;
import org.shsts.tinactory.registrate.RegistryEntry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public record PrimitiveSet<T extends ProcessingRecipe<T>>(
        RegistryEntry<PrimitiveBlock<PrimitiveMachine>> block,
        RegistryEntry<SmartBlockEntityType<PrimitiveMachine>> blockEntity,
        RecipeTypeEntry<T, ?> recipeType,
        Layout layout) {
    private static final Registrate REGISTRATE = Tinactory.REGISTRATE;

    public Block getBlock() {
        return this.block.get();
    }

    public static <T extends ProcessingRecipe<T>> PrimitiveSet<T>
    create(String id, ResourceLocation overlay, RecipeTypeEntry<T, ?> recipeType, Layout layout) {
        return create(REGISTRATE, id, overlay, recipeType, layout);
    }

    public static <T extends ProcessingRecipe<T>> PrimitiveSet<T>
    create(Registrate registrate, String id, ResourceLocation overlay,
           RecipeTypeEntry<T, ?> recipeType, Layout layout) {
        var holder = ValueHolder.<Supplier<SmartBlockEntityType<PrimitiveMachine>>>create();

        var block = registrate.entityBlock(id, PrimitiveBlock<PrimitiveMachine>::new)
                .type(holder)
                .transform(ModelGen.primitiveMachine(overlay))
                .tag(BlockTags.MINEABLE_WITH_AXE, AllTags.MINEABLE_WITH_WRENCH)
                .defaultBlockItem().dropSelf()
                .register();

        var blockEntity = registrate.blockEntity(id, PrimitiveMachine::new)
                .entityClass(PrimitiveMachine.class)
                .validBlock(block)
                .ticking()
                .capability(AllCapabilities.PROCESSING_STACK_CONTAINER, $ -> $
                        .recipeType(recipeType)
                        .layout(layout, Voltage.PRIMITIVE))
                .menu().layout(layout, Voltage.PRIMITIVE).build()
                .register();

        holder.setValue(blockEntity);

        return new PrimitiveSet<>(block, blockEntity, recipeType, layout);
    }
}
