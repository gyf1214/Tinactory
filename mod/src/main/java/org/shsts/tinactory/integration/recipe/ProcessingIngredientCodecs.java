package org.shsts.tinactory.integration.recipe;

import com.mojang.serialization.Codec;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.shsts.tinactory.api.recipe.IProcessingIngredient;
import org.shsts.tinactory.api.recipe.IProcessingObject;
import org.shsts.tinactory.core.recipe.ProcessingIngredients;

import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ProcessingIngredientCodecs {
    private static final Codec<ProcessingIngredients.ItemIngredient> ITEM_INGREDIENT_CODEC =
        ItemStack.CODEC.xmap(ProcessingIngredients.ItemIngredient::new, ProcessingIngredients.ItemIngredient::stack);
    private static final Codec<TagIngredient> TAG_INGREDIENT_CODEC = TagIngredient.codec();
    private static final Codec<ProcessingIngredients.FluidIngredient> FLUID_INGREDIENT_CODEC =
        FluidStack.CODEC.xmap(ProcessingIngredients.FluidIngredient::new, ProcessingIngredients.FluidIngredient::stack);
    private static final Map<String, Codec<? extends IProcessingIngredient>> CODECS;

    public static final Codec<IProcessingIngredient> CODEC;

    static {
        CODECS = new HashMap<>();
        CODECS.put(ProcessingIngredients.ItemIngredient.CODEC_NAME, ITEM_INGREDIENT_CODEC);
        CODECS.put(TagIngredient.CODEC_NAME, TAG_INGREDIENT_CODEC);
        CODECS.put(ProcessingIngredients.FluidIngredient.CODEC_NAME, FLUID_INGREDIENT_CODEC);
        CODEC = Codec.STRING.dispatch(IProcessingObject::codecName, CODECS::get);
    }

    private ProcessingIngredientCodecs() {}

    public static Codec<IProcessingIngredient> codec() {
        return CODEC;
    }
}
