package org.shsts.tinactory.content.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.ParametersAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.shsts.tinactory.content.machine.Workbench;
import org.shsts.tinactory.integration.tool.ToolItem;
import org.shsts.tinycorelib.api.recipe.IRecipe;

import java.util.Arrays;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ToolRecipe implements IRecipe<Workbench> {
    public static final MapCodec<ToolRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        ShapedRecipe.Serializer.CODEC.forGetter($ -> $.shapedRecipe),
        Ingredient.CODEC.listOf().fieldOf("tools").forGetter($ -> $.toolIngredients)
    ).apply(instance, ToolRecipe::new));

    public final ShapedRecipe shapedRecipe;
    public final List<Ingredient> toolIngredients;

    public ToolRecipe(ShapedRecipe shapedRecipe, List<Ingredient> toolIngredients) {
        this.shapedRecipe = shapedRecipe;
        this.toolIngredients = toolIngredients;
    }

    private boolean matchTools(IItemHandler toolStorage) {
        for (var ingredient : toolIngredients) {
            var found = false;
            for (var i = 0; i < toolStorage.getSlots(); i++) {
                var stack = toolStorage.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean matches(Workbench container) {
        return shapedRecipe.matches(container.getCraftingInput(), container.world()) &&
            matchTools(container.getToolStorage());
    }

    public ItemStack assemble(Workbench container) {
        return shapedRecipe.getResultItem(container.world().registryAccess()).copy();
    }

    public List<ItemStack> getRemainingItems(Workbench container) {
        return shapedRecipe.getRemainingItems(container.getCraftingInput());
    }

    public void doDamage(IItemHandlerModifiable toolStorage) {
        var damages = new int[toolStorage.getSlots()];
        Arrays.fill(damages, 0);

        for (var ingredient : toolIngredients) {
            var found = -1;
            for (var i = 0; i < toolStorage.getSlots(); i++) {
                var stack = toolStorage.getStackInSlot(i);
                if (ingredient.test(stack)) {
                    found = i;
                    break;
                }
            }
            if (found != -1) {
                damages[found] += 1;
            }
        }

        for (var i = 0; i < toolStorage.getSlots(); i++) {
            var stack = toolStorage.getStackInSlot(i);
            var stack1 = ToolItem.doDamage(stack, damages[i]);
            toolStorage.setStackInSlot(i, stack1);
        }
    }
}
